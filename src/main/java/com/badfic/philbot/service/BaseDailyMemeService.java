package com.badfic.philbot.service;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.bang.BaseBangCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.BaseDailyMemeEntity;
import com.badfic.philbot.data.DailyMemeRepository;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
public abstract class BaseDailyMemeService extends BaseBangCommand {

    private final DailyMemeRepository<? extends BaseDailyMemeEntity> dailyMemeRepository;
    private final String searchString;
    private final String channelName;

    public BaseDailyMemeService(final DailyMemeRepository<? extends BaseDailyMemeEntity> dailyMemeRepository, final String name, final String searchString,
                                final String channelname) {
        this.dailyMemeRepository = dailyMemeRepository;
        this.searchString = searchString;
        this.channelName = channelname;
        this.name = name;
        this.ownerCommand = true;
    }

    @PostConstruct
    public void init() {
        taskScheduler.scheduleWithFixedDelay(() -> {
            Thread.startVirtualThread(this::refreshImageUrls);
        }, Instant.now().plus(15, ChronoUnit.MINUTES), Duration.ofMinutes(15));
    }

    @Override
    public void execute(final CommandEvent event) {
        executorService.execute(() -> {
            if (event.getArgs().contains("refresh")) {
                refreshImageUrls();
            } else {
                scrapeAllMemes();
            }
        });
    }

    public List<String> getMessages() {
        return dailyMemeRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(BaseDailyMemeEntity::getTimeCreated))
                .map(entity -> StringEscapeUtils.escapeHtml4(entity.getMessage()) + "<br/>\n<img src=\"" + entity.getImageUrl() + "\" class=\"img-fluid\">")
                .toList();
    }

    private void refreshImageUrls() {
        final var textChannelsByName = philJda.getTextChannelsByName(channelName, false);
        if (CollectionUtils.isEmpty(textChannelsByName)) {
            log.error("{} Failed to find [channel={}]", name, channelName);
            return;
        }
        final var channel = textChannelsByName.getFirst();

        final var memeEntities = dailyMemeRepository.findAll();
        final var messageIds = new LongArrayList();
        for (final var memeEntity : memeEntities) {
            final var imageUrl = memeEntity.getImageUrl();
            if (StringUtils.startsWithIgnoreCase(imageUrl, "https://media.discordapp.net/")
                    || StringUtils.startsWithIgnoreCase(imageUrl, "https://cdn.discordapp.com/")) {
                final var builtUri = UriComponentsBuilder.fromHttpUrl(imageUrl).build();
                final var expirationHex = builtUri.getQueryParams().getFirst("ex");

                if (expirationHex != null) {
                    final var expiration = Long.parseLong(expirationHex, 16);
                    final var now = Instant.now().getEpochSecond();

                    if (expiration <= now) {
                        messageIds.add(memeEntity.getMessageId());
                    }
                }
            }
        }

        for (final var messageId : messageIds) {
            channel.retrieveMessageById(messageId).queue(message -> {
                if (CollectionUtils.isNotEmpty(message.getEmbeds())) {
                    message.getEmbeds()
                            .stream()
                            .findFirst()
                            .flatMap(embed -> Optional.ofNullable(embed.getImage()))
                            .map(MessageEmbed.ImageInfo::getProxyUrl)
                            .ifPresent(imageUrl -> {
                                dailyMemeRepository.findById(messageId).ifPresent(entity -> {
                                    entity.setImageUrl(imageUrl);
                                    jdbcAggregateTemplate.update(entity);
                                });
                            });
                } else if (CollectionUtils.isNotEmpty(message.getAttachments())) {
                    message.getAttachments()
                            .stream()
                            .findFirst()
                            .map(Message.Attachment::getProxyUrl)
                            .ifPresent(imageUrl -> {
                                dailyMemeRepository.findById(messageId).ifPresent(entity -> {
                                    entity.setImageUrl(imageUrl);
                                    jdbcAggregateTemplate.update(entity);
                                });
                            });
                }
            });
        }

        log.info("Successfully refreshed {} image URLs for {}", messageIds.size(), name);
    }

    protected void scrapeAllMemes() {
        final var textChannelsByName = philJda.getTextChannelsByName(channelName, false);
        if (CollectionUtils.isEmpty(textChannelsByName)) {
            log.error("{} Failed to find [channel={}]", name, channelName);
            return;
        }

        final var channel = textChannelsByName.getFirst();
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
                if (StringUtils.containsIgnoreCase(message.getContentRaw(), searchString)) {
                    if (CollectionUtils.isNotEmpty(message.getAttachments())) {
                        final var imageUrl = message.getAttachments().getFirst().getProxyUrl();
                        final var messageContent = message.getContentRaw();

                        messageIds.add(message.getIdLong());

                        final var storedMessageOpt = dailyMemeRepository.findById(message.getIdLong());

                        final BaseDailyMemeEntity storedMessage;
                        if (storedMessageOpt.isPresent()) {
                            storedMessage = storedMessageOpt.get();
                            storedMessage.setImageUrl(imageUrl);

                            if (Objects.nonNull(message.getTimeEdited()) &&
                                    !message.getTimeEdited().toLocalDateTime().isEqual(storedMessage.getTimeEdited())) {
                                storedMessage.setMessage(messageContent);
                                storedMessage.setTimeEdited(message.getTimeEdited().toLocalDateTime());
                            }

                            jdbcAggregateTemplate.update(storedMessage);
                        } else {
                            storedMessage = create(
                                    message.getIdLong(),
                                    messageContent,
                                    imageUrl,
                                    message.getTimeCreated().toLocalDateTime(),
                                    message.getTimeEdited() != null ? message.getTimeEdited().toLocalDateTime() : message.getTimeCreated().toLocalDateTime());

                            jdbcAggregateTemplate.insert(storedMessage);
                        }
                    }
                }
            }
        }

        final var allMessageIds = dailyMemeRepository.findAll().stream().map(BaseDailyMemeEntity::getMessageId).collect(Collectors.toSet());
        final var deletedMessageIds = CollectionUtils.disjunction(allMessageIds, messageIds);

        for (final var deletedMessageId : deletedMessageIds) {
            if (dailyMemeRepository.existsById(deletedMessageId)) {
                dailyMemeRepository.deleteById(deletedMessageId);
            }
        }

        Constants.debugToTestChannel(philJda, "Successfully scraped %s messages".formatted(name));
    }

    public abstract BaseDailyMemeEntity create(final long messageId, final String message, final String imageUrl, final LocalDateTime timeCreated, final LocalDateTime timeEdited);
}
