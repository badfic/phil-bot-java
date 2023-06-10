package com.badfic.philbot.commands;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.NsfwQuote;
import com.badfic.philbot.data.NsfwQuoteRepository;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.service.DailyTickable;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

@Component
public class NsfwQuoteCommand extends BaseNormalCommand implements DailyTickable {

    private static final String EGGPLANT_EMOJI = "\uD83C\uDF46";

    private final NsfwQuoteRepository nsfwQuoteRepository;

    public NsfwQuoteCommand(NsfwQuoteRepository nsfwQuoteRepository) {
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
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        long channelId = swampyGamesConfig.getNsfwSavedMemesChannelId();

        if (channelId > 0) {
            Set<String> images = nsfwQuoteRepository.findAllNonNullImages();

            TextChannel philLookedUpChannel = philJda.getTextChannelById(channelId);

            if (philLookedUpChannel == null) {
                Constants.debugToTestChannel(philJda, "Could not update nsfw saved memes channel, it does not exist");
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
    public void execute(CommandEvent event) {
        if (StringUtils.startsWithIgnoreCase(event.getArgs(), "cache")) {
            if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
                event.replyError("You do not have permission to execute this command");
                return;
            }

            applicationTaskExecutor.execute(this::runDailyTask);
            return;
        }

        if (StringUtils.isBlank(event.getArgs())) {
            List<Long> ids = nsfwQuoteRepository.findAllIds();

            if (CollectionUtils.isEmpty(ids)) {
                philJda.getTextChannelById(event.getChannel().getIdLong())
                        .sendMessage("Could not find any quotes")
                        .queue();
                return;
            }

            long idx = Constants.pickRandom(ids);
            Optional<NsfwQuote> optionalQuote = nsfwQuoteRepository.findById(idx);

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
                Member member = event.getMessage().getMentions().getMembers().get(0);
                long memberId = member.getIdLong();
                int[] quoteDaysOfWeekForUser = nsfwQuoteRepository.getQuoteDaysOfWeekForUser(memberId);
                Pair<DayOfWeek, Integer> mode = Constants.isoDayOfWeekMode(quoteDaysOfWeekForUser);

                DayOfWeek day = mode.getLeft();
                int count = mode.getRight();

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

            int[] quoteDaysOfWeek = nsfwQuoteRepository.getQuoteDaysOfWeek();
            Pair<DayOfWeek, Integer> mode = Constants.isoDayOfWeekMode(quoteDaysOfWeek);

            DayOfWeek day = mode.getLeft();
            int count = mode.getRight();

            long mostQuotedUserId = nsfwQuoteRepository.getMostQuotedUser();
            Member mostQuotedMember = event.getGuild().getMemberById(mostQuotedUserId);

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
            String[] split = event.getArgs().split("\\s+");
            if (ArrayUtils.getLength(split) != 2) {
                event.replyError("Please specify a NsfwQuote to delete: `!!nsfwQuote delete 123`");
                return;
            }

            try {
                long id = Long.parseLong(split[1]);
                Optional<NsfwQuote> optionalQuote = nsfwQuoteRepository.findById(id);

                if (optionalQuote.isPresent()) {
                    nsfwQuoteRepository.deleteById(id);
                    philJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("NsfwQuote #" + id + " successfully deleted").queue();
                } else {
                    philJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("NsfwQuote #" + id + " does not exist").queue();
                }
            } catch (NumberFormatException e) {
                philJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("Could not parse quote number from: " + split[1]).queue();
            }

            return;
        }

        try {
            long id = Long.parseLong(event.getArgs());
            Optional<NsfwQuote> optionalQuote = nsfwQuoteRepository.findById(id);

            if (optionalQuote.isEmpty()) {
                philJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("NsfwQuote #" + id + " does not exist").queue();
                return;
            }

            respondWithQuote(event, optionalQuote.get());
        } catch (NumberFormatException e) {
            philJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("Did not recognize number " + event.getArgs()).queue();
        }
    }

    public String getEmoji() {
        return EGGPLANT_EMOJI;
    }

    public void saveQuote(MessageReactionAddEvent event) {
        long messageId = event.getMessageIdLong();
        long channelId = event.getChannel().getIdLong();
        long guildId = event.getGuild().getIdLong();
        long quoterId = event.getUserIdLong();

        if (!nsfwQuoteRepository.existsByMessageId(messageId)) {
            philJda.getTextChannelById(channelId).retrieveMessageById(messageId).queue(msg -> {
                String image = null;
                if (CollectionUtils.isNotEmpty(msg.getEmbeds())) {
                    image = msg.getEmbeds().get(0).getUrl();
                }
                if (CollectionUtils.isNotEmpty(msg.getAttachments())) {
                    image = msg.getAttachments().get(0).getUrl();
                }

                NsfwQuote savedQuote = jdbcAggregateTemplate.insert(new NsfwQuote(messageId, channelId, msg.getContentRaw(), image,
                        msg.getAuthor().getIdLong(), msg.getTimeCreated().toLocalDateTime()));

                msg.addReaction(Emoji.fromUnicode(EGGPLANT_EMOJI)).queue();

                String msgLink = " [(jump)](https://discordapp.com/channels/" + guildId + '/' + channelId + '/' + messageId + ')';

                MessageEmbed messageEmbed = Constants.simpleEmbed("NsfwQuote #" + savedQuote.getId() + " Added",
                        "<@" + quoterId + "> Added NsfwQuote #" + savedQuote.getId() + msgLink);

                philJda.getTextChannelById(channelId).sendMessageEmbeds(messageEmbed).queue();
            });
        }
    }

    private void respondWithQuote(CommandEvent event, NsfwQuote quote) {
        String guildId = event.getGuild().getId();
        String msgLink = "[(jump)](https://discordapp.com/channels/" + guildId + '/' + quote.getChannelId() + '/' + quote.getMessageId() + ')';

        StringBuilder description = new StringBuilder()
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
