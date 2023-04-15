package com.badfic.philbot.listeners.behrad;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.service.BaseService;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BehradMessageListener extends BaseService {

    private static final Pattern BEHRAD_PATTERN = Constants.compileWords("behrad|shayan|sobhian|marijuana|weed|420|stoned|stoner|kush|hey b|sup sloth");
    private static final String[] SHAYAN_IMGS = {
            "https://cdn.discordapp.com/attachments/323666308107599872/750575275783487598/MV5BMGEyZDE2YmYtNjRhNi00MzQwLThjNjItM2E5YjVjOTI3MDMwXkEyXkFqcGdeQXVyMTAzMjM0MjE0.png",
            "https://cdn.discordapp.com/attachments/323666308107599872/750575276026626129/MV5BYTRjOGE2OWUtMjk2MS00MGFkLTg2YjEtYmNjZDRjODAzNWI4XkEyXkFqcGdeQXVyMTAzMjM0MjE0.png"
    };
    private static final String[] SLOTH_GIFS = {
            "https://gfycat.com/cooperativeglamoroushoneybee-animals-sloth-cute",
            "https://gfycat.com/ornateplumpicterinewarbler-animals-sloth-baby",
            "https://gfycat.com/femaleastonishingflies-relax",
            "https://gfycat.com/focusedsphericalfulmar-animals-sloth",
            "https://gfycat.com/gaseousfrightenedavocet-animals-sloth-costa-rica-matty-baby",
            "https://gfycat.com/jollyunpleasantcirriped-animals-sloth",
            "https://gfycat.com/definitivetangiblefreshwatereel-sloth-animal",
            "https://gfycat.com/flatgraveharborporpoise-sloth",
            "https://gfycat.com/accomplishedinstructivefish"
    };
    private static final Pattern WEED_PATTERN = Constants.compileWords("marijuana|weed|420|stoned|high|stoner|kush");
    private static final Map<String, List<Pair<Pattern, String>>> USER_TRIGGER_WORDS = Map.of(
            "323520695550083074", List.of(ImmutablePair.of(Constants.compileWords("child"), "Yes father?")));

    private final BehradCommand behradCommand;

    @Scheduled(cron = "${swampy.schedule.behrad.humpday}", zone = "${swampy.schedule.timezone}")
    public void humpDay() {
        TextChannel general = philJda.getTextChannelsByName("general", false).get(0);
        SwampyGamesConfig swampyGamesConfig = swampyGamesConfigDao.get();

        discordWebhookSendService.sendMessage(general.getIdLong(), swampyGamesConfig.getBehradNickname(), swampyGamesConfig.getBehradAvatar(),
                "https://tenor.com/view/itis-wednesdaymy-dudes-wednesday-viralyoutube-gif-18012295");
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String msgContent = event.getMessage().getContentRaw().toLowerCase(Locale.ENGLISH);

        long channelId = event.getMessage().getChannel().getIdLong();
        SwampyGamesConfig swampyGamesConfig = swampyGamesConfigDao.get();

        if (msgContent.contains("i'm gay")) {
            discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getBehradNickname(), swampyGamesConfig.getBehradAvatar(), "same");
            return;
        }

        if (msgContent.contains("salsa")) {
            discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getBehradNickname(), swampyGamesConfig.getBehradAvatar(),
                    "you know the rule about salsa ( ͡° ͜ʖ ͡°)");
            return;
        }

        if (msgContent.contains("sup sloth")) {
            discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getBehradNickname(), swampyGamesConfig.getBehradAvatar(),
                    Constants.pickRandom(SLOTH_GIFS));
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

        Constants.checkUserTriggerWords(event, USER_TRIGGER_WORDS, swampyGamesConfig.getBehradNickname(), swampyGamesConfig.getBehradAvatar(),
                discordWebhookSendService);

        if (BEHRAD_PATTERN.matcher(msgContent).find()) {
            behradCommand.execute(new CommandEvent(event, Constants.PREFIX, null, null));
            return;
        }
    }

}
