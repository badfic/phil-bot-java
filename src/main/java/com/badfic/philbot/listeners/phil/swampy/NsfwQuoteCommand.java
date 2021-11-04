package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.NsfwQuote;
import com.badfic.philbot.data.phil.NsfwQuoteRepository;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class NsfwQuoteCommand extends BaseSwampy {

    @Resource(name = "keanuJda")
    @Lazy
    private JDA keanuJda;

    @Resource
    private NsfwQuoteRepository nsfwQuoteRepository;

    public NsfwQuoteCommand() {
        name = "nsfwQuote";
        help = """
                18+ only
                React to any message with the 🍆 emoji to save an NsfwQuote
                `!!nsfwQuote` to get a random quote
                `!!nsfwQuote 23` to get quote number 23""";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (!event.getTextChannel().isNSFW()) {
            event.replyError("nsfwQuote can only be called in an nsfw channel");
            return;
        }

        if (StringUtils.isBlank(event.getArgs())) {
            int count = (int) nsfwQuoteRepository.count();

            if (count < 1) {
                keanuJda.getTextChannelById(event.getChannel().getIdLong())
                        .sendMessage("Could not find any NsfwQuotes")
                        .queue();
                return;
            }

            int idx = ThreadLocalRandom.current().nextInt(0, count);
            Page<NsfwQuote> quotes = nsfwQuoteRepository.findAll(PageRequest.of(idx, 1));
            NsfwQuote quote = quotes.getContent().get(0);

            respondWithQuote(event, quote);
            return;
        }

        if (StringUtils.startsWithIgnoreCase(event.getArgs(), "delete")) {
            String[] split = event.getArgs().split("\\s+");
            if (ArrayUtils.getLength(split) != 2) {
                event.replyError("Please specify a NsfwQuote to delete: `!!NsfwQuote delete 123`");
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
