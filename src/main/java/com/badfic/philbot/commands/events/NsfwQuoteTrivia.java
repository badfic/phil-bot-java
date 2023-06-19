package com.badfic.philbot.commands.events;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.BaseNormalCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.NsfwQuote;
import com.badfic.philbot.data.NsfwQuoteRepository;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.service.OnJdaReady;
import java.time.LocalDateTime;
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
public class NsfwQuoteTrivia extends BaseNormalCommand implements OnJdaReady {

    private final NsfwQuoteRepository nsfwQuoteRepository;

    public NsfwQuoteTrivia(NsfwQuoteRepository nsfwQuoteRepository) {
        name = "nsfwQuoteTrivia";
        ownerCommand = true;
        this.nsfwQuoteRepository = nsfwQuoteRepository;
    }

    @Override
    public void run() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        if (Objects.nonNull(swampyGamesConfig.getNsfwQuoteTriviaExpiration())) {
            scheduleTask(this::triviaComplete, swampyGamesConfig.getNsfwQuoteTriviaExpiration());
        }
    }

    @Override
    public void execute(CommandEvent event) {
        quoteTrivia();
    }

    @Scheduled(cron = "${swampy.schedule.events.nsfwquotetrivia}", zone = "${swampy.schedule.timezone}")
    public void quoteTrivia() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        TextChannel triviaChannel = philJda.getTextChannelsByName(Constants.CURSED_SWAMP_CHANNEL, false).get(0);
        Guild guild = philJda.getGuildById(baseConfig.guildId);

        if (swampyGamesConfig.getNsfwQuoteTriviaMsgId() != null) {
            triviaChannel.sendMessage("There is currently an nsfw quote trivia running, you can't trigger another.").queue();
            return;
        }

        List<Long> allQuotes = nsfwQuoteRepository.findAllIds();

        if (CollectionUtils.isEmpty(allQuotes)) {
            triviaChannel.sendMessage("There are no nsfw quotes to do a trivia").queue();
            return;
        }

        Collections.shuffle(allQuotes);

        NsfwQuote quote = null;
        Member member = null;
        for (Long quoteId : allQuotes) {
            Optional<NsfwQuote> optionalQuote = nsfwQuoteRepository.findById(quoteId);

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
            triviaChannel.sendMessage("There are no nsfw quotes with valid active swamplings to do a trivia").queue();
            return;
        }
        final Member finalMember = member;
        final NsfwQuote finalQuote = quote;

        List<Member> members = guild.getMemberCache().stream().collect(Collectors.toList());
        Collections.shuffle(members);

        Set<Member> otherTwoMembers = new HashSet<>();
        for (Member randomMember : members) {
            if (randomMember.getIdLong() != finalMember.getIdLong() && hasRole(randomMember, Constants.EIGHTEEN_PLUS_ROLE)) {
                otherTwoMembers.add(randomMember);
            }

            if (otherTwoMembers.size() > 1) {
                break;
            }
        }

        if (otherTwoMembers.size() < 2) {
            triviaChannel.sendMessage("Not enough members to run an \uD83C\uDF46 quote trivia").queue();
            return;
        }

        short correctAnswer = (short) ThreadLocalRandom.current().nextInt(3);

        String description = "Who said the following quote?" +
                "\n\"" + finalQuote.getQuote() + '"' +
                "\n\nA: " + getTriviaAnswer((short) 0, finalMember, otherTwoMembers, correctAnswer) +
                "\n\nB: " + getTriviaAnswer((short) 1, finalMember, otherTwoMembers, correctAnswer) +
                "\n\nC: " + getTriviaAnswer((short) 2, finalMember, otherTwoMembers, correctAnswer);
        String title = "\uD83C\uDF46 Quote Trivia time! (for " + swampyGamesConfig.getNsfwQuoteTriviaEventPoints() + " points)";
        triviaChannel.sendMessageEmbeds(Constants.simpleEmbed(title, description)).queue(success -> {
            swampyGamesConfig.setNsfwQuoteTriviaCorrectAnswer(correctAnswer);
            swampyGamesConfig.setNsfwQuoteTriviaMsgId(success.getId());
            LocalDateTime expiration = LocalDateTime.now().plusMinutes(15);
            swampyGamesConfig.setNsfwQuoteTriviaExpiration(expiration);
            saveSwampyGamesConfig(swampyGamesConfig);

            scheduleTask(this::triviaComplete, expiration);

            success.addReaction(Emoji.fromUnicode("\uD83C\uDDE6")).queue();
            success.addReaction(Emoji.fromUnicode("\uD83C\uDDE7")).queue();
            success.addReaction(Emoji.fromUnicode("\uD83C\uDDE8")).queue();
        });
    }

    private void triviaComplete() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.CURSED_SWAMP_CHANNEL, false).get(0);

        if (Objects.isNull(swampyGamesConfig.getNsfwQuoteTriviaMsgId())) {
            swampysChannel.sendMessage("Could not find nsfw quote trivia question anymore. Failed to award points.").queue();
            return;
        }

        String quoteTriviaMsgId = swampyGamesConfig.getNsfwQuoteTriviaMsgId();
        short quoteTriviaCorrectAnswer = swampyGamesConfig.getNsfwQuoteTriviaCorrectAnswer();
        int quoteTriviaEventPoints = swampyGamesConfig.getNsfwQuoteTriviaEventPoints();

        swampyGamesConfig.setNsfwQuoteTriviaCorrectAnswer(null);
        swampyGamesConfig.setNsfwQuoteTriviaMsgId(null);
        swampyGamesConfig.setNsfwQuoteTriviaExpiration(null);
        saveSwampyGamesConfig(swampyGamesConfig);

        Guild guild = philJda.getGuildById(baseConfig.guildId);

        StringBuilder description = new StringBuilder();

        Message msg;
        try {
            msg = swampysChannel.retrieveMessageById(quoteTriviaMsgId).timeout(30, TimeUnit.SECONDS).complete();
        } catch (Exception e) {
            swampysChannel.sendMessage("Could not find nsfw quote trivia question anymore. Failed to award points.").queue();
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
            swampysChannel.sendMessageEmbeds(Constants.simpleEmbed("\uD83C\uDF46 Quote Trivia Results", description.toString())).queue();
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
