package com.badfic.philbot.listeners.behrad;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.behrad.BehradResponsesConfig;
import com.badfic.philbot.data.behrad.BehradResponsesConfigRepository;
import com.badfic.philbot.listeners.BasicResponsesBot;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BehradCommand extends BasicResponsesBot<BehradResponsesConfig> implements PhilMarker {

    private static final Set<String> SHAYAN_IMGS = ImmutableSet.of(
            "https://cdn.discordapp.com/attachments/323666308107599872/750575009650573332/unknown-15.png",
            "https://cdn.discordapp.com/attachments/323666308107599872/750575009885454356/unknown-21.png",
            "https://cdn.discordapp.com/attachments/323666308107599872/750575010221129889/unknown-17.png",
            "https://cdn.discordapp.com/attachments/323666308107599872/750575275783487598/MV5BMGEyZDE2YmYtNjRhNi00MzQwLThjNjItM2E5YjVjOTI3MDMwXkEyXkFqcGdeQXVyMTAzMjM0MjE0.png",
            "https://cdn.discordapp.com/attachments/323666308107599872/750575276026626129/MV5BYTRjOGE2OWUtMjk2MS00MGFkLTg2YjEtYmNjZDRjODAzNWI4XkEyXkFqcGdeQXVyMTAzMjM0MjE0.png"
    );
    private static final Set<String> SLOTH_GIFS = ImmutableSet.of(
            "https://gfycat.com/cooperativeglamoroushoneybee-animals-sloth-cute",
            "https://gfycat.com/ornateplumpicterinewarbler-animals-sloth-baby",
            "https://gfycat.com/femaleastonishingflies-relax",
            "https://gfycat.com/focusedsphericalfulmar-animals-sloth",
            "https://gfycat.com/gaseousfrightenedavocet-animals-sloth-costa-rica-matty-baby",
            "https://gfycat.com/jollyunpleasantcirriped-animals-sloth",
            "https://gfycat.com/definitivetangiblefreshwatereel-sloth-animal",
            "https://gfycat.com/flatgraveharborporpoise-sloth",
            "https://gfycat.com/accomplishedinstructivefish"
    );
    private static final Pattern NAME_PATTERN = Constants.compileWords("shayan|sobhian");
    private static final Pattern WEED_PATTERN = Constants.compileWords("marijuana|weed|420|stoned|high|stoner|kush");
    private static final Pattern SLOTH_PATTERN = Constants.compileWords("sup sloth");

    @Resource(name = "behradJda")
    @Lazy
    private JDA behradJda;

    @Autowired
    public BehradCommand(ObjectMapper objectMapper, BehradResponsesConfigRepository behradResponsesConfigRepository) throws Exception {
        super(behradResponsesConfigRepository, objectMapper, "behrad",
                "behrad-kidFriendlyConfig.json", "behrad-nsfwConfig.json", BehradResponsesConfig::new);
    }

    @Scheduled(cron = "0 2 17 * * WED", zone = "GMT")
    public void goodMorning() {
        TextChannel general = behradJda.getTextChannelsByName("general", false).get(0);
        general.sendMessage("https://tenor.com/view/itis-wednesdaymy-dudes-wednesday-viralyoutube-gif-18012295").queue();
    }

    @Override
    protected Optional<String> getResponse(CommandEvent event, BehradResponsesConfig responsesConfig) {
        String msgContent = event.getMessage().getContentRaw();
        String channelName = event.getChannel().getName();
        Set<String> responses;
        if (responsesConfig.getSfwConfig().getChannels().contains(channelName)) {
            responses = responsesConfig.getSfwConfig().getResponses();
        } else if (responsesConfig.getNsfwConfig().getChannels().contains(channelName)) {
            if (NAME_PATTERN.matcher(msgContent).find()) {
                event.getJDA().getGuilds().get(0).getTextChannelById(event.getChannel().getId())
                        .sendMessage(Constants.pickRandom(SHAYAN_IMGS)).queue();
                return Optional.empty();
            }

            responses = responsesConfig.getNsfwConfig().getResponses();
        } else {
            return Optional.empty();
        }

        if (SLOTH_PATTERN.matcher(msgContent).find()) {
            event.getJDA().getGuilds().get(0).getTextChannelById(event.getChannel().getId())
                    .sendMessage(Constants.pickRandom(SLOTH_GIFS)).queue();
            return Optional.empty();
        }

        if (WEED_PATTERN.matcher(msgContent).find()) {
            MessageEmbed messageEmbed = Constants.simpleEmbed(null, null,
                    "https://cdn.discordapp.com/attachments/323666308107599872/750575541266022410/cfff6b4479a51d245d26cd82e16d4f3f.png",
                    Constants.SWAMP_GREEN);
            Message message = new MessageBuilder(messageEmbed)
                    .setContent("420 whatcha smokin?")
                    .build();
            event.getJDA().getGuilds().get(0).getTextChannelById(event.getChannel().getId())
                    .sendMessage(message).queue();
            return Optional.empty();
        }

        List<Emote> emotes = event.getMessage().getEmotes();
        if (CollectionUtils.isNotEmpty(emotes)) {
            return Optional.of(emotes.get(0).getAsMention());
        }

        return Optional.of(Constants.pickRandom(responses));
    }

}
