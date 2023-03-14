package com.badfic.philbot.listeners.keanu;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.keanu.KeanuResponsesConfig;
import com.badfic.philbot.data.keanu.KeanuResponsesConfigRepository;
import com.badfic.philbot.listeners.BasicResponsesBot;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.jagrosh.jdautilities.command.CommandEvent;
import io.honeybadger.reporter.HoneybadgerReporter;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class KeanuCommand extends BasicResponsesBot<KeanuResponsesConfig> {

    private static final Pattern PUPPY_PATTERN = Constants.compileWords("puppy|puppies|pupper|doggo|doge");
    private static final String HELLO_GIF = "https://gfycat.com/consciousambitiousantipodesgreenparakeet-squarepants-tumbelweed-spongebob-morning-reeves";
    private static final String PUPPIES_GIF = "https://media.giphy.com/media/8rFNes6jllJQRnHTsF/giphy.gif";
    private static final Set<String> GOOD_MORNING_GIFS = ImmutableSet.of(
            HELLO_GIF,
            PUPPIES_GIF,
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
    );
    private static final Set<String> FIGHT_GIFS = ImmutableSet.of(
            "https://cdn.discordapp.com/attachments/707453916882665552/880260097752838185/john-wick-5.gif",
            "https://cdn.discordapp.com/attachments/707453916882665552/880260100105830440/john-wick-4.gif",
            "https://cdn.discordapp.com/attachments/707453916882665552/880260103830380574/john-wick-3.gif",
            "https://cdn.discordapp.com/attachments/707453916882665552/880260105562652672/john-wick-2.gif",
            "https://cdn.discordapp.com/attachments/707453916882665552/880260108968394762/john-wick-1.gif"
    );

    @Autowired
    @Qualifier("keanuJda")
    @Lazy
    private JDA keanuJda;

    @Autowired
    public KeanuCommand(ObjectMapper objectMapper, HoneybadgerReporter honeybadgerReporter, KeanuResponsesConfigRepository keanuResponsesConfigRepository)
            throws Exception {
        super(keanuResponsesConfigRepository, objectMapper, honeybadgerReporter, "keanu", "keanu-kidFriendlyConfig.json", "keanu-nsfwConfig.json",
                KeanuResponsesConfig::new);
    }

    @Scheduled(cron = "${swampy.schedule.keanu.goodmorning}", zone = "${swampy.schedule.timezone}")
    public void goodMorning() {
        TextChannel general = keanuJda.getTextChannelsByName("general", false).get(0);
        general.sendMessage(Constants.pickRandom(GOOD_MORNING_GIFS)).queue();
    }

    @Override
    protected Optional<String> getResponse(CommandEvent event, KeanuResponsesConfig responsesConfig) {
        String msgContent = event.getMessage().getContentRaw();
        String channelName = event.getChannel().getName();
        Set<String> responses;
        if (responsesConfig.getSfwConfig().getChannels().contains(channelName)) {
            if (ThreadLocalRandom.current().nextInt(100) < 28) {
                responses = GOOD_MORNING_GIFS;
            } else {
                responses = responsesConfig.getSfwConfig().getResponses();
            }
        } else if (responsesConfig.getNsfwConfig().getChannels().contains(channelName)) {
            responses = responsesConfig.getNsfwConfig().getResponses();
        } else {
            return Optional.empty();
        }

        if (StringUtils.containsIgnoreCase(msgContent, "hello")) {
            event.getJDA().getTextChannelById(event.getChannel().getId())
                    .sendMessage(HELLO_GIF).queue();
            return Optional.empty();
        }

        if (StringUtils.containsIgnoreCase(msgContent, "fight") || StringUtils.containsIgnoreCase(msgContent, "kill")) {
            event.getJDA().getTextChannelById(event.getChannel().getId())
                    .sendMessage(Constants.pickRandom(FIGHT_GIFS)).queue();
            return Optional.empty();
        }

        if (PUPPY_PATTERN.matcher(msgContent).find()) {
            event.getJDA().getTextChannelById(event.getChannel().getId())
                    .sendMessage(PUPPIES_GIF).queue();
            return Optional.empty();
        }

        return Optional.of(Constants.pickRandom(responses));
    }

}
