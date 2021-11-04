package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.data.phil.NsfwQuote;
import com.badfic.philbot.data.phil.NsfwQuoteRepository;
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
public class NsfwQuoteLoader extends BaseSwampy {

    @Resource
    private NsfwQuoteRepository nsfwQuoteRepository;

    public NsfwQuoteLoader() {
        name = "NsfwQuoteLoader";
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
                if (!textChannel.isNSFW()) {
                    philJda.getTextChannelById(originalChannelId)
                            .sendMessage("Skipping NsfwQuotes in channel: " + textChannel.getName() + " because it is not an nsfw channel").queue();
                    continue;
                }

                philJda.getTextChannelById(originalChannelId).sendMessage("Processing NsfwQuotes in channel: " + textChannel.getName() + " ...").queue();

                long lastMsgId = 0;
                while (lastMsgId != -1) {
                    MessageHistory history;
                    if (lastMsgId == 0) {
                        history = textChannel.getHistoryFromBeginning(100).complete();
                    } else {
                        history = textChannel.getHistoryAfter(lastMsgId, 100).complete();
                    }

                    lastMsgId = CollectionUtils.isNotEmpty(history.getRetrievedHistory())
                            ? history.getRetrievedHistory().get(0).getIdLong()
                            : -1;

                    for (Message message : history.getRetrievedHistory()) {
                        List<MessageReaction> reactions = message.getReactions();

                        boolean doAdd = false;
                        for (MessageReaction reaction : reactions) {
                            if (reaction.getReactionEmote().isEmoji() && "\uD83C\uDF46".equalsIgnoreCase(reaction.getReactionEmote().getEmoji())) {
                                doAdd = true;
                            }
                        }

                        if (doAdd) {
                            long messageId = message.getIdLong();
                            long userId = message.getAuthor().getIdLong();
                            long channelId = textChannel.getIdLong();
                            String image = null;
                            if (CollectionUtils.isNotEmpty(message.getEmbeds())) {
                                image = message.getEmbeds().get(0).getUrl();
                            }
                            if (CollectionUtils.isNotEmpty(message.getAttachments())) {
                                image = message.getAttachments().get(0).getUrl();
                            }

                            if (!nsfwQuoteRepository.existsByMessageId(messageId)) {
                                NsfwQuote savedQuote = nsfwQuoteRepository.save(new NsfwQuote(
                                        messageId, channelId, message.getContentRaw(), image, userId, message.getTimeCreated().toLocalDateTime()));

                                savedQuotes.add(savedQuote.getId());
                            }
                        }
                    }
                }
            }

            philJda.getTextChannelById(originalChannelId).sendMessage("Successfully created " + savedQuotes.size() + " new NsfwQuotes").queue();
        }).start();
    }

}
