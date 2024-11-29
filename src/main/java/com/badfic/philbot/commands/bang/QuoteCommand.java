package com.badfic.philbot.commands.bang;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.Quote;
import com.badfic.philbot.data.QuoteRepository;
import com.badfic.philbot.service.DailyTickable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class QuoteCommand extends BaseBangCommand implements DailyTickable {

    private static final String SPEECH_BUBBLE_EMOJI = "\uD83D\uDCAC";

    private final QuoteRepository quoteRepository;

    public QuoteCommand(final QuoteRepository quoteRepository) {
        name = "quote";
        aliases = new String[] {"quotes"};
        help = """
                React to any message with the \uD83D\uDCAC emoji to save a Quote
                `!!quote` to get a random quote
                `!!quote 23` to get quote number 23
                `!!quote stats` returns statistics about all quotes
                `!!quote stats @Santiago` returns statistics about Santiago
                `!!quote cache` runs the meme saver if a channel is configured""";
        this.quoteRepository = quoteRepository;
    }

    @Override
    public void runDailyTask() {
        final var swampyGamesConfig = getSwampyGamesConfig();
        final var channelId = swampyGamesConfig.getSfwSavedMemesChannelId();

        if (channelId > 0) {
            final var images = quoteRepository.findAllNonNullImages();

            final var philLookedUpChannel = philJda.getTextChannelById(channelId);

            if (philLookedUpChannel == null) {
                Constants.debugToTestChannel(philJda, "Could not update nsfw saved memes channel, it does not exist");
                return;
            }

            var lastMsgId = 0L;
            while (lastMsgId != -1) {
                final MessageHistory history;
                if (lastMsgId == 0) {
                    history = philLookedUpChannel.getHistoryFromBeginning(100).timeout(30, TimeUnit.SECONDS).complete();
                } else {
                    history = philLookedUpChannel.getHistoryAfter(lastMsgId, 100).timeout(30, TimeUnit.SECONDS).complete();
                }

                lastMsgId = CollectionUtils.isNotEmpty(history.getRetrievedHistory())
                        ? history.getRetrievedHistory().getFirst().getIdLong()
                        : -1;

                for (final var message : history.getRetrievedHistory()) {
                    images.remove(message.getContentRaw());
                }
            }

            for (final var image : images) {
                discordWebhookSendService.sendMessage(philLookedUpChannel.getIdLong(), swampyGamesConfig.getJohnNickname(), swampyGamesConfig.getJohnAvatar(), image);
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
            }

            Constants.debugToTestChannel(philJda, "Successfully updated sfw saved memes channel");
        } else {
            Constants.debugToTestChannel(philJda, "Could not update sfw saved memes channel, no channel is configured");
        }
    }

    @Override
    public void execute(final CommandEvent event) {
        if (StringUtils.startsWithIgnoreCase(event.getArgs(), "cache")) {
            if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
                event.replyError("You do not have permission to execute this command");
                return;
            }

            executorService.execute(this::runDailyTask);
            return;
        }

        if (StringUtils.isBlank(event.getArgs())) {
            final var ids = quoteRepository.findAllIds();

            if (CollectionUtils.isEmpty(ids)) {
                philJda.getTextChannelById(event.getChannel().getIdLong())
                        .sendMessage("Could not find any quotes")
                        .queue();
                return;
            }

            final var idx = Constants.pickRandom(ids);
            final var optionalQuote = quoteRepository.findById(idx);

            if (optionalQuote.isEmpty()) {
                philJda.getTextChannelById(event.getChannel().getIdLong())
                        .sendMessage("Could not find any quotes")
                        .queue();
                return;
            }

            respondWithQuote(event, optionalQuote.get());
            return;
        }

        if (StringUtils.startsWithIgnoreCase(event.getArgs(), "stat")) {
            if (CollectionUtils.isNotEmpty(event.getMessage().getMentions().getMembers())) {
                final var member = event.getMessage().getMentions().getMembers().getFirst();
                final var memberId = member.getIdLong();
                final var quoteDaysOfWeekForUser = quoteRepository.getQuoteDaysOfWeekForUser(memberId);
                final var mode = Constants.isoDayOfWeekMode(quoteDaysOfWeekForUser);

                final var day = mode.left();
                final var count = mode.rightInt();

                philJda.getTextChannelById(event.getChannel().getIdLong()).
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

            final var quoteDaysOfWeek = quoteRepository.getQuoteDaysOfWeek();
            final var mode = Constants.isoDayOfWeekMode(quoteDaysOfWeek);

            final var day = mode.left();
            final var count = mode.rightInt();

            final var mostQuotedUserId = quoteRepository.getMostQuotedUser();
            final var mostQuotedMember = event.getGuild().getMemberById(mostQuotedUserId);

            philJda.getTextChannelById(event.getChannel().getIdLong()).sendMessageEmbeds(Constants.simpleEmbed("Overall Quote Statistics",
                    String.format("""
                                    Day of the week with the most quotes: %s
                                    Total number of quotes on %sS: %d

                                    User quoted the most: %s""",
                            day,
                            day,
                            count,
                            mostQuotedMember != null ? mostQuotedMember.getAsMention() : "<@" + mostQuotedUserId + ">"))).queue();
            return;
        }

        if (StringUtils.startsWithIgnoreCase(event.getArgs(), "delete")) {
            final var split = event.getArgs().split("\\s+");
            if (ArrayUtils.getLength(split) != 2) {
                event.replyError("Please specify a quote to delete: `!!quote delete 123`");
                return;
            }

            try {
                final var id = Long.parseLong(split[1]);
                final var optionalQuote = quoteRepository.findById(id);

                if (optionalQuote.isPresent()) {
                    quoteRepository.deleteById(id);
                    philJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("Quote #" + id + " successfully deleted").queue();
                } else {
                    philJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("Quote #" + id + " does not exist").queue();
                }
            } catch (final NumberFormatException e) {
                philJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("Could not parse quote number from: " + split[1]).queue();
            }

            return;
        }

        try {
            final var id = Long.parseLong(event.getArgs());
            final var optionalQuote = quoteRepository.findById(id);

            if (optionalQuote.isEmpty()) {
                philJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("Quote #" + id + " does not exist").queue();
                return;
            }

            respondWithQuote(event, optionalQuote.get());
        } catch (final NumberFormatException e) {
            philJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("Did not recognize number " + event.getArgs()).queue();
        }
    }

    public String getEmoji() {
        return SPEECH_BUBBLE_EMOJI;
    }

    public void saveQuote(final MessageReactionAddEvent event) {
        final var messageId = event.getMessageIdLong();
        final var channelId = event.getChannel().getIdLong();
        final var guildId = event.getGuild().getIdLong();
        final var quoterId = event.getUserIdLong();

        if (!quoteRepository.existsByMessageId(messageId)) {
            philJda.getTextChannelById(channelId).retrieveMessageById(messageId).queue(msg -> {
                String image = null;
                if (CollectionUtils.isNotEmpty(msg.getEmbeds())) {
                    image = msg.getEmbeds().getFirst().getUrl();
                }
                if (CollectionUtils.isNotEmpty(msg.getAttachments())) {
                    image = msg.getAttachments().getFirst().getUrl();
                }

                final var savedQuote = jdbcAggregateTemplate.insert(new Quote(messageId, channelId, msg.getContentRaw(), image,
                        msg.getAuthor().getIdLong(), msg.getTimeCreated().toLocalDateTime()));

                msg.addReaction(Emoji.fromUnicode(SPEECH_BUBBLE_EMOJI)).queue();

                final var msgLink = " [(jump)](https://discordapp.com/channels/" + guildId + '/' + channelId + '/' + messageId + ')';

                final var messageEmbed = Constants.simpleEmbed("Quote #" + savedQuote.getId() + " Added",
                        "<@" + quoterId + "> Added quote #" + savedQuote.getId() + msgLink);

                philJda.getTextChannelById(event.getChannel().getIdLong()).sendMessageEmbeds(messageEmbed).queue();
            });
        }
    }

    private void respondWithQuote(final CommandEvent event, final Quote quote) {
        final var guildId = event.getGuild().getId();
        final var msgLink = "[(jump)](https://discordapp.com/channels/" + guildId + '/' + quote.getChannelId() + '/' + quote.getMessageId() + ')';

        final var description = new StringBuilder()
                .append(quote.getQuote())
                .append("\n- <@")
                .append(quote.getUserId())
                .append("> ")
                .append(msgLink);
        philJda.getTextChannelById(event.getChannel().getIdLong())
                .sendMessageEmbeds(Constants.simpleEmbed("Quote #" + quote.getId(), description.toString(), quote.getImage(),
                        TIMESTAMP_FORMAT.format(quote.getCreated()))).queue();
    }

}
