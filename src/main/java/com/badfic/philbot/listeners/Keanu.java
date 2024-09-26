package com.badfic.philbot.listeners;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.data.keanu.KeanuResponsesConfig;
import com.badfic.philbot.data.keanu.KeanuResponsesConfigRepository;
import com.badfic.philbot.service.BaseService;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

class Keanu {
    @Component
    static class Command extends BasicResponsesBot<KeanuResponsesConfig> {
        Command(final KeanuResponsesConfigRepository keanuResponsesConfigRepository, final JdbcAggregateTemplate jdbcAggregateTemplate) {
            super(keanuResponsesConfigRepository, jdbcAggregateTemplate, "keanu", KeanuResponsesConfig::new,
                    SwampyGamesConfig::getKeanuNickname, SwampyGamesConfig::getKeanuAvatar);
        }
    }

    @Component
    static class Talk extends BaseTalk {
        Talk() {
            super("keanuTalk");
        }

        @Override
        protected Function<SwampyGamesConfig, String> usernameGetter() {
            return SwampyGamesConfig::getKeanuNickname;
        }

        @Override
        protected Function<SwampyGamesConfig, String> avatarGetter() {
            return SwampyGamesConfig::getKeanuAvatar;
        }
    }


    @Component
    @RequiredArgsConstructor
    static class MessageListener extends BaseService {
        private static final Pattern KEANU_PATTERN = Constants.compileWords("keanu|reeves|neo|john wick|puppy|puppies|pupper|doggo|doge");
        private static final String[] FIGHT_GIFS = {
                "https://cdn.discordapp.com/attachments/707453916882665552/880260103830380574/john-wick-3.gif",
                "https://cdn.discordapp.com/attachments/794506942906761226/1095949481024954429/keanu-reeves-weird.gif",
                "https://cdn.discordapp.com/attachments/794506942906761226/1095949755072393236/PointedMistyHog.gif",
                "https://cdn.discordapp.com/attachments/794506942906761226/1095950269977743380/SlimCarelessDalmatian.gif",
                "https://cdn.discordapp.com/attachments/794506942906761226/1095950252047093780/EverlastingHastyHorseshoebat.gif"
        };
        private static final Pattern PUPPY_PATTERN = Constants.compileWords("puppy|puppies|pupper|doggo|doge");
        private static final String HELLO_GIF = "https://media.giphy.com/media/PnUatAYWMEMvmiwsyx/giphy.gif";
        private static final String PUPPIES_GIF = "https://media.giphy.com/media/8rFNes6jllJQRnHTsF/giphy.gif";

        private final Command keanuCommand;

        void onMessageReceived(final @NotNull MessageReceivedEvent event, final SwampyGamesConfig swampyGamesConfig) {
            final var msgContent = event.getMessage().getContentRaw().toLowerCase(Locale.ENGLISH);

            final var channelId = event.getMessage().getChannel().getIdLong();

            if (msgContent.contains("lightning mcqueen")) {
                discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getKeanuNickname(), swampyGamesConfig.getKeanuAvatar(), "KACHOW!");
                return;
            }

            if (msgContent.contains("hello")) {
                discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getKeanuNickname(), swampyGamesConfig.getKeanuAvatar(), HELLO_GIF);
                return;
            }

            if (msgContent.contains("fight")) {
                discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getKeanuNickname(), swampyGamesConfig.getKeanuAvatar(),
                        Constants.pickRandom(FIGHT_GIFS));
                return;
            }

            if (PUPPY_PATTERN.matcher(msgContent).find()) {
                discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getKeanuNickname(), swampyGamesConfig.getKeanuAvatar(), PUPPIES_GIF);
                return;
            }

            if (KEANU_PATTERN.matcher(msgContent).find()) {
                keanuCommand.execute(new CommandEvent(event));
                return;
            }
        }
    }
}
