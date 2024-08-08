package com.badfic.philbot.listeners;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.bang.BaseBangCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.SwampyGamesConfig;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.apache.commons.lang3.StringUtils;

public abstract class BaseTalk extends BaseBangCommand {

    public BaseTalk(String name) {
        this.name = name;
        guildOnly = false;
        help = Constants.PREFIX + name + " #channel Type Your Message\nExample: !!philTalk #general Hello Swamplings";
    }

    protected Function<SwampyGamesConfig, String> usernameGetter() {
        return null;
    }

    protected Function<SwampyGamesConfig, String> avatarGetter() {
        return null;
    }

    @Override
    public void execute(CommandEvent event) {
        if (event.getChannelType() != ChannelType.PRIVATE) {
            return;
        }

        final var memberById = philJda.getGuildById(baseConfig.guildId).getMemberById(event.getAuthor().getId());
        if (memberById == null || memberById.getRoles().stream().noneMatch(r -> r.getName().equalsIgnoreCase(Constants.ADMIN_ROLE))) {
            event.replyError("Failed, either you're not an admin or you left the server or the member cache is broken");
            return;
        }

        final var split = event.getArgs().split("\\s+");
        if (split.length < 2) {
            event.replyError("Badly formatted command. Example `!!" + name + " channel-name whatever you want me to say`");
            return;
        }

        var msg = event.getMessage().getContentRaw();
        msg = StringUtils.replaceIgnoreCase(msg, Constants.PREFIX + name + " ", "");
        msg = msg.replace(split[0], "").trim();
        final var finalMsg = msg;

        final var swampyGamesConfig = getSwampyGamesConfig();
        philJda.getTextChannelsByName(split[0].replace("#", ""), true)
                .stream()
                .findFirst()
                .ifPresent(channel -> {
                    final var channelId = channel.getIdLong();

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
