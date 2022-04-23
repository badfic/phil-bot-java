package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.Quote;
import com.badfic.philbot.data.phil.QuoteRepository;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.badfic.philbot.service.DailyTickable;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.time.DayOfWeek;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class QuoteCommand extends BaseSwampy implements DailyTickable {

    private static final String SPEECH_BUBBLE_EMOJI = "\uD83D\uDCAC";

    @Resource(name = "johnJda")
    @Lazy
    private JDA johnJda;

    @Resource
    private QuoteRepository quoteRepository;

    public QuoteCommand() {
        name = "quote";
        aliases = new String[] {"quotes"};
        help = """
                React to any message with the \uD83D\uDCAC emoji to save a Quote
                `!!quote` to get a random quote
                `!!quote 23` to get quote number 23
                `!!quote stats` returns statistics about all quotes
                `!!quote stats @Santiago` returns statistics about Santiago
                `!!quote cache` runs the meme saver if a channel is configured""";
    }

    @Override
    public void runDailyTask() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        long channelId = swampyGamesConfig.getSfwSavedMemesChannelId();

        if (channelId > 0) {
            Set<String> images = quoteRepository.findAllNonNullImages();

            TextChannel philLookedUpChannel = philJda.getTextChannelById(channelId);
            TextChannel johnLookedUpChannel = johnJda.getTextChannelById(channelId);

            if (philLookedUpChannel == null || johnLookedUpChannel == null) {
                Constants.debugToTestChannel(johnJda, "Could not update nsfw saved memes channel, it does not exist");
                return;
            }

            long lastMsgId = 0;
            while (lastMsgId != -1) {
                MessageHistory history;
                if (lastMsgId == 0) {
                    history = philLookedUpChannel.getHistoryFromBeginning(100).timeout(30, TimeUnit.SECONDS).complete();
                } else {
                    history = philLookedUpChannel.getHistoryAfter(lastMsgId, 100).timeout(30, TimeUnit.SECONDS).complete();
                }

                lastMsgId = CollectionUtils.isNotEmpty(history.getRetrievedHistory())
                        ? history.getRetrievedHistory().get(0).getIdLong()
                        : -1;

                for (Message message : history.getRetrievedHistory()) {
                    images.remove(message.getContentRaw());
                }
            }

            for (String image : images) {
                johnLookedUpChannel.sendMessage(image).queue();
            }

            Constants.debugToTestChannel(johnJda, "Successfully updated sfw saved memes channel");
        } else {
            Constants.debugToTestChannel(johnJda, "Could not update sfw saved memes channel, no channel is configured");
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        if (StringUtils.startsWithIgnoreCase(event.getArgs(), "cache")) {
            if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
                event.replyError("You do not have permission to execute this command");
                return;
            }

            userTriggeredTasksExecutor.execute(this::runDailyTask);
            return;
        }

        if (StringUtils.isBlank(event.getArgs())) {
            int count = (int) quoteRepository.count();

            if (count < 1) {
                johnJda.getTextChannelById(event.getChannel().getIdLong())
                        .sendMessage("Could not find any quotes")
                        .queue();
                return;
            }

            int idx = ThreadLocalRandom.current().nextInt(0, count);
            Page<Quote> quotes = quoteRepository.findAll(PageRequest.of(idx, 1));
            Quote quote = quotes.getContent().get(0);

            respondWithQuote(event, quote);
            return;
        }

        if (StringUtils.startsWithIgnoreCase(event.getArgs(), "stat")) {
            if (CollectionUtils.isNotEmpty(event.getMessage().getMentionedMembers())) {
                Member member = event.getMessage().getMentionedMembers().get(0);
                long memberId = member.getIdLong();
                int[] quoteDaysOfWeekForUser = quoteRepository.getQuoteDaysOfWeekForUser(memberId);
                Pair<DayOfWeek, Integer> mode = Constants.isoDayOfWeekMode(quoteDaysOfWeekForUser);

                DayOfWeek day = mode.getLeft();
                int count = mode.getRight();

                johnJda.getTextChannelById(event.getChannel().getIdLong()).
                        sendMessageEmbeds(Constants.simpleEmbed("User Quote Statistics",
                                String.format("""
                                                %s Statistics:

                                                Total number of quotes: %d

                                                Day of week with the most quotes: %s
                                                Number of quotes on %sS: %d""",
                                        member.getAsMention(),
                                        quoteDaysOfWeekForUser.length,
                                        day,
                                        day,
                                        count))).queue();
                return;
            }

            int[] quoteDaysOfWeek = quoteRepository.getQuoteDaysOfWeek();
            Pair<DayOfWeek, Integer> mode = Constants.isoDayOfWeekMode(quoteDaysOfWeek);

            DayOfWeek day = mode.getLeft();
            int count = mode.getRight();

            long mostQuotedUserId = quoteRepository.getMostQuotedUser();
            Member mostQuotedMember = event.getGuild().getMemberById(mostQuotedUserId);

            johnJda.getTextChannelById(event.getChannel().getIdLong()).sendMessageEmbeds(Constants.simpleEmbed("Overall Quote Statistics",
                    String.format("""
                                    Day of the week with the most quotes: %s
                                    Total number of quotes on %sS: %d

                                    User quoted the most: %s""",
                            day,
                            day,
                            count,
                            mostQuotedMember != null ? mostQuotedMember.getAsMention() : "<@!" + mostQuotedUserId + ">"))).queue();
            return;
        }

        if (StringUtils.startsWithIgnoreCase(event.getArgs(), "delete")) {
            String[] split = event.getArgs().split("\\s+");
            if (ArrayUtils.getLength(split) != 2) {
                event.replyError("Please specify a quote to delete: `!!quote delete 123`");
                return;
            }

            try {
                long id = Long.parseLong(split[1]);
                Optional<Quote> optionalQuote = quoteRepository.findById(id);

                if (optionalQuote.isPresent()) {
                    quoteRepository.deleteById(id);
                    johnJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("Quote #" + id + " successfully deleted").queue();
                } else {
                    johnJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("Quote #" + id + " does not exist").queue();
                }
            } catch (NumberFormatException e) {
                johnJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("Could not parse quote number from: " + split[1]).queue();
            }

            return;
        }

        try {
            long id = Long.parseLong(event.getArgs());
            Optional<Quote> optionalQuote = quoteRepository.findById(id);

            if (optionalQuote.isEmpty()) {
                johnJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("Quote #" + id + " does not exist").queue();
                return;
            }

            respondWithQuote(event, optionalQuote.get());
        } catch (NumberFormatException e) {
            johnJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("Did not recognize number " + event.getArgs()).queue();
        }
    }

    public String getEmoji() {
        return SPEECH_BUBBLE_EMOJI;
    }

    public void saveQuote(GenericGuildMessageReactionEvent event) {
        long messageId = event.getMessageIdLong();
        long channelId = event.getChannel().getIdLong();
        long guildId = event.getGuild().getIdLong();
        long quoterId = event.getUserIdLong();

        if (!quoteRepository.existsByMessageId(messageId)) {
            johnJda.getTextChannelById(channelId).retrieveMessageById(messageId).queue(msg -> {
                String image = null;
                if (CollectionUtils.isNotEmpty(msg.getEmbeds())) {
                    image = msg.getEmbeds().get(0).getUrl();
                }
                if (CollectionUtils.isNotEmpty(msg.getAttachments())) {
                    image = msg.getAttachments().get(0).getUrl();
                }

                Quote savedQuote = quoteRepository.save(new Quote(messageId, channelId, msg.getContentRaw(), image,
                        msg.getAuthor().getIdLong(), msg.getTimeCreated().toLocalDateTime()));

                msg.addReaction(SPEECH_BUBBLE_EMOJI).queue();

                String msgLink = " [(jump)](https://discordapp.com/channels/" + guildId + '/' + channelId + '/' + messageId + ')';

                MessageEmbed messageEmbed = Constants.simpleEmbed("Quote #" + savedQuote.getId() + " Added",
                        "<@!" + quoterId + "> Added quote #" + savedQuote.getId() + msgLink);

                johnJda.getTextChannelById(event.getChannel().getIdLong()).sendMessageEmbeds(messageEmbed).queue();
            });
        }
    }

    private void respondWithQuote(CommandEvent event, Quote quote) {
        String guildId = event.getGuild().getId();
        String msgLink = "[(jump)](https://discordapp.com/channels/" + guildId + '/' + quote.getChannelId() + '/' + quote.getMessageId() + ')';

        StringBuilder description = new StringBuilder()
                .append(quote.getQuote())
                .append("\n- <@!")
                .append(quote.getUserId())
                .append("> ")
                .append(msgLink);
        johnJda.getTextChannelById(event.getChannel().getIdLong())
                .sendMessageEmbeds(Constants.simpleEmbed("Quote #" + quote.getId(), description.toString(), quote.getImage(),
                        TIMESTAMP_FORMAT.format(quote.getCreated()))).queue();
    }

}
