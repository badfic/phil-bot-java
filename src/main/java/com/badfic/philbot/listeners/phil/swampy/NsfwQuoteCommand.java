package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.NsfwQuote;
import com.badfic.philbot.data.phil.NsfwQuoteRepository;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.badfic.philbot.service.DailyTickable;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.time.DayOfWeek;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
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
public class NsfwQuoteCommand extends BaseSwampy implements DailyTickable {

    private static final String EGGPLANT_EMOJI = "\uD83C\uDF46";

    @Resource(name = "keanuJda")
    @Lazy
    private JDA keanuJda;

    @Resource
    private NsfwQuoteRepository nsfwQuoteRepository;

    public NsfwQuoteCommand() {
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
    }

    @Override
    public void runDailyTask() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        long channelId = swampyGamesConfig.getNsfwSavedMemesChannelId();

        if (channelId > 0) {
            Set<String> images = nsfwQuoteRepository.findAllNonNullImages();

            TextChannel philLookedUpChannel = philJda.getTextChannelById(channelId);
            TextChannel keanuLookedUpChannel = keanuJda.getTextChannelById(channelId);

            if (philLookedUpChannel == null || keanuLookedUpChannel == null) {
                Constants.debugToTestChannel(keanuJda, "Could not update nsfw saved memes channel, it does not exist");
                return;
            }

            long lastMsgId = 0;
            while (lastMsgId != -1) {
                MessageHistory history;
                if (lastMsgId == 0) {
                    history = philLookedUpChannel.getHistoryFromBeginning(100).complete();
                } else {
                    history = philLookedUpChannel.getHistoryAfter(lastMsgId, 100).complete();
                }

                lastMsgId = CollectionUtils.isNotEmpty(history.getRetrievedHistory())
                        ? history.getRetrievedHistory().get(0).getIdLong()
                        : -1;

                for (Message message : history.getRetrievedHistory()) {
                    images.remove(message.getContentRaw());
                }
            }

            for (String image : images) {
                keanuLookedUpChannel.sendMessage(image).queue();
            }

            Constants.debugToTestChannel(keanuJda, "Successfully updated nsfw saved memes channel");
        } else {
            Constants.debugToTestChannel(keanuJda, "Could not update nsfw saved memes channel, no channel is configured");
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        if (StringUtils.startsWithIgnoreCase(event.getArgs(), "cache")) {
            if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
                event.replyError("You do not have permission to execute this command");
                return;
            }

            threadPoolTaskExecutor.submit(this::runDailyTask);
            return;
        }

        if (StringUtils.isBlank(event.getArgs())) {
            int count = (int) nsfwQuoteRepository.count();

            if (count < 1) {
                keanuJda.getTextChannelById(event.getChannel().getIdLong())
                        .sendMessage("Could not find any nsfwQuotes")
                        .queue();
                return;
            }

            int idx = ThreadLocalRandom.current().nextInt(0, count);
            Page<NsfwQuote> quotes = nsfwQuoteRepository.findAll(PageRequest.of(idx, 1));
            NsfwQuote quote = quotes.getContent().get(0);

            respondWithQuote(event, quote);
            return;
        }

        if (StringUtils.startsWithIgnoreCase(event.getArgs(), "stat")) {
            if (CollectionUtils.isNotEmpty(event.getMessage().getMentionedMembers())) {
                Member member = event.getMessage().getMentionedMembers().get(0);
                long memberId = member.getIdLong();
                int[] quoteDaysOfWeekForUser = nsfwQuoteRepository.getQuoteDaysOfWeekForUser(memberId);
                Pair<DayOfWeek, Integer> mode = Constants.isoDayOfWeekMode(quoteDaysOfWeekForUser);

                DayOfWeek day = mode.getLeft();
                int count = mode.getRight();

                keanuJda.getTextChannelById(event.getChannel().getIdLong()).
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

            keanuJda.getTextChannelById(event.getChannel().getIdLong()).sendMessageEmbeds(Constants.simpleEmbed("Overall Cursed Quote Statistics",
                    String.format("""
                                    Day of the week with the most cursed quotes: %s
                                    Total number of cursed quotes on %sS: %d

                                    User cursed-quoted the most: %s""",
                            day,
                            day,
                            count,
                            mostQuotedMember != null ? mostQuotedMember.getAsMention() : "<@!" + mostQuotedUserId + ">"))).queue();
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
                    keanuJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("NsfwQuote #" + id + " successfully deleted").queue();
                } else {
                    keanuJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("NsfwQuote #" + id + " does not exist").queue();
                }
            } catch (NumberFormatException e) {
                keanuJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("Could not parse quote number from: " + split[1]).queue();
            }

            return;
        }

        try {
            long id = Long.parseLong(event.getArgs());
            Optional<NsfwQuote> optionalQuote = nsfwQuoteRepository.findById(id);

            if (optionalQuote.isEmpty()) {
                keanuJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("NsfwQuote #" + id + " does not exist").queue();
                return;
            }

            respondWithQuote(event, optionalQuote.get());
        } catch (NumberFormatException e) {
            keanuJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage("Did not recognize number " + event.getArgs()).queue();
        }
    }

    public String getEmoji() {
        return EGGPLANT_EMOJI;
    }

    public void saveQuote(GenericGuildMessageReactionEvent event) {
        long messageId = event.getMessageIdLong();
        long channelId = event.getChannel().getIdLong();
        long guildId = event.getGuild().getIdLong();
        long quoterId = event.getUserIdLong();

        if (!nsfwQuoteRepository.existsByMessageId(messageId)) {
            keanuJda.getTextChannelById(channelId).retrieveMessageById(messageId).queue(msg -> {
                String image = null;
                if (CollectionUtils.isNotEmpty(msg.getEmbeds())) {
                    image = msg.getEmbeds().get(0).getUrl();
                }
                if (CollectionUtils.isNotEmpty(msg.getAttachments())) {
                    image = msg.getAttachments().get(0).getUrl();
                }

                NsfwQuote savedQuote = nsfwQuoteRepository.save(new NsfwQuote(messageId, channelId, msg.getContentRaw(), image,
                        msg.getAuthor().getIdLong(), msg.getTimeCreated().toLocalDateTime()));

                msg.addReaction(EGGPLANT_EMOJI).queue();

                String msgLink = " [(jump)](https://discordapp.com/channels/" + guildId + '/' + channelId + '/' + messageId + ')';

                MessageEmbed messageEmbed = Constants.simpleEmbed("NsfwQuote #" + savedQuote.getId() + " Added",
                        "<@!" + quoterId + "> Added NsfwQuote #" + savedQuote.getId() + msgLink);

                keanuJda.getTextChannelById(channelId).sendMessageEmbeds(messageEmbed).queue();
            });
        }
    }

    private void respondWithQuote(CommandEvent event, NsfwQuote quote) {
        String guildId = event.getGuild().getId();
        String msgLink = "[(jump)](https://discordapp.com/channels/" + guildId + '/' + quote.getChannelId() + '/' + quote.getMessageId() + ')';

        StringBuilder description = new StringBuilder()
                .append(quote.getQuote())
                .append("\n- <@!")
                .append(quote.getUserId())
                .append("> ")
                .append(msgLink);
        keanuJda.getTextChannelById(event.getChannel().getIdLong())
                .sendMessageEmbeds(Constants.simpleEmbed("NsfwQuote #" + quote.getId(), description.toString(), quote.getImage(),
                        TIMESTAMP_FORMAT.format(quote.getCreated()))).queue();
    }

}
