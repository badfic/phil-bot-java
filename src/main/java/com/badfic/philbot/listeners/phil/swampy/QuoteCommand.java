package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.phil.Quote;
import com.badfic.philbot.data.phil.QuoteRepository;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class QuoteCommand extends BaseSwampy implements PhilMarker {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("MMM d yyyy HH:mm");

    @Resource
    private QuoteRepository quoteRepository;

    public QuoteCommand() {
        name = "quote";
        help = "!!quote\n`!!quote` to get a random quote\n`!!quote 23` to get quote number 23";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (StringUtils.isBlank(event.getArgs())) {
            List<Quote> quotes = quoteRepository.findAll();
            Collections.shuffle(quotes);
            Quote quote = pickRandom(quotes);

            respondWithQuote(event, quote);
            return;
        }

        try {
            long id = Long.parseLong(event.getArgs());
            Optional<Quote> optionalQuote = quoteRepository.findById(id);

            if (!optionalQuote.isPresent()) {
                event.replyError("Quote #" + id + " does not exist");
                return;
            }

            respondWithQuote(event, optionalQuote.get());
        } catch (NumberFormatException e) {
            event.replyError("Did not recognize number " + event.getArgs());
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
        event.reply(simpleEmbed("Quote #" + quote.getId(), description.toString(), null, TIMESTAMP_FORMAT.format(quote.getCreated())));
    }

}
