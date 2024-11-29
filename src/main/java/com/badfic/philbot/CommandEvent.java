package com.badfic.philbot;

import com.badfic.philbot.config.Constants;
import java.util.function.Consumer;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class CommandEvent {
    private final MessageReceivedEvent event;
    @Getter private final String name;
    @Getter private final String args;

    public CommandEvent(final MessageReceivedEvent event) {
        this.event = event;

        var message = event.getMessage().getContentRaw();

        if (!StringUtils.startsWith(message, Constants.PREFIX)) {
            this.name = StringUtils.EMPTY;
            this.args = StringUtils.EMPTY;
            return;
        }

        message = message.substring(Constants.PREFIX.length()).trim();

        final var split = message.split("\\s+");

        if (ArrayUtils.getLength(split) < 1) {
            this.name = StringUtils.EMPTY;
            this.args = StringUtils.EMPTY;
            return;
        }

        this.name = split[0];

        if (ArrayUtils.getLength(split) > 1) {
            this.args = message.substring(this.name.length()).trim();
        } else {
            this.args = StringUtils.EMPTY;
        }
    }

    public User getAuthor() {
        return event.getAuthor();
    }

    public Message getMessage() {
        return event.getMessage();
    }

    public Guild getGuild() {
        return event.getGuild();
    }

    public MessageChannel getChannel() {
        return event.getChannel();
    }

    public ChannelType getChannelType() {
        return event.getChannelType();
    }

    public Member getMember() {
        return event.getMember();
    }

    public void reply(final String message) {
        event.getChannel().sendMessage(message).queue();
    }

    public void reply(final MessageEmbed embed) {
        event.getChannel().sendMessageEmbeds(embed).queue();
    }

    public void reply(final MessageEmbed embed, final Consumer<Message> messageConsumer) {
        event.getChannel().sendMessageEmbeds(embed).queue(messageConsumer);
    }

    public void replySuccess(final String message) {
        event.getChannel().sendMessage("✅ " + message).queue();
    }

    public void replyError(final String message) {
        event.getChannel().sendMessage("❌ " + message).queue();
    }

    public void replyInDm(final MessageEmbed embed) {
        event.getAuthor().openPrivateChannel().flatMap(privateChannel -> {
            return privateChannel.sendMessage(MessageCreateData.fromEmbeds(embed));
        }).queue();
    }
}
