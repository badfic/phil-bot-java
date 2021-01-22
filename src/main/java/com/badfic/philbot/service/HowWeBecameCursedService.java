package com.badfic.philbot.service;

import com.badfic.philbot.data.phil.HowWeBecameCursedEntity;
import com.badfic.philbot.data.phil.HowWeBecameCursedRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class HowWeBecameCursedService extends BaseService implements MinuteTickable {

    private static final int MINUTE_REFRESH_INTERVAL = 60;
    private static final AtomicInteger REFRESH_COUNT = new AtomicInteger(-1);

    @Resource
    private HowWeBecameCursedRepository howWeBecameCursedRepository;

    @Override
    public void tick() throws Exception {
        if (REFRESH_COUNT.compareAndSet(-1, 0) || REFRESH_COUNT.compareAndSet(MINUTE_REFRESH_INTERVAL, 0)) {
            threadPoolTaskExecutor.submit(this::refresh);
            return;
        }

        REFRESH_COUNT.incrementAndGet();
    }

    public List<String> getMessages() {
        return howWeBecameCursedRepository.findAll(Sort.by(Sort.Direction.ASC, "timeCreated"))
                .stream()
                .map(HowWeBecameCursedEntity::getMessage)
                .collect(Collectors.toList());
    }

    private void refresh() {
        Optional<TextChannel> optionalChannel = philJda.getGuilds().get(0).getTextChannelsByName("how-we-became-cursed", false).stream().findFirst();

        if (!optionalChannel.isPresent()) {
            honeybadgerReporter.reportError(new IllegalArgumentException("Could not find how-we-became-cursed channel"));
            return;
        }

        TextChannel channel = optionalChannel.get();

        long lastMsgId = 0;
        while (lastMsgId != -1) {
            MessageHistory history;
            if (lastMsgId == 0) {
                history = channel.getHistoryFromBeginning(25).complete();
            } else {
                history = channel.getHistoryAfter(lastMsgId, 25).complete();
            }

            lastMsgId = CollectionUtils.isNotEmpty(history.getRetrievedHistory())
                    ? history.getRetrievedHistory().get(0).getIdLong()
                    : -1;

            for (Message message : history.getRetrievedHistory()) {
                String content = message.getContentDisplay();
                if (CollectionUtils.isNotEmpty(message.getAttachments())) {
                    for (Message.Attachment attachment : message.getAttachments()) {
                        if (attachment.getUrl() != null) {
                            content += '\n' + attachment.getUrl();
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(message.getEmbeds())) {
                    for (MessageEmbed embed : message.getEmbeds()) {
                        if (embed.getUrl() != null) {
                            content += '\n' + embed.getUrl();
                        }
                    }
                }

                HowWeBecameCursedEntity storedMessage;
                Optional<HowWeBecameCursedEntity> storedMessageOpt = howWeBecameCursedRepository.findById(message.getIdLong());

                if (storedMessageOpt.isPresent()) {
                    storedMessage = storedMessageOpt.get();
                } else {
                    storedMessage = new HowWeBecameCursedEntity(message.getIdLong(), content, message.getTimeCreated().toLocalDateTime(),
                            message.getTimeEdited() != null ? message.getTimeEdited().toLocalDateTime() : message.getTimeCreated().toLocalDateTime());
                    storedMessage = howWeBecameCursedRepository.save(storedMessage);
                }

                if (Objects.nonNull(message.getTimeEdited()) &&
                        !message.getTimeEdited().toLocalDateTime().isEqual(storedMessage.getTimeEdited())) {
                    storedMessage.setMessage(content);
                    storedMessage.setTimeEdited(message.getTimeEdited().toLocalDateTime());
                    storedMessage = howWeBecameCursedRepository.save(storedMessage);
                }
            }
        }
    }

}
