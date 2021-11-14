package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.NsfwQuote;
import com.badfic.philbot.data.phil.NsfwQuoteRepository;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NsfwQuoteTrivia extends BaseSwampy {

    @Resource
    private NsfwQuoteRepository nsfwQuoteRepository;

    public NsfwQuoteTrivia() {
        name = "nsfwQuoteTrivia";
        ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        quoteTrivia();
    }

    @Scheduled(cron = "${swampy.schedule.events.nsfwquotetrivia}", zone = "${swampy.schedule.timezone}")
    public void quoteTrivia() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        TextChannel triviaChannel = philJda.getTextChannelsByName(Constants.CURSED_SWAMP_CHANNEL, false).get(0);
        Guild guild = philJda.getGuilds().get(0);

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
            swampyGamesConfig.setNsfwQuoteTriviaExpiration(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(15));
            swampyGamesConfigRepository.save(swampyGamesConfig);

            success.addReaction("\uD83C\uDDE6").queue();
            success.addReaction("\uD83C\uDDE7").queue();
            success.addReaction("\uD83C\uDDE8").queue();
        });
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
