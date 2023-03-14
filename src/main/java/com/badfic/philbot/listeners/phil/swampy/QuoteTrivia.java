package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.Quote;
import com.badfic.philbot.data.phil.QuoteRepository;
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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class QuoteTrivia extends BaseSwampy {

    @Autowired
    private QuoteRepository quoteRepository;

    public QuoteTrivia() {
        name = "quoteTrivia";
        ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
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
            swampyGamesConfig.setQuoteTriviaExpiration(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(15));
            swampyGamesConfigRepository.save(swampyGamesConfig);

            success.addReaction(Emoji.fromUnicode("\uD83C\uDDE6")).queue();
            success.addReaction(Emoji.fromUnicode("\uD83C\uDDE7")).queue();
            success.addReaction(Emoji.fromUnicode("\uD83C\uDDE8")).queue();
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
