package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.HowWeBecameCursedEntity;
import com.badfic.philbot.data.phil.HowWeBecameCursedRepository;
import com.google.common.collect.Sets;
import com.google.common.html.HtmlEscapers;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class HowWeBecameCursedService extends BaseService implements DailyTickable {

    @Autowired
    private HowWeBecameCursedRepository howWeBecameCursedRepository;

    @Override
    public void runDailyTask() {
        Optional<TextChannel> optionalChannel = philJda.getGuildById(baseConfig.guildId).getTextChannelsByName("how-we-became-cursed", false)
                .stream()
                .findFirst();

        if (optionalChannel.isEmpty()) {
            honeybadgerReporter.reportError(new IllegalArgumentException("Could not find how-we-became-cursed channel"));
            return;
        }

        TextChannel channel = optionalChannel.get();
        Set<Long> messageIds = new HashSet<>();

        long lastMsgId = 0;
        while (lastMsgId != -1) {
            MessageHistory history;
            if (lastMsgId == 0) {
                history = channel.getHistoryFromBeginning(100).timeout(30, TimeUnit.SECONDS).complete();
            } else {
                history = channel.getHistoryAfter(lastMsgId, 100).timeout(30, TimeUnit.SECONDS).complete();
            }

            lastMsgId = CollectionUtils.isNotEmpty(history.getRetrievedHistory())
                    ? history.getRetrievedHistory().get(0).getIdLong()
                    : -1;

            for (Message message : history.getRetrievedHistory()) {
                messageIds.add(message.getIdLong());

                HowWeBecameCursedEntity storedMessage;
                Optional<HowWeBecameCursedEntity> storedMessageOpt = howWeBecameCursedRepository.findById(message.getIdLong());

                if (storedMessageOpt.isPresent()) {
                    storedMessage = storedMessageOpt.get();

                    if (Objects.nonNull(message.getTimeEdited()) &&
                            !message.getTimeEdited().toLocalDateTime().isEqual(storedMessage.getTimeEdited())) {
                        String content = getMessageContent(message);

                        storedMessage.setMessage(content);
                        storedMessage.setTimeEdited(message.getTimeEdited().toLocalDateTime());
                        howWeBecameCursedRepository.save(storedMessage);
                    }
                } else {
                    String content = getMessageContent(message);

                    storedMessage = new HowWeBecameCursedEntity(message.getIdLong(), content, message.getTimeCreated().toLocalDateTime(),
                            message.getTimeEdited() != null ? message.getTimeEdited().toLocalDateTime() : message.getTimeCreated().toLocalDateTime());
                    howWeBecameCursedRepository.save(storedMessage);
                }
            }
        }

        Sets.SetView<Long> deletedMessageIds = Sets.difference(howWeBecameCursedRepository.findAllIds(), messageIds);

        for (Long deletedMessageId : deletedMessageIds) {
            if (howWeBecameCursedRepository.existsById(deletedMessageId)) {
                howWeBecameCursedRepository.deleteById(deletedMessageId);
            }
        }

        Constants.debugToTestChannel(philJda, "Successfully updated how-we-became-cursed cache");
    }

    public List<String> getMessages() {
        return howWeBecameCursedRepository.findAll(Sort.by(Sort.Direction.ASC, "timeCreated"))
                .stream()
                .map(HowWeBecameCursedEntity::getMessage)
                .collect(Collectors.toList());
    }

    private String getMessageContent(Message message) {
        StringBuilder contentBuilder = new StringBuilder();

        contentBuilder.append("<pre style=\"white-space: pre-wrap;\">\n")
                .append(HtmlEscapers.htmlEscaper().escape(message.getContentDisplay()))
                .append("\n</pre>\n");

        if (CollectionUtils.isNotEmpty(message.getAttachments())) {
            for (Message.Attachment attachment : message.getAttachments()) {
                if (Constants.urlIsImage(attachment.getUrl())) {
                    contentBuilder.append("\n<img src=\"")
                            .append(attachment.getUrl())
                            .append("\" class=\"img-fluid\">");
                } else {
                    contentBuilder.append("\n<a href=\"")
                            .append(attachment.getUrl())
                            .append("\" target=\"_blank\"/>");
                }
            }
        }

        if (CollectionUtils.isNotEmpty(message.getEmbeds())) {
            for (MessageEmbed embed : message.getEmbeds()) {
                if (embed.getUrl() != null) {
                    if (Constants.urlIsImage(embed.getUrl())) {
                        contentBuilder.append("\n<img src=\"")
                                .append(embed.getUrl())
                                .append("\" class=\"img-fluid\">");
                    } else {
                        contentBuilder.append("\n<a href=\"")
                                .append(embed.getUrl())
                                .append("\" target=\"_blank\"/>");
                    }
                }
                if (embed.getImage() != null) {
                    contentBuilder.append("\n<img src=\"")
                            .append(embed.getImage().getUrl())
                            .append("\" class=\"img-fluid\">");
                }
            }
        }

        String content = contentBuilder.toString();
        if (CollectionUtils.isNotEmpty(message.getMentions().getCustomEmojis())) {
            for (CustomEmoji emote : message.getMentions().getCustomEmojis()) {
                String imageUrl = emote.getImageUrl();
                content = RegExUtils.replaceAll(
                        content,
                        ':' + emote.getName() + ':',
                        "<img alt=\"" + HtmlEscapers.htmlEscaper().escape(emote.getName()) + "\" src=\"" + imageUrl + "\" width=\"32\" height=\"32\">");
            }
        }

        return content;
    }

}
