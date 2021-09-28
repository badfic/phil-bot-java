package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.Quote;
import com.badfic.philbot.data.phil.QuoteRepository;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Resource;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class QuoteTrivia extends BaseSwampy {

    @Resource
    private QuoteRepository quoteRepository;

    public QuoteTrivia() {
        name = "quoteTrivia";
        ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        quoteTrivia();
    }

    @Scheduled(cron = "0 37 2,6,10,14,18,22 * * ?", zone = "GMT")
    public void quoteTrivia() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);
        Guild guild = philJda.getGuilds().get(0);

        if (swampyGamesConfig.getTriviaGuid() != null) {
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

        List<Member> otherTwoMembers = new ArrayList<>();

        // JDA recommends to use forEach as it doesn't create a copy of the underlying store. All other usages/iterations will create a copy.
        guild.getMemberCache().forEach(randomMember -> {
            if (otherTwoMembers.size() > 1) {
                return;
            }

            if (randomMember.getIdLong() != finalMember.getIdLong()) {
                otherTwoMembers.add(randomMember);
            }
        });

        short correctAnswer = (short) ThreadLocalRandom.current().nextInt(3);

        MutableInt otherMembersIterator = new MutableInt(0);
        String description = "Who said the following quote?" +
                "\n\"" + finalQuote.getQuote() + '"' +
                "\n\nA: " + getTriviaAnswer((short) 0, finalMember, otherTwoMembers, correctAnswer, otherMembersIterator) +
                "\n\nB: " + getTriviaAnswer((short) 1, finalMember, otherTwoMembers, correctAnswer, otherMembersIterator) +
                "\n\nC: " + getTriviaAnswer((short) 2, finalMember, otherTwoMembers, correctAnswer, otherMembersIterator);
        String title = "Quote Trivia time! (for " + swampyGamesConfig.getTriviaEventPoints() + " points)";
        swampysChannel.sendMessageEmbeds(Constants.simpleEmbed(title, description)).queue(success -> {
            swampyGamesConfig.setQuoteTriviaCorrectAnswer(correctAnswer);
            swampyGamesConfig.setQuoteTriviaMsgId(success.getId());
            swampyGamesConfig.setQuoteTriviaExpiration(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(15));
            swampyGamesConfigRepository.save(swampyGamesConfig);

            success.addReaction("\uD83C\uDDE6").queue();
            success.addReaction("\uD83C\uDDE7").queue();
            success.addReaction("\uD83C\uDDE8").queue();
        });
    }

    private String getTriviaAnswer(short position, Member correctMember, List<Member> otherTwoMembers, short correctAnswer, MutableInt otherMembersIterator) {
        final Member selectedMember;
        if (correctAnswer == position) {
            selectedMember = correctMember;
        } else {
            selectedMember = otherTwoMembers.get(otherMembersIterator.getAndIncrement());
        }

        return selectedMember.getAsMention() + " (" + selectedMember.getEffectiveName() + ')';
    }

}
