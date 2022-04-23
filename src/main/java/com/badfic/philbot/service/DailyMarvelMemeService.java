package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.DailyMarvelMemeEntity;
import com.badfic.philbot.data.phil.DailyMarvelMemeRepository;
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
import javax.annotation.Resource;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class DailyMarvelMemeService extends BaseSwampy implements DailyTickable {

    private static final String SEARCH_STRING = "Out of context Marvel meme";

    @Resource
    private DailyMarvelMemeRepository dailyMarvelMemeRepository;

    public DailyMarvelMemeService() {
        name = "updateDailyMarvelMemes";
        ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        userTriggeredTasksExecutor.execute(this::runDailyTask);
    }

    @Override
    public void runDailyTask() {
        List<TextChannel> textChannelsByName = philJda.getTextChannelsByName(Constants.MEMES_CHANNEL, false);
        if (CollectionUtils.isEmpty(textChannelsByName)) {
            honeybadgerReporter.reportError(new RuntimeException("Failed to find " + Constants.MEMES_CHANNEL + " channel"));
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

                        DailyMarvelMemeEntity storedMessage;
                        Optional<DailyMarvelMemeEntity> storedMessageOpt = dailyMarvelMemeRepository.findById(message.getIdLong());

                        if (storedMessageOpt.isPresent()) {
                            storedMessage = storedMessageOpt.get();

                            if (Objects.nonNull(message.getTimeEdited()) &&
                                    !message.getTimeEdited().toLocalDateTime().isEqual(storedMessage.getTimeEdited())) {
                                storedMessage.setMessage(messageContent);
                                storedMessage.setTimeEdited(message.getTimeEdited().toLocalDateTime());
                                storedMessage.setImageUrl(imageUrl);
                                dailyMarvelMemeRepository.save(storedMessage);
                            }
                        } else {
                            storedMessage = new DailyMarvelMemeEntity(
                                    message.getIdLong(),
                                    messageContent,
                                    imageUrl,
                                    message.getTimeCreated().toLocalDateTime(),
                                    message.getTimeEdited() != null ? message.getTimeEdited().toLocalDateTime() : message.getTimeCreated().toLocalDateTime());

                            dailyMarvelMemeRepository.save(storedMessage);
                        }
                    }
                }
            }
        }

        Sets.SetView<Long> deletedMessageIds = Sets.difference(dailyMarvelMemeRepository.findAllIds(), messageIds);

        for (Long deletedMessageId : deletedMessageIds) {
            if (dailyMarvelMemeRepository.existsById(deletedMessageId)) {
                dailyMarvelMemeRepository.deleteById(deletedMessageId);
            }
        }

        Constants.debugToTestChannel(philJda, "Successfully updated daily marvel memes cache");
    }

    public List<String> getMessages() {
        return dailyMarvelMemeRepository.findAll(Sort.by(Sort.Direction.DESC, "timeCreated"))
                .stream()
                .map(entity -> HtmlEscapers.htmlEscaper().escape(entity.getMessage()) + "<br/>\n<img src=\"" + entity.getImageUrl() + "\" class=\"img-fluid\">")
                .collect(Collectors.toList());
    }

}
