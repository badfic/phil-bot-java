package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.Quote;
import com.badfic.philbot.data.phil.QuoteRepository;
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
public class QuoteCommand extends BaseSwampy {

    @Resource(name = "johnJda")
    @Lazy
    private JDA johnJda;

    @Resource
    private QuoteRepository quoteRepository;

    public QuoteCommand() {
        name = "quote";
        help = """
                React to any message with the \uD83D\uDCAC emoji to save a Quote
                `!!quote` to get a random quote
                `!!quote 23` to get quote number 23""";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (StringUtils.isBlank(event.getArgs())) {
            int count = (int) quoteRepository.count();
            int idx = ThreadLocalRandom.current().nextInt(0, count);
            Page<Quote> quotes = quoteRepository.findAll(PageRequest.of(idx, 1));
            Quote quote = quotes.getContent().get(0);

            respondWithQuote(event, quote);
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
                .sendMessage(Constants.simpleEmbed("Quote #" + quote.getId(), description.toString(), quote.getImage(),
                        TIMESTAMP_FORMAT.format(quote.getCreated()))).queue();
    }

}
