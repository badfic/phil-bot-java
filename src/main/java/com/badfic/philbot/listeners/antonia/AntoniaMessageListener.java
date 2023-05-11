package com.badfic.philbot.listeners.antonia;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.service.BaseService;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AntoniaMessageListener extends BaseService {

    private static final Pattern ANTONIA_PATTERN = Constants.compileWords("antonia|toni|tony|stark|tash|iron man|tin can");
    private static final Map<String, List<Pair<Pattern, String>>> USER_TRIGGER_WORDS = Map.of(
            "307611036134146080", List.of(ImmutablePair.of(Constants.compileWords("I love you"), "I know")),
            "323520695550083074", List.of(ImmutablePair.of(Constants.compileWords("togna"), "bologna"),
                    ImmutablePair.of(Constants.compileWords("child"), "Yes father?")));
    private static final ConcurrentMap<Long, Pair<String, Long>> LAST_WORD_MAP = new ConcurrentHashMap<>();

    private final AntoniaCommand antoniaCommand;

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String msgContent = event.getMessage().getContentRaw().toLowerCase(Locale.ENGLISH);

        SwampyGamesConfig swampyGamesConfig = swampyGamesConfigDal.get();

        long channelId = event.getMessage().getChannel().getIdLong();

        if (msgContent.contains("kite man")) {
            discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getAntoniaNickname(), swampyGamesConfig.getAntoniaAvatar(), "Hell yea");
            return;
        }
        if (msgContent.contains("owen wilson")) {
            discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getAntoniaNickname(), swampyGamesConfig.getAntoniaAvatar(), "wow");
            return;
        }

        LAST_WORD_MAP.compute(channelId, (key, oldValue) -> {
            if (oldValue == null) {
                return new ImmutablePair<>(msgContent, 1L);
            }
            if (oldValue.getLeft().equals(msgContent)) {
                if (oldValue.getRight() + 1 >= 3 && "bird".equals(msgContent)) {
                    discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getAntoniaNickname(), swampyGamesConfig.getAntoniaAvatar(),
                            "the bird is the word");
                    return new ImmutablePair<>(msgContent, 0L);
                }
                if (oldValue.getRight() + 1 >= 3 && "word".equals(msgContent)) {
                    discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getAntoniaNickname(), swampyGamesConfig.getAntoniaAvatar(),
                            "the word is the bird");
                    return new ImmutablePair<>(msgContent, 0L);
                }
                if (oldValue.getRight() + 1 >= 3 && "mattgrinch".equals(msgContent)) {
                    discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getAntoniaNickname(), swampyGamesConfig.getAntoniaAvatar(),
                            "https://cdn.discordapp.com/attachments/707453916882665552/914409167610056734/unknown.png");
                    return new ImmutablePair<>(msgContent, 0L);
                }

                return new ImmutablePair<>(msgContent, oldValue.getRight() + 1);
            } else {
                return new ImmutablePair<>(msgContent, 1L);
            }
        });

        Constants.checkUserTriggerWords(event, USER_TRIGGER_WORDS, swampyGamesConfig.getAntoniaNickname(), swampyGamesConfig.getAntoniaAvatar(),
                discordWebhookSendService);

        if (ANTONIA_PATTERN.matcher(msgContent).find()) {
            antoniaCommand.execute(new CommandEvent(event));
            return;
        }
    }

}
