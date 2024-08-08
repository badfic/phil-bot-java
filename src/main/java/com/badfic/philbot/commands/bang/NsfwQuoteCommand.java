package com.badfic.philbot.commands.bang;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.NsfwQuote;
import com.badfic.philbot.data.NsfwQuoteRepository;
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
public class NsfwQuoteCommand extends BaseBangCommand implements DailyTickable {

    private static final String EGGPLANT_EMOJI = "\uD83C\uDF46";

    private final NsfwQuoteRepository nsfwQuoteRepository;

    NsfwQuoteCommand(final NsfwQuoteRepository nsfwQuoteRepository) {
        name = "nsfwQuote";
        aliases = new String[]{"nsfwQuotes", "cursedQuote", "cursedQuotes"};
        nsfwOnly = true;
        help = """
                18+ only
                React to any message with the 🍆 emoji to save an NsfwQuote
                `!!nsfwQuote` to get a random quote
                `!!nsfwQuote 23` to get quote number 23
                `!!nsfwQuote stats` returns statistics about all nsfw quotes
                `!!nsfwQuote stats @Santiago` returns statistics about Santiago
                `!!nsfwQuote cache` runs the meme saver if a channel is configured""";
        this.nsfwQuoteRepository = nsfwQuoteRepository;
    }

    @Override
    public void runDailyTask() {
        final var swampyGamesConfig = getSwampyGamesConfig();
        final var channelId = swampyGamesConfig.getNsfwSavedMemesChannelId();

        if (channelId > 0) {
            final var images = nsfwQuoteRepository.findAllNonNullImages();

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
                discordWebhookSendService.sendMessage(philLookedUpChannel.getIdLong(), swampyGamesConfig.getKeanuNickname(),
                        swampyGamesConfig.getKeanuAvatar(), image);
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
            }

            Constants.debugToTestChannel(philJda, "Successfully updated nsfw saved memes channel");
        } else {
            Constants.debugToTestChannel(philJda, "Could not update nsfw saved memes channel, no channel is configured");
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
            final var ids = nsfwQuoteRepository.findAllIds();

            if (CollectionUtils.isEmpty(ids)) {
                philJda.getTextChannelById(event.getChannel().getIdLong())
                        .sendMessage("Could not find any quotes")
                        .queue();
                return;
            }

            final var idx = Constants.pickRandom(ids);
            final var optionalQuote = nsfwQuoteRepository.findById(idx);

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
                final var quoteDaysOfWeekForUser = nsfwQuoteRepository.getQuoteDaysOfWeekForUser(memberId);
                final var mode = Constants.isoDayOfWeekMode(quoteDaysOfWeekForUser);

                final var day = mode.left();
                final var count = mode.rightInt();

                philJda.getTextChannelById(event.getChannel().getIdLong()).
                        sendMessageEmbeds(Constants.simpleEmbed("User Cursed Quote Statistics",
                                String.format("""
                                                %s Statistics:

                                                Total number of cursed quotes: %d

                                                Day of week with the most cursed quotes: %s
                                                Number of cursed quotes on %sS: %d""",
                                        member.getAsMention(),
                                        quoteDaysOfWeekForUser.length,
                                        day,
                                        day,
                                        count))).queue();
                return;
            }

            final var quoteDaysOfWeek = nsfwQuoteRepository.getQuoteDaysOfWeek();
            final var mode = Constants.isoDayOfWeekMode(quoteDaysOfWeek);

            final var day = mode.left();
            final var count = mode.rightInt();

            final var mostQuotedUserId = nsfwQuoteRepository.getMostQuotedUser();
            final var mostQuotedMember = event.getGuild().getMemberById(mostQuotedUserId);

