package com.badfic.philbot.commands.events;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.BaseNormalCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.data.Quote;
import com.badfic.philbot.data.QuoteRepository;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.service.OnJdaReady;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class QuoteTrivia extends BaseNormalCommand implements OnJdaReady {

    private final QuoteRepository quoteRepository;

    public QuoteTrivia(QuoteRepository quoteRepository) {
        name = "quoteTrivia";
        ownerCommand = true;
        this.quoteRepository = quoteRepository;
    }

    @Override
    public void run() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        if (Objects.nonNull(swampyGamesConfig.getQuoteTriviaExpiration())) {
            scheduleTask(this::triviaComplete, swampyGamesConfig.getQuoteTriviaExpiration());
        }
    }

    @Override
    public void execute(CommandEvent event) {
        quoteTrivia();
    }

    @Scheduled(cron = "${swampy.schedule.events.quotetrivia}", zone = "${swampy.schedule.timezone}")
    public void quoteTrivia() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);
        Guild guild = philJda.getGuildById(baseConfig.guildId);

        if (swampyGamesConfig.getQuoteTriviaMsgId() != null) {
            swampysChannel.sendMessage("There is currently a quote trivia running, you can't trigger another.").queue();
            return;
        }

        List<Long> allQuotes = quoteRepository.findAllIds();

        if (CollectionUtils.isEmpty(allQuotes)) {
            swampysChannel.sendMessage("There are no quotes to do a quote trivia").queue();
            return;
        }

        Collections.shuffle(allQuotes);

        Quote quote = null;
        Member member = null;
        for (Long quoteId : allQuotes) {
            Optional<Quote> optionalQuote = quoteRepository.findById(quoteId);

            if (optionalQuote.isPresent()) {
                quote = optionalQuote.get();

                if (StringUtils.isBlank(quote.getImage())) {
                    member = guild.getMemberById(quote.getUserId());

                    if (member != null) {
                        break;
                    }
                }
            }
        }

        if (member == null) {
            swampysChannel.sendMessage("There are no quotes with valid active swamplings to do a quote trivia").queue();
            return;
        }
        final Member finalMember = member;
        final Quote finalQuote = quote;

        List<Member> members = guild.getMemberCache().stream().collect(Collectors.toList());
        Collections.shuffle(members);

        Set<Member> otherTwoMembers = new HashSet<>();
        for (Member randomMember : members) {
            if (randomMember.getIdLong() != finalMember.getIdLong()) {
                otherTwoMembers.add(randomMember);
            }

            if (otherTwoMembers.size() > 1) {
                break;
            }
        }

        if (otherTwoMembers.size() < 2) {
            swampysChannel.sendMessage("Not enough members to run a quote trivia").queue();
            return;
        }

        short correctAnswer = (short) ThreadLocalRandom.current().nextInt(3);

        String description = "Who said the following quote?" +
                "\n\"" + finalQuote.getQuote() + '"' +
                "\n\nA: " + getTriviaAnswer((short) 0, finalMember, otherTwoMembers, correctAnswer) +
                "\n\nB: " + getTriviaAnswer((short) 1, finalMember, otherTwoMembers, correctAnswer) +
                "\n\nC: " + getTriviaAnswer((short) 2, finalMember, otherTwoMembers, correctAnswer);
        String title = "Quote Trivia time! (for " + swampyGamesConfig.getQuoteTriviaEventPoints() + " points)";
        swampysChannel.sendMessageEmbeds(Constants.simpleEmbed(title, description)).queue(success -> {
            swampyGamesConfig.setQuoteTriviaCorrectAnswer(correctAnswer);
            swampyGamesConfig.setQuoteTriviaMsgId(success.getId());
            LocalDateTime expiration = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(15);
            swampyGamesConfig.setQuoteTriviaExpiration(expiration);
            saveSwampyGamesConfig(swampyGamesConfig);

            scheduleTask(this::triviaComplete, expiration);

            success.addReaction(Emoji.fromUnicode("\uD83C\uDDE6")).queue();
            success.addReaction(Emoji.fromUnicode("\uD83C\uDDE7")).queue();
            success.addReaction(Emoji.fromUnicode("\uD83C\uDDE8")).queue();
        });
    }

    public void triviaComplete() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);

        if (Objects.isNull(swampyGamesConfig.getQuoteTriviaMsgId())) {
            swampysChannel.sendMessage("Could not find quote trivia question anymore. Failed to award points.").queue();
            return;
        }

        String quoteTriviaMsgId = swampyGamesConfig.getQuoteTriviaMsgId();
        short quoteTriviaCorrectAnswer = swampyGamesConfig.getQuoteTriviaCorrectAnswer();
        int quoteTriviaEventPoints = swampyGamesConfig.getQuoteTriviaEventPoints();

        swampyGamesConfig.setQuoteTriviaCorrectAnswer(null);
        swampyGamesConfig.setQuoteTriviaMsgId(null);
        swampyGamesConfig.setQuoteTriviaExpiration(null);
        saveSwampyGamesConfig(swampyGamesConfig);

        Guild guild = philJda.getGuildById(baseConfig.guildId);

        StringBuilder description = new StringBuilder();

        Message msg;
        try {
            msg = swampysChannel.retrieveMessageById(quoteTriviaMsgId).timeout(30, TimeUnit.SECONDS).complete();
        } catch (Exception e) {
            swampysChannel.sendMessage("Could not find quote trivia question anymore. Failed to award points.").queue();
            return;
        }

        long wrongPoints = quoteTriviaEventPoints * -1;

        List<CompletableFuture<?>> futures = new ArrayList<>();

        List<User> users = msg.retrieveReactionUsers(Emoji.fromUnicode("\uD83C\uDDE6")).timeout(30, TimeUnit.SECONDS).complete();
        awardPoints(description, users, quoteTriviaCorrectAnswer == 0 ? (long) quoteTriviaEventPoints : wrongPoints, futures, guild);

        users = msg.retrieveReactionUsers(Emoji.fromUnicode("\uD83C\uDDE7")).timeout(30, TimeUnit.SECONDS).complete();
        awardPoints(description, users, quoteTriviaCorrectAnswer == 1 ? (long) quoteTriviaEventPoints : wrongPoints, futures, guild);

        users = msg.retrieveReactionUsers(Emoji.fromUnicode("\uD83C\uDDE8")).timeout(30, TimeUnit.SECONDS).complete();
        awardPoints(description, users, quoteTriviaCorrectAnswer == 2 ? (long) quoteTriviaEventPoints : wrongPoints, futures, guild);

        msg.clearReactions().queue();

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
            swampysChannel.sendMessageEmbeds(Constants.simpleEmbed("Quote Trivia Results", description.toString())).queue();
        });
    }

    private void awardPoints(StringBuilder description, List<User> users, long points, List<CompletableFuture<?>> futures, Guild guild) {
        for (User user : users) {
            if (user.getId().equals(philJda.getSelfUser().getId())) {
                continue;
            }

            Member memberById = guild.getMemberById(user.getId());
            if (memberById == null) {
                description.append("Could not award points to <@")
                        .append(user.getId())
                        .append(">\n");
            } else {
                if (isNotParticipating(memberById)) {
                    description.append("<@")
                            .append(user.getId())
                            .append("> Please ask a mod to check your roles to participate in the swampys\n");
                } else {
                    description.append("Awarded ")
                            .append(points)
                            .append(" points to <@")
                            .append(user.getId())
                            .append(">\n");
                }
                if (points < 1) {
                    futures.add(takePointsFromMember(points * -1, memberById, PointsStat.QUOTE_TRIVIA));
                } else {
                    futures.add(givePointsToMember(points, memberById, PointsStat.QUOTE_TRIVIA));
                }
            }
        }
    }

    private String getTriviaAnswer(short position, Member correctMember, Set<Member> otherTwoMembers, short correctAnswer) {
        final Member selectedMember;
        if (correctAnswer == position) {
            selectedMember = correctMember;
        } else {
            Iterator<Member> i = otherTwoMembers.iterator();
            Member next = i.next();
            i.remove();
            selectedMember = next;
        }

        return selectedMember.getAsMention() + " (" + selectedMember.getEffectiveName() + ')';
    }

}
