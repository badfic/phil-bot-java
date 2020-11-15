package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.phil.Quote;
import com.badfic.philbot.data.phil.QuoteRepository;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
public class PopulateOldQuotes extends BaseSwampy implements PhilMarker {

    @Resource
    private QuoteRepository quoteRepository;

    public PopulateOldQuotes() {
        name = "populateOldQuotes";
        ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        final long originalChannelId = event.getChannel().getIdLong();

        new Thread(() -> {
            List<Long> savedQuotes = new ArrayList<>();
            List<TextChannel> textChannels = philJda.getGuilds().get(0).getTextChannels();

            philJda.getTextChannelById(originalChannelId).sendMessage("_sigh_ This is gonna take a while...").queue();

            for (TextChannel textChannel : textChannels) {
                philJda.getTextChannelById(originalChannelId).sendMessage("Processing quotes in channel: " + textChannel.getName() + " ...").queue();

                long lastMsgId = 0;
                while (lastMsgId != -1) {
                    MessageHistory history;
                    if (lastMsgId == 0) {
                        history = textChannel.getHistoryFromBeginning(25).complete();
                    } else {
                        history = textChannel.getHistoryAfter(lastMsgId, 25).complete();
                    }

                    lastMsgId = CollectionUtils.isNotEmpty(history.getRetrievedHistory())
                            ? history.getRetrievedHistory().get(0).getIdLong()
                            : -1;

                    for (Message message : history.getRetrievedHistory()) {
                        List<MessageReaction> reactions = message.getReactions();

                        boolean doAdd = false;
                        for (MessageReaction reaction : reactions) {
                            if (reaction.getReactionEmote().isEmoji() && "\uD83D\uDCAC".equalsIgnoreCase(reaction.getReactionEmote().getEmoji())) {
                                doAdd = true;
                            }
                        }

                        if (doAdd) {
                            long messageId = message.getIdLong();
                            long userId = message.getAuthor().getIdLong();
                            long channelId = textChannel.getIdLong();

                            if (!quoteRepository.existsByMessageId(messageId)) {
                                Quote savedQuote = quoteRepository
                                        .save(new Quote(messageId, channelId, message.getContentRaw(), userId, message.getTimeCreated().toLocalDateTime()));

                                savedQuotes.add(savedQuote.getId());
                            }
                        }
                    }
                }
            }

            philJda.getTextChannelById(originalChannelId).sendMessage("Successfully created " + savedQuotes.size() + " new quotes").queue();
        }).start();
    }
}
