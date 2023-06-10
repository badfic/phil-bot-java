package com.badfic.philbot.listeners.antonia;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.service.BaseService;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private static final Long2ObjectMap<List<Pair<Pattern, String>>> USER_TRIGGER_WORDS = new Long2ObjectArrayMap<>(Map.of(
            307611036134146080L, List.of(ImmutablePair.of(Constants.compileWords("I love you"), "I know")),
            323520695550083074L, List.of(ImmutablePair.of(Constants.compileWords("togna"), "bologna"))
    ));

    private final AntoniaCommand antoniaCommand;

    public void onMessageReceived(@NotNull MessageReceivedEvent event, SwampyGamesConfig swampyGamesConfig) {
        String msgContent = event.getMessage().getContentRaw().toLowerCase(Locale.ENGLISH);

        long channelId = event.getMessage().getChannel().getIdLong();

        if (msgContent.contains("kite man")) {
            discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getAntoniaNickname(), swampyGamesConfig.getAntoniaAvatar(), "Hell yea");
            return;
        }
        if (msgContent.contains("owen wilson")) {
            discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getAntoniaNickname(), swampyGamesConfig.getAntoniaAvatar(), "wow");
            return;
        }

        Constants.checkUserTriggerWords(event, USER_TRIGGER_WORDS, swampyGamesConfig.getAntoniaNickname(), swampyGamesConfig.getAntoniaAvatar(),
                discordWebhookSendService);

        if (ANTONIA_PATTERN.matcher(msgContent).find()) {
            antoniaCommand.execute(new CommandEvent(event));
            return;
        }
    }

}
