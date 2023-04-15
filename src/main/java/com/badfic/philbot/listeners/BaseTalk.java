package com.badfic.philbot.listeners;

import com.badfic.philbot.commands.BaseNormalCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.apache.commons.lang3.StringUtils;

public abstract class BaseTalk extends BaseNormalCommand {

    public BaseTalk(String name) {
        this.name = name;
        guildOnly = false;
        help = Constants.PREFIX + name + " #channel Type Your Message\nExample: !!philTalk #general Hello Swamplings";
    }

    public abstract Function<SwampyGamesConfig, String> usernameGetter();
    public abstract Function<SwampyGamesConfig, String> avatarGetter();

    @Override
    public void execute(CommandEvent event) {
        if (event.getChannelType() != ChannelType.PRIVATE) {
            return;
        }

        Member memberById = philJda.getGuildById(baseConfig.guildId).getMemberById(event.getAuthor().getId());
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

        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        String finalMsg = msg;
        philJda.getTextChannelsByName(split[0].replace("#", ""), true)
                .stream()
                .findFirst()
                .ifPresent(channel -> {
                    long channelId = channel.getIdLong();

                    if (usernameGetter() == null) {
                        channel.sendTyping()
                                .submit()
                                .thenRun(() -> channel.sendMessage(finalMsg).queueAfter(5, TimeUnit.SECONDS));
                        return;
                    }

                    discordWebhookSendService.sendMessage(channelId, usernameGetter().apply(swampyGamesConfig), avatarGetter().apply(swampyGamesConfig),
                            finalMsg);
                });
    }

}
