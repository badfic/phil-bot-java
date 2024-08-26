package com.badfic.philbot.service;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.bang.BaseBangCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DailyMarvelMemeEntity;
import com.badfic.philbot.data.DailyMarvelMemeRepository;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
public class DailyMarvelMemeService extends BaseBangCommand {

    private static final String SEARCH_STRING = "Out of context Marvel meme";

    private final DailyMarvelMemeRepository dailyMarvelMemeRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    public DailyMarvelMemeService(final DailyMarvelMemeRepository dailyMarvelMemeRepository, final JdbcAggregateTemplate jdbcAggregateTemplate) {
        this.name = "updateDailyMarvelMemes";
        this.ownerCommand = true;
        this.dailyMarvelMemeRepository = dailyMarvelMemeRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
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
        return dailyMarvelMemeRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(DailyMarvelMemeEntity::getTimeCreated))
                .map(entity -> StringEscapeUtils.escapeHtml4(entity.getMessage()) + "<br/>\n<img src=\"" + entity.getImageUrl() + "\" class=\"img-fluid\">")
                .toList();
    }

    @Scheduled(cron = "0 0 * * * *")
    private void refreshImageUrls() {
        final var textChannelsByName = philJda.getTextChannelsByName(Constants.MEMES_CHANNEL, false);
        if (CollectionUtils.isEmpty(textChannelsByName)) {
            log.error("DailyMarvelMemeService Failed to find [channel={}]", Constants.MEMES_CHANNEL);
            return;
        }
        final var channel = textChannelsByName.getFirst();

        final var messageIds = new LongArrayList();
        final var memeEntities = dailyMarvelMemeRepository.findAll();
        for (final var memeEntity : memeEntities) {
            final var imageUrl = memeEntity.getImageUrl();
            if (StringUtils.startsWithIgnoreCase(imageUrl, "https://media.discordapp.net/")) {
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

        final var futures = new ArrayList<CompletableFuture<Message>>();
        for (final var messageId : messageIds) {
            final var messageFuture = channel.retrieveMessageById(messageId).submit();
            futures.add(messageFuture);

            messageFuture.whenComplete((message, err) -> {
                if (err != null) {
                    log.error("Failed to find [messageId={}]", messageId, err);
                    return;
                }

                if (CollectionUtils.isNotEmpty(message.getEmbeds())) {
                    message.getEmbeds()
                            .stream()
                            .findFirst()
                            .flatMap(embed -> Optional.ofNullable(embed.getImage()))
                            .map(MessageEmbed.ImageInfo::getProxyUrl)
                            .ifPresent(imageUrl -> {
                                dailyMarvelMemeRepository.findById(messageId).ifPresent(entity -> {
                                    entity.setImageUrl(imageUrl);
                                    dailyMarvelMemeRepository.save(entity);
                                });
                            });
                } else if (CollectionUtils.isNotEmpty(message.getAttachments())) {
                    message.getAttachments()
                            .stream()
                            .findFirst()
                            .map(Message.Attachment::getProxyUrl)
                            .ifPresent(imageUrl -> {
                                dailyMarvelMemeRepository.findById(messageId).ifPresent(entity -> {
                                    entity.setImageUrl(imageUrl);
                                    dailyMarvelMemeRepository.save(entity);
                                });
                            });
                }
            });
        }

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        Constants.debugToModLogsChannel(philJda, "Successfully updated %d daily marvel memes image URLs".formatted(messageIds.size()));
    }

    private void scrapeAllMemes() {
        final var textChannelsByName = philJda.getTextChannelsByName(Constants.MEMES_CHANNEL, false);
        if (CollectionUtils.isEmpty(textChannelsByName)) {
            log.error("DailyMarvelMemeService Failed to find [channel={}]", Constants.MEMES_CHANNEL);
            return;
        }

        final var cursedChannel = textChannelsByName.getFirst();
        final var messageIds = new HashSet<Long>();

        var lastMsgId = 0L;
        while (lastMsgId != -1) {
            final MessageHistory history;
            if (lastMsgId == 0) {
                history = cursedChannel.getHistoryFromBeginning(100).timeout(30, TimeUnit.SECONDS).complete();
            } else {
                history = cursedChannel.getHistoryAfter(lastMsgId, 100).timeout(30, TimeUnit.SECONDS).complete();
            }

            final var historyList = history.getRetrievedHistory();
            lastMsgId = CollectionUtils.isNotEmpty(historyList)
                    ? historyList.getFirst().getIdLong()
                    : -1;

            for (final var message : historyList) {
                if (StringUtils.containsIgnoreCase(message.getContentRaw(), SEARCH_STRING)) {
                    if (CollectionUtils.isNotEmpty(message.getAttachments())) {
                        final var imageUrl = message.getAttachments().getFirst().getProxyUrl();
                        final var messageContent = message.getContentRaw();

                        messageIds.add(message.getIdLong());

                        final var storedMessageOpt = dailyMarvelMemeRepository.findById(message.getIdLong());

                        final DailyMarvelMemeEntity storedMessage;
                        if (storedMessageOpt.isPresent()) {
                            storedMessage = storedMessageOpt.get();
                            storedMessage.setImageUrl(imageUrl);

                            if (Objects.nonNull(message.getTimeEdited()) &&
                                    !message.getTimeEdited().toLocalDateTime().isEqual(storedMessage.getTimeEdited())) {
                                storedMessage.setMessage(messageContent);
                                storedMessage.setTimeEdited(message.getTimeEdited().toLocalDateTime());
                            }

                            dailyMarvelMemeRepository.save(storedMessage);
                        } else {
                            storedMessage = new DailyMarvelMemeEntity(
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

        final var deletedMessageIds = CollectionUtils.disjunction(dailyMarvelMemeRepository.findAllIds(), messageIds);

        for (final var deletedMessageId : deletedMessageIds) {
            if (dailyMarvelMemeRepository.existsById(deletedMessageId)) {
                dailyMarvelMemeRepository.deleteById(deletedMessageId);
            }
        }

        Constants.debugToTestChannel(philJda, "Successfully scraped daily marvel meme messages");
    }
}
