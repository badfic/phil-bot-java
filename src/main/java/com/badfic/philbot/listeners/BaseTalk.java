package com.badfic.philbot.listeners;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;

public abstract class BaseTalk extends Command {

    public BaseTalk(String name) {
        this.name = name;
        guildOnly = false;
    }

    public abstract JDA getJda();

    @Override
    public void execute(CommandEvent event) {
        if (event.getChannelType() != ChannelType.PRIVATE) {
            return;
        }

        String[] split = event.getArgs().split("\\s+");
        if (split.length < 2) {
            event.replyError("Badly formatted command. Example `!!" + name + " channel-name whatever you want me to say`");
            return;
        }

        String msg = event.getMessage().getContentRaw();
        msg = msg.replace("!!" + name + " ", "");
        msg = msg.replace(split[0], "");
        msg = msg.trim();

        String finalMsg = msg;
        getJda().getGuilds()
                .get(0)
                .getTextChannelsByName(split[0].replace("#", ""), true)
                .stream()
                .findFirst()
                .ifPresent(channel -> {
                    channel.sendTyping()
                            .submit()
                            .thenRun(() -> channel.sendMessage(finalMsg).queueAfter(5, TimeUnit.SECONDS));
                });
    }

}
