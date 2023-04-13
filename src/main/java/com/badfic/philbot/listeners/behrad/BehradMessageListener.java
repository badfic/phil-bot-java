package com.badfic.philbot.listeners.behrad;

import com.badfic.philbot.config.Constants;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class BehradMessageListener {

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
    private final JDA behradJda;

    public BehradMessageListener(BehradCommand behradCommand, @Qualifier("behradJda") JDA behradJda) {
        this.behradCommand = behradCommand;
        this.behradJda = behradJda;
    }

    @Scheduled(cron = "${swampy.schedule.behrad.humpday}", zone = "${swampy.schedule.timezone}")
    public void wednesday() {
        TextChannel general = behradJda.getTextChannelsByName("general", false).get(0);
        general.sendMessage("https://tenor.com/view/itis-wednesdaymy-dudes-wednesday-viralyoutube-gif-18012295").queue();
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String msgContent = event.getMessage().getContentRaw().toLowerCase(Locale.ENGLISH);

        if (StringUtils.isBlank(msgContent) || msgContent.startsWith(Constants.PREFIX) || event.getAuthor().isBot()) {
            return;
        }

        long channelId = event.getMessage().getChannel().getIdLong();

        if (msgContent.contains("i'm gay")) {
            event.getJDA().getTextChannelById(channelId).sendMessage("same").queue();
            return;
        }

        if (msgContent.contains("salsa")) {
            event.getJDA().getTextChannelById(channelId).sendMessage("you know the rule about salsa ( ͡° ͜ʖ ͡°)").queue();
            return;
        }

        if (msgContent.contains("sup sloth")) {
            event.getJDA().getTextChannelById(channelId)
                    .sendMessage(Constants.pickRandom(SLOTH_GIFS)).queue();
            return;
        }

        if (msgContent.contains("shayan") || msgContent.contains("sobhian")) {
            event.getJDA().getTextChannelById(channelId)
                    .sendMessage(Constants.pickRandom(SHAYAN_IMGS)).queue();
            return;
        }

        if (WEED_PATTERN.matcher(msgContent).find()) {
            MessageEmbed messageEmbed = Constants.simpleEmbed(null, null,
                    "https://cdn.discordapp.com/attachments/323666308107599872/750575541266022410/cfff6b4479a51d245d26cd82e16d4f3f.png",
                    Constants.SWAMP_GREEN);
            MessageCreateData message = new MessageCreateBuilder()
                    .setEmbeds(messageEmbed)
                    .setContent("420 whatcha smokin?")
                    .build();
            event.getJDA().getTextChannelById(channelId)
                    .sendMessage(message).queue();
            return;
        }

        Constants.checkUserTriggerWords(event, USER_TRIGGER_WORDS);

        if (BEHRAD_PATTERN.matcher(msgContent).find()) {
            behradCommand.execute(new CommandEvent(event, Constants.PREFIX, null, null));
            return;
        }
    }

}
