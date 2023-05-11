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
import org.apache.commons.lang3.StringUtils;

@Getter
public class CommandEvent {
    private final MessageReceivedEvent event;
    private final String args;

    public CommandEvent(MessageReceivedEvent event) {
        this.event = event;

        String contentRaw = event.getMessage().getContentRaw();
        contentRaw = contentRaw.substring(Constants.PREFIX.length()).trim();

        String commandName = null;

        for (int i = 0; i < contentRaw.length(); i++) {
            if (Character.isWhitespace(contentRaw.charAt(i))) {
                commandName = contentRaw.substring(0, i);
                break;
            }
        }

        String localArgs = StringUtils.EMPTY;
        if (commandName != null) {
            localArgs = contentRaw.substring(commandName.length()).trim();
        }
        this.args = localArgs;
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

    public void reply(String message) {
        event.getChannel().sendMessage(message).queue();
    }

    public void reply(MessageEmbed embed) {
        event.getChannel().sendMessageEmbeds(embed).queue();
    }

    public void reply(MessageEmbed embed, Consumer<Message> messageConsumer) {
        event.getChannel().sendMessageEmbeds(embed).queue(messageConsumer);
    }

    public void replySuccess(String message) {
        event.getChannel().sendMessage("✅ " + message).queue();
    }

    public void replyError(String message) {
        event.getChannel().sendMessage("❌ " + message).queue();
    }

    public void replyInDm(MessageEmbed embed) {
        event.getAuthor().openPrivateChannel().flatMap(privateChannel -> {
            return privateChannel.sendMessage(MessageCreateData.fromEmbeds(embed));
        }).queue();
    }
}