            philJda.getTextChannelById(event.getChannel().getIdLong()).sendMessageEmbeds(Constants.simpleEmbed("Overall Cursed Quote Statistics",
                    String.format("""
                                    Day of the week with the most cursed quotes: %s
                                    Total number of cursed quotes on %sS: %d

                                    User cursed-quoted the most: %s""",
                            day,
                            day,
                            count,
                            mostQuotedMember != null ? mostQuotedMember.getAsMention() : "<@" + mostQuotedUserId + ">"))).queue();
            return;
        }

        if (StringUtils.startsWithIgnoreCase(event.getArgs(), "delete")) {
            final var split = event.getArgs().split("\\s+");
            if (ArrayUtils.getLength(split) != 2) {
                event.replyError("Please specify a NsfwQuote to delete: `!!nsfwQuote delete 123`");
                return;
            }

            try {
                final var id = Long.parseLong(split[1]);
                final var optionalQuote = nsfwQuoteRepository.findById(id);

                if (optionalQuote.isPresent()) {
                    nsfwQuoteRepository.deleteById(id);
                    philJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("NsfwQuote #" + id + " successfully deleted").queue();
                } else {
                    philJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("NsfwQuote #" + id + " does not exist").queue();
                }
            } catch (final NumberFormatException e) {
                philJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("Could not parse quote number from: " + split[1]).queue();
            }

            return;
        }

        try {
            final var id = Long.parseLong(event.getArgs());
            final var optionalQuote = nsfwQuoteRepository.findById(id);

            if (optionalQuote.isEmpty()) {
                philJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("NsfwQuote #" + id + " does not exist").queue();
                return;
            }

            respondWithQuote(event, optionalQuote.get());
        } catch (final NumberFormatException e) {
            philJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("Did not recognize number " + event.getArgs()).queue();
        }
    }

    public String getEmoji() {
        return EGGPLANT_EMOJI;
    }

    public void saveQuote(final MessageReactionAddEvent event) {
        final var messageId = event.getMessageIdLong();
        final var channelId = event.getChannel().getIdLong();
        final var guildId = event.getGuild().getIdLong();
        final var quoterId = event.getUserIdLong();

        if (!nsfwQuoteRepository.existsByMessageId(messageId)) {
            philJda.getTextChannelById(channelId).retrieveMessageById(messageId).queue(msg -> {
                String image = null;
                if (CollectionUtils.isNotEmpty(msg.getEmbeds())) {
                    image = msg.getEmbeds().getFirst().getUrl();
                }
                if (CollectionUtils.isNotEmpty(msg.getAttachments())) {
                    image = msg.getAttachments().getFirst().getUrl();
                }

                final var savedQuote = jdbcAggregateTemplate.insert(new NsfwQuote(messageId, channelId, msg.getContentRaw(), image,
                        msg.getAuthor().getIdLong(), msg.getTimeCreated().toLocalDateTime()));

                msg.addReaction(Emoji.fromUnicode(EGGPLANT_EMOJI)).queue();

                final var msgLink = " [(jump)](https://discordapp.com/channels/" + guildId + '/' + channelId + '/' + messageId + ')';

                final var messageEmbed = Constants.simpleEmbed("NsfwQuote #" + savedQuote.getId() + " Added",
                        "<@" + quoterId + "> Added NsfwQuote #" + savedQuote.getId() + msgLink);

                philJda.getTextChannelById(channelId).sendMessageEmbeds(messageEmbed).queue();
            });
        }
    }

    private void respondWithQuote(final CommandEvent event, final NsfwQuote quote) {
        final var guildId = event.getGuild().getId();
        final var msgLink = "[(jump)](https://discordapp.com/channels/" + guildId + '/' + quote.getChannelId() + '/' + quote.getMessageId() + ')';

        final var description = new StringBuilder()
                .append(quote.getQuote())
                .append("\n- <@")
                .append(quote.getUserId())
                .append("> ")
                .append(msgLink);
        philJda.getTextChannelById(event.getChannel().getIdLong())
                .sendMessageEmbeds(Constants.simpleEmbed("NsfwQuote #" + quote.getId(), description.toString(), quote.getImage(),
                        TIMESTAMP_FORMAT.format(quote.getCreated()))).queue();
    }

}
