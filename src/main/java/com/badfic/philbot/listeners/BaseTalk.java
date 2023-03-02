package com.badfic.philbot.listeners;

import com.badfic.philbot.config.Constants;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;

public abstract class BaseTalk extends Command {

    @Autowired
    @Qualifier("philJda")
    @Lazy
    protected JDA philJda;

    public BaseTalk(String name) {
        this.name = name;
        guildOnly = false;
        help = Constants.PREFIX + name + " #channel Type Your Message\nExample: !!philTalk #general Hello Swamplings";
    }

    public abstract JDA getJda();

    @Override
    public void execute(CommandEvent event) {
        if (event.getChannelType() != ChannelType.PRIVATE) {
            return;
        }

        Member memberById = philJda.getGuilds().get(0).getMemberById(event.getAuthor().getId());
        if (memberById == null || memberById.getRoles().stream().noneMatch(r -> r.getName().equalsIgnoreCase(Constants.ADMIN_ROLE))) {
            event.replyError("Failed, either you're not an admin or you left the server or the member cache is broken");
            return;
        }

        String[] split = event.getArgs().split("\\s+");
        if (split.length < 2) {
            event.replyError("Badly formatted command. Example `!!" + name + " channel-name whatever you want me to say`");
            return;
        }

        String msg = event.getMessage().getContentRaw();
        msg = StringUtils.replaceIgnoreCase(msg, Constants.PREFIX + name + " ", "");
        msg = msg.replace(split[0], "").trim();

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
