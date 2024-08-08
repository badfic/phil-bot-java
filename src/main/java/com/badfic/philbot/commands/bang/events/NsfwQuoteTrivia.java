package com.badfic.philbot.commands.bang.events;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.bang.BaseBangCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.NsfwQuote;
import com.badfic.philbot.data.NsfwQuoteRepository;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.service.OnJdaReady;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NsfwQuoteTrivia extends BaseBangCommand implements OnJdaReady {

    private final NsfwQuoteRepository nsfwQuoteRepository;

    public NsfwQuoteTrivia(final NsfwQuoteRepository nsfwQuoteRepository) {
        name = "nsfwQuoteTrivia";
        ownerCommand = true;
        this.nsfwQuoteRepository = nsfwQuoteRepository;
    }

    @Override
    public void run() {
        final var swampyGamesConfig = getSwampyGamesConfig();

        if (Objects.nonNull(swampyGamesConfig.getNsfwQuoteTriviaExpiration())) {
            scheduleTask(this::triviaComplete, swampyGamesConfig.getNsfwQuoteTriviaExpiration());
        }
    }

    @Override
    public void execute(final CommandEvent event) {
        quoteTrivia();
    }

    @Scheduled(cron = "${swampy.schedule.events.nsfwquotetrivia}", zone = "${swampy.schedule.timezone}")
    void quoteTrivia() {
        final var swampyGamesConfig = getSwampyGamesConfig();
        final var triviaChannel = philJda.getTextChannelsByName(Constants.CURSED_SWAMP_CHANNEL, false).getFirst();
        final var guild = philJda.getGuildById(baseConfig.guildId);

        if (swampyGamesConfig.getNsfwQuoteTriviaMsgId() != null) {
            triviaChannel.sendMessage("There is currently an nsfw quote trivia running, you can't trigger another.").queue();
            return;
        }

        final var allQuotes = nsfwQuoteRepository.findAllIds();

        if (CollectionUtils.isEmpty(allQuotes)) {
            triviaChannel.sendMessage("There are no nsfw quotes to do a trivia").queue();
            return;
        }

        Collections.shuffle(allQuotes);

        NsfwQuote quote = null;
        Member member = null;
        for (final var quoteId : allQuotes) {
            final var optionalQuote = nsfwQuoteRepository.findById(quoteId);

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
        final var finalMember = member;
        final var finalQuote = quote;

        final var members = guild.getMemberCache().stream().collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(members);

        final var otherTwoMembers = new HashSet<Member>();
        for (final var randomMember : members) {
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

        final var correctAnswer = (short) randomNumberService.nextInt(3);

        final var description = "Who said the following quote?" +
                "\n\"" + finalQuote.getQuote() + '"' +
                "\n\nA: " + getTriviaAnswer((short) 0, finalMember, otherTwoMembers, correctAnswer) +
                "\n\nB: " + getTriviaAnswer((short) 1, finalMember, otherTwoMembers, correctAnswer) +
                "\n\nC: " + getTriviaAnswer((short) 2, finalMember, otherTwoMembers, correctAnswer);
        final var title = "\uD83C\uDF46 Quote Trivia time! (for " + swampyGamesConfig.getNsfwQuoteTriviaEventPoints() + " points)";
        triviaChannel.sendMessageEmbeds(Constants.simpleEmbed(title, description)).queue(success -> {
            swampyGamesConfig.setNsfwQuoteTriviaCorrectAnswer(correctAnswer);
            swampyGamesConfig.setNsfwQuoteTriviaMsgId(success.getId());
            final var expiration = LocalDateTime.now().plusMinutes(15);
            swampyGamesConfig.setNsfwQuoteTriviaExpiration(expiration);
            saveSwampyGamesConfig(swampyGamesConfig);

            scheduleTask(this::triviaComplete, expiration);

            success.addReaction(Emoji.fromUnicode("\uD83C\uDDE6")).queue();
            success.addReaction(Emoji.fromUnicode("\uD83C\uDDE7")).queue();
            success.addReaction(Emoji.fromUnicode("\uD83C\uDDE8")).queue();
        });
    }

    private void triviaComplete() {
        final var swampyGamesConfig = getSwampyGamesConfig();
        final var swampysChannel = philJda.getTextChannelsByName(Constants.CURSED_SWAMP_CHANNEL, false).getFirst();

        if (Objects.isNull(swampyGamesConfig.getNsfwQuoteTriviaMsgId())) {
            swampysChannel.sendMessage("Could not find nsfw quote trivia question anymore. Failed to award points.").queue();
            return;
        }

        final var quoteTriviaMsgId = swampyGamesConfig.getNsfwQuoteTriviaMsgId();
        final var quoteTriviaCorrectAnswer = swampyGamesConfig.getNsfwQuoteTriviaCorrectAnswer();
        final var quoteTriviaEventPoints = swampyGamesConfig.getNsfwQuoteTriviaEventPoints();

        swampyGamesConfig.setNsfwQuoteTriviaCorrectAnswer(null);
        swampyGamesConfig.setNsfwQuoteTriviaMsgId(null);
        swampyGamesConfig.setNsfwQuoteTriviaExpiration(null);
        saveSwampyGamesConfig(swampyGamesConfig);

        final var guild = philJda.getGuildById(baseConfig.guildId);

        final var description = new StringBuilder();

        final Message msg;
        try {
            msg = swampysChannel.retrieveMessageById(quoteTriviaMsgId).timeout(30, TimeUnit.SECONDS).complete();
        } catch (final Exception e) {
            swampysChannel.sendMessage("Could not find nsfw quote trivia question anymore. Failed to award points.").queue();
            return;
        }

        final var wrongPoints = quoteTriviaEventPoints * -1;

        final var futures = new ArrayList<CompletableFuture<?>>();

        var users = msg.retrieveReactionUsers(Emoji.fromUnicode("\uD83C\uDDE6")).timeout(30, TimeUnit.SECONDS).complete();
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

    private void awardPoints(final StringBuilder description, final List<User> users, final long points, final List<CompletableFuture<?>> futures, final Guild guild) {
        for (final var user : users) {
            if (user.getId().equals(philJda.getSelfUser().getId())) {
                continue;
            }

            final var memberById = guild.getMemberById(user.getId());
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

    private String getTriviaAnswer(final short position, final Member correctMember, final Set<Member> otherTwoMembers, final short correctAnswer) {
        final Member selectedMember;
        if (correctAnswer == position) {
            selectedMember = correctMember;
        } else {
            final var i = otherTwoMembers.iterator();
            final var next = i.next();
            i.remove();
            selectedMember = next;
        }

        return selectedMember.getAsMention() + " (" + selectedMember.getEffectiveName() + ')';
    }

}
