package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.DailyRiverdaleMemeEntity;
import com.badfic.philbot.data.phil.DailyRiverdaleMemeRepository;
import com.badfic.philbot.listeners.phil.swampy.BaseSwampy;
import com.google.common.collect.Sets;
import com.google.common.html.HtmlEscapers;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class DailyRiverdaleMemeService extends BaseSwampy {

    private static final String SEARCH_STRING = "out of context riverdale meme";

    @Autowired
    private DailyRiverdaleMemeRepository dailyRiverdaleMemeRepository;

    public DailyRiverdaleMemeService() {
        name = "updateDailyRiverdaleMemes";
        ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        threadPoolTaskExecutor.execute(this::runTask);
    }

    private void runTask() {
        List<TextChannel> textChannelsByName = philJda.getTextChannelsByName(Constants.CURSED_SWAMP_CHANNEL, false);
        if (CollectionUtils.isEmpty(textChannelsByName)) {
            honeybadgerReporter.reportError(new RuntimeException("Failed to find " + Constants.CURSED_SWAMP_CHANNEL + " channel"));
            return;
        }

        TextChannel cursedChannel = textChannelsByName.get(0);
        Set<Long> messageIds = new HashSet<>();

        long lastMsgId = 0;
        while (lastMsgId != -1) {
            MessageHistory history;
            if (lastMsgId == 0) {
                history = cursedChannel.getHistoryFromBeginning(100).timeout(30, TimeUnit.SECONDS).complete();
            } else {
                history = cursedChannel.getHistoryAfter(lastMsgId, 100).timeout(30, TimeUnit.SECONDS).complete();
            }

            lastMsgId = CollectionUtils.isNotEmpty(history.getRetrievedHistory())
                    ? history.getRetrievedHistory().get(0).getIdLong()
                    : -1;

            for (Message message : history.getRetrievedHistory()) {
                if (StringUtils.containsIgnoreCase(message.getContentRaw(), SEARCH_STRING)) {
                    if (CollectionUtils.isNotEmpty(message.getAttachments())) {
                        String imageUrl = message.getAttachments().get(0).getUrl();
                        String messageContent = message.getContentRaw();

                        messageIds.add(message.getIdLong());

                        DailyRiverdaleMemeEntity storedMessage;
                        Optional<DailyRiverdaleMemeEntity> storedMessageOpt = dailyRiverdaleMemeRepository.findById(message.getIdLong());

                        if (storedMessageOpt.isPresent()) {
                            storedMessage = storedMessageOpt.get();

                            if (Objects.nonNull(message.getTimeEdited()) &&
                                    !message.getTimeEdited().toLocalDateTime().isEqual(storedMessage.getTimeEdited())) {
                                storedMessage.setMessage(messageContent);
                                storedMessage.setTimeEdited(message.getTimeEdited().toLocalDateTime());
                                storedMessage.setImageUrl(imageUrl);
                                dailyRiverdaleMemeRepository.save(storedMessage);
                            }
                        } else {
                            storedMessage = new DailyRiverdaleMemeEntity(
                                    message.getIdLong(),
                                    messageContent,
                                    imageUrl,
                                    message.getTimeCreated().toLocalDateTime(),
                                    message.getTimeEdited() != null ? message.getTimeEdited().toLocalDateTime() : message.getTimeCreated().toLocalDateTime());

                            dailyRiverdaleMemeRepository.save(storedMessage);
                        }
                    }
                }
            }
        }

        Sets.SetView<Long> deletedMessageIds = Sets.difference(dailyRiverdaleMemeRepository.findAllIds(), messageIds);

        for (Long deletedMessageId : deletedMessageIds) {
            if (dailyRiverdaleMemeRepository.existsById(deletedMessageId)) {
                dailyRiverdaleMemeRepository.deleteById(deletedMessageId);
            }
        }

        Constants.debugToTestChannel(philJda, "Successfully updated daily riverdale memes cache");
    }

    public List<String> getMessages() {
        return dailyRiverdaleMemeRepository.findAll(Sort.by(Sort.Direction.DESC, "timeCreated"))
                .stream()
                .map(entity -> HtmlEscapers.htmlEscaper().escape(entity.getMessage()) + "<br/>\n<img src=\"" + entity.getImageUrl() + "\" class=\"img-fluid\">")
                .collect(Collectors.toList());
    }

}
