package com.badfic.philbot.service;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.bang.BaseBangCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.HungerGamesWinnerEntity;
import com.badfic.philbot.data.HungerGamesWinnerRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HungerGamesWinnersService extends BaseBangCommand implements DailyTickable {

    private static final String REACTION_EMOJI = "\uD83C\uDFC1";

    private final HungerGamesWinnerRepository hungerGamesWinnerRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    public HungerGamesWinnersService(final HungerGamesWinnerRepository hungerGamesWinnerRepository, final JdbcAggregateTemplate jdbcAggregateTemplate) {
        name = "hungerGames";
        requiredRole = Constants.ADMIN_ROLE;

        this.hungerGamesWinnerRepository = hungerGamesWinnerRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
    }

    @Override
    public void execute(final CommandEvent event) {
        final var args = event.getArgs().split("\\s+");

        if ("delete".equalsIgnoreCase(args[0])) {
            final long messageId;
            try {
                messageId = Long.parseLong(args[1]);
            } catch (final NumberFormatException nfe) {
                event.replyError("Could not parse messageId: " + args[1]);
                return;
            }

            hungerGamesWinnerRepository.deleteById(messageId);
            event.replySuccess("Deleted hunger games message: " + messageId);
            return;
        }

        event.replyError("Could not parse arg: " + args[0]);
    }

    @Override
    public void runDailyTask() {
        refreshImageUrls();
    }

    private void refreshImageUrls() {
        final var textChannelsByName = philJda.getTextChannelsByName(Constants.HUNGERDOME_CHANNEL, false);
        if (CollectionUtils.isEmpty(textChannelsByName)) {
            log.error("HungerGamesWinnersService Failed to find [channel={}]", Constants.HUNGERDOME_CHANNEL);
            return;
        }
        final var channel = textChannelsByName.getFirst();

        final var messageIds = hungerGamesWinnerRepository.findAllIds();

        final var futures = new ArrayList<CompletableFuture<Message>>();
        for (final var messageId : messageIds) {
            final var messageFuture = channel.retrieveMessageById(messageId).submit();
            futures.add(messageFuture);

            messageFuture.whenComplete((message, err) -> {
                if (err != null) {
                    log.error("Failed to find [messageId={}]", messageId, err);
                    return;
                }

                final var messageContent = getMessageContent(message);
                hungerGamesWinnerRepository.findById(messageId).ifPresent(entity -> {
                    entity.setMessage(messageContent);
                    hungerGamesWinnerRepository.save(entity);
                });
            });
        }

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        Constants.debugToTestChannel(philJda, "Successfully updated hunger games winners image URLs");
    }

    public void addWinner(MessageReactionAddEvent event) {
        final var messageId = event.getMessageIdLong();
        final var channelId = event.getChannel().getIdLong();

        final var textChannel = philJda.getTextChannelById(channelId);

        if (!Constants.HUNGERDOME_CHANNEL.equals(textChannel.getName())) {
            return;
        }

        textChannel.retrieveMessageById(messageId).queue(message -> {
            final var storedMessageOpt = hungerGamesWinnerRepository.findById(messageId);

            final HungerGamesWinnerEntity storedMessage;
            if (storedMessageOpt.isPresent()) {
                storedMessage = storedMessageOpt.get();

                if (Objects.nonNull(message.getTimeEdited()) &&
                        !message.getTimeEdited().toLocalDateTime().isEqual(storedMessage.getTimeEdited())) {
                    final var content = getMessageContent(message);

                    storedMessage.setMessage(content);
                    storedMessage.setTimeEdited(message.getTimeEdited().toLocalDateTime());
                    hungerGamesWinnerRepository.save(storedMessage);
                }
            } else {
                final var content = getMessageContent(message);

                storedMessage = new HungerGamesWinnerEntity(message.getIdLong(), content, message.getTimeCreated().toLocalDateTime(),
                        message.getTimeEdited() != null ? message.getTimeEdited().toLocalDateTime() : message.getTimeCreated().toLocalDateTime());
                jdbcAggregateTemplate.insert(storedMessage);
            }

            message.addReaction(Emoji.fromUnicode("✅")).queue();
        });
    }

    public String getEmoji() {
        return REACTION_EMOJI;
    }

    public List<String> getMessages() {
        return hungerGamesWinnerRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(HungerGamesWinnerEntity::getTimeCreated))
                .map(HungerGamesWinnerEntity::getMessage)
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
