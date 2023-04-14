package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.HowWeBecameCursedEntity;
import com.badfic.philbot.data.HowWeBecameCursedRepository;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HowWeBecameCursedService extends BaseService implements DailyTickable {

    private final HowWeBecameCursedRepository howWeBecameCursedRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    public HowWeBecameCursedService(HowWeBecameCursedRepository howWeBecameCursedRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
        this.howWeBecameCursedRepository = howWeBecameCursedRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
    }

    @Override
    public void runDailyTask() {
        Optional<TextChannel> optionalChannel = philJda.getTextChannelsByName("how-we-became-cursed", false)
                .stream()
                .findFirst();

        if (optionalChannel.isEmpty()) {
            log.error("HowWeBecameCursedService Could not find how-we-became-cursed channel");
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
                    jdbcAggregateTemplate.insert(storedMessage);
                }
            }
        }

        Collection<Long> deletedMessageIds = CollectionUtils.disjunction(howWeBecameCursedRepository.findAllIds(), messageIds);

        for (Long deletedMessageId : deletedMessageIds) {
            if (howWeBecameCursedRepository.existsById(deletedMessageId)) {
                howWeBecameCursedRepository.deleteById(deletedMessageId);
            }
        }

        Constants.debugToTestChannel(philJda, "Successfully updated how-we-became-cursed cache");
    }

    public List<String> getMessages() {
        return howWeBecameCursedRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(HowWeBecameCursedEntity::getTimeCreated))
                .map(HowWeBecameCursedEntity::getMessage)
                .collect(Collectors.toList());
    }

    private String getMessageContent(Message message) {
        StringBuilder contentBuilder = new StringBuilder();

        contentBuilder.append("<pre style=\"white-space: pre-wrap;\">\n")
                .append(StringEscapeUtils.escapeHtml4(message.getContentDisplay()))
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
                        "<img alt=\"" + StringEscapeUtils.escapeHtml4(emote.getName()) + "\" src=\"" + imageUrl + "\" width=\"32\" height=\"32\">");
            }
        }

        return content;
    }

}
