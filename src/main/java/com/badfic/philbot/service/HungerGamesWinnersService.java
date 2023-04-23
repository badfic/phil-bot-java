package com.badfic.philbot.service;

import com.badfic.philbot.commands.BaseNormalCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.HungerGamesWinnerEntity;
import com.badfic.philbot.data.HungerGamesWinnerRepository;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HungerGamesWinnersService extends BaseNormalCommand {

    private static final String REACTION_EMOJI = "\uD83C\uDFC1";

    private final HungerGamesWinnerRepository hungerGamesWinnerRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    public HungerGamesWinnersService(HungerGamesWinnerRepository hungerGamesWinnerRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
        name = "hungerGames";
        requiredRole = Constants.ADMIN_ROLE;

        this.hungerGamesWinnerRepository = hungerGamesWinnerRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split("\\s+");

        if ("delete".equalsIgnoreCase(args[0])) {
            long messageId;
            try {
                messageId = Long.parseLong(args[1]);
            } catch (NumberFormatException nfe) {
                event.replyError("Could not parse messageId: " + args[1]);
                return;
            }

            hungerGamesWinnerRepository.deleteById(messageId);
            event.replySuccess("Deleted hunger games message: " + messageId);
            return;
        }

        event.replyError("Could not parse arg: " + args[0]);
    }

    public void addWinner(MessageReactionAddEvent event) {
        long messageId = event.getMessageIdLong();
        long channelId = event.getChannel().getIdLong();

        TextChannel textChannel = philJda.getTextChannelById(channelId);

        if (!"the-hungerdome".equals(textChannel.getName())) {
            return;
        }

        textChannel.retrieveMessageById(messageId).queue(message -> {
            HungerGamesWinnerEntity storedMessage;
            Optional<HungerGamesWinnerEntity> storedMessageOpt = hungerGamesWinnerRepository.findById(messageId);

            if (storedMessageOpt.isPresent()) {
                storedMessage = storedMessageOpt.get();

                if (Objects.nonNull(message.getTimeEdited()) &&
                        !message.getTimeEdited().toLocalDateTime().isEqual(storedMessage.getTimeEdited())) {
                    String content = getMessageContent(message);

                    storedMessage.setMessage(content);
                    storedMessage.setTimeEdited(message.getTimeEdited().toLocalDateTime());
                    hungerGamesWinnerRepository.save(storedMessage);
                }
            } else {
                String content = getMessageContent(message);

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
