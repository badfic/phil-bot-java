package com.badfic.philbot.service;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.bang.BaseBangCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DailyLegendsMemeEntity;
import com.badfic.philbot.data.DailyLegendsMemeRepository;
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
public class DailyLegendsMemeService extends BaseBangCommand implements DailyTickable {

    private static final String SEARCH_STRING = "out of context legends of tomorrow meme";

    private final DailyLegendsMemeRepository dailyLegendsMemeRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    public DailyLegendsMemeService(final DailyLegendsMemeRepository dailyLegendsMemeRepository, final JdbcAggregateTemplate jdbcAggregateTemplate) {
        this.name = "updateDailyLegendsMemes";
        this.ownerCommand = true;
        this.dailyLegendsMemeRepository = dailyLegendsMemeRepository;
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

    @Override
    public void runDailyTask() {
        scrapeAllMemes();
    }

    public List<String> getMessages() {
        return dailyLegendsMemeRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(DailyLegendsMemeEntity::getTimeCreated))
                .map(entity -> StringEscapeUtils.escapeHtml4(entity.getMessage()) + "<br/>\n<img src=\"" + entity.getImageUrl() + "\" class=\"img-fluid\">")
                .toList();
    }

    @Scheduled(cron = "${swampy.schedule.imagerefresh}", zone = "${swampy.schedule.timezone}")
    private void refreshImageUrls() {
        final var textChannelsByName = philJda.getTextChannelsByName(Constants.MEMES_CHANNEL, false);
        if (CollectionUtils.isEmpty(textChannelsByName)) {
            log.error("DailyLegendsMemeService Failed to find [channel={}]", Constants.MEMES_CHANNEL);
            return;
        }
        final var channel = textChannelsByName.getFirst();

        final var memeEntities = dailyLegendsMemeRepository.findAll();
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
                                dailyLegendsMemeRepository.findById(messageId).ifPresent(entity -> {
                                    entity.setImageUrl(imageUrl);
                                    dailyLegendsMemeRepository.save(entity);
                                });
                            });
                } else if (CollectionUtils.isNotEmpty(message.getAttachments())) {
                    message.getAttachments()
                            .stream()
                            .findFirst()
                            .map(Message.Attachment::getProxyUrl)
                            .ifPresent(imageUrl -> {
                                dailyLegendsMemeRepository.findById(messageId).ifPresent(entity -> {
                                    entity.setImageUrl(imageUrl);
                                    dailyLegendsMemeRepository.save(entity);
                                });
                            });
                }
            });
        }

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        Constants.debugToModLogsChannel(philJda, "Successfully updated %d daily legends memes image URLs".formatted(messageIds.size()));
    }

    private void scrapeAllMemes() {
        final var textChannelsByName = philJda.getTextChannelsByName(Constants.MEMES_CHANNEL, false);
        if (CollectionUtils.isEmpty(textChannelsByName)) {
            log.error("DailyLegendsMemeService Failed to find [channel={}]", Constants.MEMES_CHANNEL);
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

                        final var storedMessageOpt = dailyLegendsMemeRepository.findById(message.getIdLong());

                        final DailyLegendsMemeEntity storedMessage;
                        if (storedMessageOpt.isPresent()) {
                            storedMessage = storedMessageOpt.get();
                            storedMessage.setImageUrl(imageUrl);

                            if (Objects.nonNull(message.getTimeEdited()) &&
                                    !message.getTimeEdited().toLocalDateTime().isEqual(storedMessage.getTimeEdited())) {
                                storedMessage.setMessage(messageContent);
                                storedMessage.setTimeEdited(message.getTimeEdited().toLocalDateTime());
                            }

                            dailyLegendsMemeRepository.save(storedMessage);
                        } else {
                            storedMessage = new DailyLegendsMemeEntity(
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

        final var deletedMessageIds = CollectionUtils.disjunction(dailyLegendsMemeRepository.findAllIds(), messageIds);

        for (final var deletedMessageId : deletedMessageIds) {
            if (dailyLegendsMemeRepository.existsById(deletedMessageId)) {
                dailyLegendsMemeRepository.deleteById(deletedMessageId);
            }
        }

        Constants.debugToTestChannel(philJda, "Successfully scraped daily legends meme messages");
    }
}
