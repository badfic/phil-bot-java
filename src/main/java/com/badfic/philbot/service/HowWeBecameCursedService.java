package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.HowWeBecameCursedEntity;
import com.badfic.philbot.data.HowWeBecameCursedRepository;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class HowWeBecameCursedService extends BaseService implements DailyTickable {

    private final HowWeBecameCursedRepository howWeBecameCursedRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    @Override
    public void runDailyTask() {
        final var optionalChannel = philJda.getTextChannelsByName("how-we-became-cursed", false)
                .stream()
                .findFirst();

        if (optionalChannel.isEmpty()) {
            log.error("HowWeBecameCursedService Could not find how-we-became-cursed channel");
            return;
        }

        final var channel = optionalChannel.get();
        final var messageIds = new HashSet<Long>();

        var lastMsgId = 0L;
        while (lastMsgId != -1) {
            final MessageHistory history;
            if (lastMsgId == 0) {
                history = channel.getHistoryFromBeginning(100).timeout(30, TimeUnit.SECONDS).complete();
            } else {
                history = channel.getHistoryAfter(lastMsgId, 100).timeout(30, TimeUnit.SECONDS).complete();
            }

            final var historyList = history.getRetrievedHistory();
            lastMsgId = CollectionUtils.isNotEmpty(historyList)
                    ? historyList.getFirst().getIdLong()
                    : -1;

            for (final var message : historyList) {
                messageIds.add(message.getIdLong());

                final var storedMessageOpt = howWeBecameCursedRepository.findById(message.getIdLong());

                final HowWeBecameCursedEntity storedMessage;
                if (storedMessageOpt.isPresent()) {
                    storedMessage = storedMessageOpt.get();

                    final var content = getMessageContent(message);
                    storedMessage.setMessage(content);

                    if (Objects.nonNull(message.getTimeEdited()) &&
                            !message.getTimeEdited().toLocalDateTime().isEqual(storedMessage.getTimeEdited())) {
                        storedMessage.setTimeEdited(message.getTimeEdited().toLocalDateTime());
                    }

                    howWeBecameCursedRepository.save(storedMessage);
                } else {
                    final var content = getMessageContent(message);

                    storedMessage = new HowWeBecameCursedEntity(message.getIdLong(), content, message.getTimeCreated().toLocalDateTime(),
                            message.getTimeEdited() != null ? message.getTimeEdited().toLocalDateTime() : message.getTimeCreated().toLocalDateTime());
                    jdbcAggregateTemplate.insert(storedMessage);
                }
            }
        }

        final var deletedMessageIds = CollectionUtils.disjunction(howWeBecameCursedRepository.findAllIds(), messageIds);

        for (final var deletedMessageId : deletedMessageIds) {
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
                .toList();
    }

    private String getMessageContent(final Message message) {
        final var contentBuilder = new StringBuilder();

        contentBuilder.append("<pre style=\"white-space: pre-wrap;\">\n")
                .append(StringEscapeUtils.escapeHtml4(message.getContentDisplay()))
                .append("\n</pre>\n");

        if (CollectionUtils.isNotEmpty(message.getAttachments())) {
            for (final var attachment : message.getAttachments()) {
                if (Constants.urlIsImage(attachment.getProxyUrl())) {
                    contentBuilder.append("\n<img src=\"")
                            .append(attachment.getProxyUrl())
                            .append("\" class=\"img-fluid\">");
                } else {
                    contentBuilder.append("\n<a href=\"")
                            .append(attachment.getProxyUrl())
                            .append("\" target=\"_blank\"/>");
                }
            }
        }

        if (CollectionUtils.isNotEmpty(message.getEmbeds())) {
            for (final var embed : message.getEmbeds()) {
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
                            .append(embed.getImage().getProxyUrl())
                            .append("\" class=\"img-fluid\">");
                }
            }
        }

        var content = contentBuilder.toString();
        if (CollectionUtils.isNotEmpty(message.getMentions().getCustomEmojis())) {
            for (final var emote : message.getMentions().getCustomEmojis()) {
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
