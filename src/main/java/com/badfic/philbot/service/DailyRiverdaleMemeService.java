package com.badfic.philbot.service;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.bang.BaseBangCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DailyRiverdaleMemeEntity;
import com.badfic.philbot.data.DailyRiverdaleMemeRepository;
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
public class DailyRiverdaleMemeService extends BaseBangCommand {

    private static final String SEARCH_STRING = "out of context riverdale meme";

    private final DailyRiverdaleMemeRepository dailyRiverdaleMemeRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    public DailyRiverdaleMemeService(final DailyRiverdaleMemeRepository dailyRiverdaleMemeRepository, final JdbcAggregateTemplate jdbcAggregateTemplate) {
        this.name = "updateDailyRiverdaleMemes";
        this.ownerCommand = true;
        this.dailyRiverdaleMemeRepository = dailyRiverdaleMemeRepository;
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
        return dailyRiverdaleMemeRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(DailyRiverdaleMemeEntity::getTimeCreated))
                .map(entity -> StringEscapeUtils.escapeHtml4(entity.getMessage()) + "<br/>\n<img src=\"" + entity.getImageUrl() + "\" class=\"img-fluid\">")
                .toList();
    }

    @Scheduled(cron = "${swampy.schedule.imagerefresh}", zone = "${swampy.schedule.timezone}")
    private void refreshImageUrls() {
        final var textChannelsByName = philJda.getTextChannelsByName(Constants.CURSED_SWAMP_CHANNEL, false);
        if (CollectionUtils.isEmpty(textChannelsByName)) {
            log.error("DailyRiverdaleMemeService Failed to find [channel={}]", Constants.CURSED_SWAMP_CHANNEL);
            return;
        }
        final var channel = textChannelsByName.getFirst();

        final var memeEntities = dailyRiverdaleMemeRepository.findAll();
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

        final var futures = new ArrayList<CompletableFuture<Message>>();
        for (final var messageId : messageIds) {
            final var messageFuture = channel.retrieveMessageById(messageId).submit();
            futures.add(messageFuture);

            messageFuture.whenComplete((message, err) -> {
                if (CollectionUtils.isNotEmpty(message.getEmbeds())) {
                    message.getEmbeds()
                            .stream()
                            .findFirst()
                            .flatMap(embed -> Optional.ofNullable(embed.getImage()))
                            .map(MessageEmbed.ImageInfo::getProxyUrl)
                            .ifPresent(imageUrl -> {
                                dailyRiverdaleMemeRepository.findById(messageId).ifPresent(entity -> {
                                    entity.setImageUrl(imageUrl);
                                    dailyRiverdaleMemeRepository.save(entity);
                                });
                            });
                } else if (CollectionUtils.isNotEmpty(message.getAttachments())) {
                    message.getAttachments()
                            .stream()
                            .findFirst()
                            .map(Message.Attachment::getProxyUrl)
                            .ifPresent(imageUrl -> {
                                dailyRiverdaleMemeRepository.findById(messageId).ifPresent(entity -> {
                                    entity.setImageUrl(imageUrl);
                                    dailyRiverdaleMemeRepository.save(entity);
                                });
                            });
                }
            });
        }

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        if (!messageIds.isEmpty()) {
            Constants.debugToModLogsChannel(philJda, "Successfully updated %d daily riverdale memes image URLs".formatted(messageIds.size()));
        }
    }

    private void scrapeAllMemes() {
        final var textChannelsByName = philJda.getTextChannelsByName(Constants.CURSED_SWAMP_CHANNEL, false);
        if (CollectionUtils.isEmpty(textChannelsByName)) {
            log.error("DailyRiverdaleMemeService Failed to find [channel={}]", Constants.CURSED_SWAMP_CHANNEL);
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

                        final var storedMessageOpt = dailyRiverdaleMemeRepository.findById(message.getIdLong());

                        final DailyRiverdaleMemeEntity storedMessage;
                        if (storedMessageOpt.isPresent()) {
                            storedMessage = storedMessageOpt.get();
                            storedMessage.setImageUrl(imageUrl);

                            if (Objects.nonNull(message.getTimeEdited()) &&
                                    !message.getTimeEdited().toLocalDateTime().isEqual(storedMessage.getTimeEdited())) {
                                storedMessage.setMessage(messageContent);
                                storedMessage.setTimeEdited(message.getTimeEdited().toLocalDateTime());
                            }

                            dailyRiverdaleMemeRepository.save(storedMessage);
                        } else {
                            storedMessage = new DailyRiverdaleMemeEntity(
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

        final var deletedMessageIds = CollectionUtils.disjunction(dailyRiverdaleMemeRepository.findAllIds(), messageIds);

        for (final var deletedMessageId : deletedMessageIds) {
            if (dailyRiverdaleMemeRepository.existsById(deletedMessageId)) {
                dailyRiverdaleMemeRepository.deleteById(deletedMessageId);
            }
        }

        Constants.debugToTestChannel(philJda, "Successfully scraped daily riverdale meme messages");
    }
}
