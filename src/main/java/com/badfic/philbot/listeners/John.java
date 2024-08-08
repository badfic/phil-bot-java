package com.badfic.philbot.listeners;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.bang.RemindersCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.data.john.JohnResponsesConfig;
import com.badfic.philbot.data.john.JohnResponsesConfigRepository;
import com.badfic.philbot.service.BaseService;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

class John {
    @Component
    static class Command extends BasicResponsesBot<JohnResponsesConfig> {
        Command(final JohnResponsesConfigRepository johnResponsesConfigRepository, final JdbcAggregateTemplate jdbcAggregateTemplate) {
            super(johnResponsesConfigRepository, jdbcAggregateTemplate, "john", JohnResponsesConfig::new,
                    SwampyGamesConfig::getJohnNickname, SwampyGamesConfig::getJohnAvatar);
        }
    }

    @Component
    static class Talk extends BaseTalk {
        Talk() {
            super("johnTalk");
        }

        @Override
        protected Function<SwampyGamesConfig, String> usernameGetter() {
            return SwampyGamesConfig::getJohnNickname;
        }

        @Override
        protected Function<SwampyGamesConfig, String> avatarGetter() {
            return SwampyGamesConfig::getJohnAvatar;
        }
    }

    @Service
    @RequiredArgsConstructor
    @Slf4j
    static class MessageListener extends BaseService {
        private static final Pattern JOHN_PATTERN = Constants.compileWords("john|constantine|johnno|johnny|hellblazer");
        private static final Pattern REMINDER_PATTER = Pattern.compile("\\b(remind me in |remind <@[0-9]+> in )[0-9]+\\b", Pattern.CASE_INSENSITIVE);
        private static final String[] UWU = {
                "https://tenor.com/bbmRv.gif",
                "https://tenor.com/X7Nq.gif",
                "https://tenor.com/bhbNA.gif",
                "https://tenor.com/baaJk.gif",
                "https://tenor.com/bedWY.gif",
                "https://tenor.com/bpXCH.gif",
                "https://tenor.com/bjCS0.gif",
                "https://tenor.com/bcse7.gif",
                "https://tenor.com/bcsfc.gif",
                "https://tenor.com/bcse9.gif",
                "https://tenor.com/bcsfb.gif",
                "https://tenor.com/bcsfd.gif",
                "https://tenor.com/bcsfe.gif",
                "https://tenor.com/bcse6.gif",
                "https://tenor.com/bh7al.gif",
                "https://tenor.com/bgP9d.gif",
                "https://tenor.com/beotA.gif",
                "https://tenor.com/bfoYa.gif",
                "https://tenor.com/bhzQJ.gif",
                "https://tenor.com/bhngy.gif",
                "https://tenor.com/POHh.gif",
                "https://cdn.discordapp.com/attachments/530193785351569423/777962192083353611/zaricat.gif",
                "https://emoji.gg/assets/emoji/8178_arainblob.gif",
                "https://emoji.gg/assets/emoji/8195_agooglecat.gif",
                "https://emoji.gg/assets/emoji/8569_ablobmeltsoblove.gif",
                "https://emoji.gg/assets/emoji/2231_ablobhearteyes.gif",
                "https://emoji.gg/assets/emoji/2171_ablobaww.gif",
                "https://emoji.gg/assets/emoji/1279_Flying_Hearts_Red.gif",
                "https://cdn.discordapp.com/attachments/707453916882665552/793431107289743370/giphy.gif",
                "https://cdn.discordapp.com/attachments/707453916882665552/793431132963733514/giphy2.gif",
                "https://cdn.discordapp.com/attachments/707453916882665552/793431157717991464/chimmy.gif",
                "https://cdn.discordapp.com/attachments/707453916882665552/793431180099452948/tata.gif",
                "https://cdn.discordapp.com/attachments/761398315119280158/867804756621131786/chris-evans-steve-rogers-heart-Favim.png"
        };
        private static final String[] GOOD_BOT = {
                ":D!!!",
                "I know",
                "Yeah, yeah",
                "Everyone who puts their trust in me dies",
                "I'm damned is what I am",
                "Thanks, luv",
                "Quite right",
                "Ey up",
                "**Oi**",
                "You must be off yer trolley, mate",
                "Shut up yer biff",
                "Oi, just geg out will ya",
                "Are you messin?",
                "Give it a rest ya muppet",
                "Yeah, sound mate",
                "That's right boss innit",
                "Ain't you a proper meff, then?",
                "Thanks, luv. I'm absolutely made up.",
                "What a beut!",
                "Ta, mate",
                "https://emoji.gg/assets/emoji/1554_ablobderpyhappy.gif",
                "https://emoji.gg/assets/emoji/8783_ablobhop.gif"
        };

        private final Command johnCommand;
        private final RemindersCommand remindersCommand;

        void onMessageReceived(final @NotNull MessageReceivedEvent event, final SwampyGamesConfig swampyGamesConfig) {
            final var msgContent = event.getMessage().getContentRaw().toLowerCase(Locale.ENGLISH);

            if (REMINDER_PATTER.matcher(msgContent).find()) {
                remindersCommand.addReminder(event.getMessage(), swampyGamesConfig);
                return;
            }

            final var channelId = event.getMessage().getChannel().getIdLong();

            if ("ayy".equals(msgContent)) {
                discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getJohnNickname(), swampyGamesConfig.getJohnAvatar(), "lmao");
                return;
            }
            if ("slang".equals(msgContent)) {
                discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getJohnNickname(), swampyGamesConfig.getJohnAvatar(),
                        "You're an absolute whopper");
                return;
            }
            if ("stughead".equals(msgContent)) {
                discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getJohnNickname(), swampyGamesConfig.getJohnAvatar(), "\uD83D\uDC40");
                return;
            }
            if ("africa".equals(msgContent)) {
                discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getJohnNickname(), swampyGamesConfig.getJohnAvatar(),
                        "I BLESS THE RAINS DOWN IN AAAAFRICAAA");
                return;
            }
            if ("cool".equals(msgContent)) {
                discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getJohnNickname(), swampyGamesConfig.getJohnAvatar(), "cool cool cool");
                return;
            }
            if ("uwu".equals(msgContent)) {
                discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getJohnNickname(), swampyGamesConfig.getJohnAvatar(), Constants.pickRandom(UWU));
                return;
            }
            if ("good bot".equals(msgContent)) {
                if (601043580945170443L == event.getAuthor().getIdLong()) {
                    discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getJohnNickname(), swampyGamesConfig.getJohnAvatar(), ":D!!!");
                } else {
                    discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getJohnNickname(), swampyGamesConfig.getJohnAvatar(),
                            Constants.pickRandom(GOOD_BOT));
                }
                return;
            }

            if (JOHN_PATTERN.matcher(msgContent).find()) {
                johnCommand.execute(new CommandEvent(event));
                return;
            }
        }
    }
}
