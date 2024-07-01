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
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

class Keanu {
    @Component
    static class Command extends BasicResponsesBot<KeanuResponsesConfig> {
        Command(KeanuResponsesConfigRepository keanuResponsesConfigRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
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
        private static final String[] GOOD_MORNING_GIFS = {
                "https://media.giphy.com/media/8rFNes6jllJQRnHTsF/giphy.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638622099832852/donut.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638643562086432/smilesmile.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638684641230908/hecute.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638695999537192/beardrub.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638707026362368/bow.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638709043691580/keanu.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638732288393256/yeah.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638754799353887/eyy.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638757303484526/eyebrows.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638770238586950/kitteneanu.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638772667220049/gay.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638780715958403/imweird_imaweirdo.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638791629668493/keanu_by_firelight.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638807534469141/keanu_smooch.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638810201915392/keanu_stark.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638826379346031/breathtaking.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638835518734446/breath_taking.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638836797997106/winkwonk.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638849771110410/laugh.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638864857890816/keanushark.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638874966163526/keanu_marshmallow.gif",
                "https://cdn.discordapp.com/attachments/323666308107599872/752638879500206171/keanu_confetti.gif",
                "https://cdn.discordapp.com/attachments/741030569307275436/753991114301898762/image0.png"
        };
        private static final Pattern PUPPY_PATTERN = Constants.compileWords("puppy|puppies|pupper|doggo|doge");
        private static final String HELLO_GIF = "https://media.giphy.com/media/PnUatAYWMEMvmiwsyx/giphy.gif";
        private static final String PUPPIES_GIF = "https://media.giphy.com/media/8rFNes6jllJQRnHTsF/giphy.gif";

        private final Command keanuCommand;

        @Scheduled(cron = "${swampy.schedule.keanu.goodmorning}", zone = "${swampy.schedule.timezone}")
        void goodMorning() {
            TextChannel general = philJda.getTextChannelsByName("general", false).getFirst();
            SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

            discordWebhookSendService.sendMessage(general.getIdLong(), swampyGamesConfig.getKeanuNickname(), swampyGamesConfig.getKeanuAvatar(),
                    Constants.pickRandom(GOOD_MORNING_GIFS));
        }

        void onMessageReceived(@NotNull MessageReceivedEvent event, SwampyGamesConfig swampyGamesConfig) {
            String msgContent = event.getMessage().getContentRaw().toLowerCase(Locale.ENGLISH);

            long channelId = event.getMessage().getChannel().getIdLong();

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
