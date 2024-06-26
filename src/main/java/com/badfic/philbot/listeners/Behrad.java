package com.badfic.philbot.listeners;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.data.behrad.BehradResponsesConfig;
import com.badfic.philbot.data.behrad.BehradResponsesConfigRepository;
import com.badfic.philbot.service.BaseService;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

class Behrad {
    @Component
    static class Command extends BasicResponsesBot<BehradResponsesConfig> {
        Command(BehradResponsesConfigRepository behradResponsesConfigRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
            super(behradResponsesConfigRepository, jdbcAggregateTemplate, "behrad", BehradResponsesConfig::new,
                    SwampyGamesConfig::getBehradNickname, SwampyGamesConfig::getBehradAvatar);
        }
    }

    @Component
    static class Talk extends BaseTalk {
        Talk() {
            super("behradTalk");
        }

        @Override
        protected Function<SwampyGamesConfig, String> usernameGetter() {
            return SwampyGamesConfig::getBehradNickname;
        }

        @Override
        protected Function<SwampyGamesConfig, String> avatarGetter() {
            return SwampyGamesConfig::getBehradAvatar;
        }
    }

    @Service
    @RequiredArgsConstructor
    static class MessageListener extends BaseService {
        private static final Pattern BEHRAD_PATTERN = Constants.compileWords("behrad|shayan|sobhian|marijuana|weed|420|stoned|stoner|kush|hey b|sup sloth");
        private static final String[] SHAYAN_IMGS = {
                "https://cdn.discordapp.com/attachments/323666308107599872/750575275783487598/MV5BMGEyZDE2YmYtNjRhNi00MzQwLThjNjItM2E5YjVjOTI3MDMwXkEyXkFqcGdeQXVyMTAzMjM0MjE0.png",
                "https://cdn.discordapp.com/attachments/323666308107599872/750575276026626129/MV5BYTRjOGE2OWUtMjk2MS00MGFkLTg2YjEtYmNjZDRjODAzNWI4XkEyXkFqcGdeQXVyMTAzMjM0MjE0.png"
        };
        private static final Pattern WEED_PATTERN = Constants.compileWords("marijuana|weed|420|stoned|stoner|kush");

        private final Command behradCommand;

        @Scheduled(cron = "${swampy.schedule.behrad.humpday}", zone = "${swampy.schedule.timezone}")
        void behradHumpDay() {
            philJda.getTextChannelsByName("general", false).stream().findAny().ifPresent(channel -> {
                SwampyGamesConfig swampyGamesConfig = swampyGamesConfigDal.get();

                discordWebhookSendService.sendMessage(channel.getIdLong(), swampyGamesConfig.getBehradNickname(), swampyGamesConfig.getBehradAvatar(),
                        "https://tenor.com/view/itis-wednesdaymy-dudes-wednesday-viralyoutube-gif-18012295");
            });
        }

        void onMessageReceived(@NotNull MessageReceivedEvent event, SwampyGamesConfig swampyGamesConfig) {
            String msgContent = event.getMessage().getContentRaw().toLowerCase(Locale.ENGLISH);

            long channelId = event.getMessage().getChannel().getIdLong();

            if (msgContent.contains("i'm gay")) {
                discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getBehradNickname(), swampyGamesConfig.getBehradAvatar(), "same");
                return;
            }

            if (msgContent.contains("salsa")) {
                discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getBehradNickname(), swampyGamesConfig.getBehradAvatar(),
                        "you know the rule about salsa ( ͡° ͜ʖ ͡°)");
                return;
            }

            if (msgContent.contains("shayan") || msgContent.contains("sobhian")) {
                discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getBehradNickname(), swampyGamesConfig.getBehradAvatar(),
                        Constants.pickRandom(SHAYAN_IMGS));
                return;
            }

            if (WEED_PATTERN.matcher(msgContent).find()) {
                discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getBehradNickname(), swampyGamesConfig.getBehradAvatar(),
                        "420 whatcha smokin? https://cdn.discordapp.com/attachments/323666308107599872/750575541266022410/cfff6b4479a51d245d26cd82e16d4f3f.png");
                return;
            }

            if (BEHRAD_PATTERN.matcher(msgContent).find()) {
                behradCommand.execute(new CommandEvent(event));
                return;
            }
        }
    }
}
