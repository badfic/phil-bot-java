package com.badfic.philbot.listeners.keanu;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.KeanuMarker;
import com.badfic.philbot.data.keanu.KeanuResponsesConfig;
import com.badfic.philbot.data.keanu.KeanuResponsesConfigRepository;
import com.badfic.philbot.listeners.BasicResponsesBot;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class KeanuCommand extends BasicResponsesBot<KeanuResponsesConfig> implements KeanuMarker {

    private static final Pattern PUPPY_PATTERN = Pattern.compile("\\b(puppy|puppies|pupper|doggo|doge)\\b", Pattern.CASE_INSENSITIVE);
    private static final String HELLO_GIF = "https://gfycat.com/consciousambitiousantipodesgreenparakeet-squarepants-tumbelweed-spongebob-morning-reeves";
    private static final String PUPPIES_GIF = "https://media.giphy.com/media/8rFNes6jllJQRnHTsF/giphy.gif";
    private static final Set<String> GOOD_MORNING_GIFS = new HashSet<>(Arrays.asList(
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
    ));

    @Resource(name = "keanuJda")
    @Lazy
    private JDA keanuJda;

    @Autowired
    public KeanuCommand(ObjectMapper objectMapper, BaseConfig baseConfig, KeanuResponsesConfigRepository keanuResponsesConfigRepository,
                        CloseableHttpClient gfycatClient) throws Exception {
        super(baseConfig, keanuResponsesConfigRepository, gfycatClient, objectMapper, "keanu",
                "keanu-kidFriendlyConfig.json", "keanu-nsfwConfig.json", KeanuResponsesConfig::new);
    }

    @Scheduled(cron = "0 0 17 * * ?", zone = "GMT")
    public void goodMorning() {
        TextChannel general = keanuJda.getTextChannelsByName("general", false).get(0);
        general.sendMessage(pickRandom(GOOD_MORNING_GIFS)).queue();
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
            responses = maybeGetGif("keanu+reeves").map(Collections::singleton).orElseGet(() -> responsesConfig.getNsfwConfig().getResponses());
        } else {
            return Optional.empty();
        }

        if (StringUtils.containsIgnoreCase(msgContent, "hello")) {
            event.getChannel().sendMessage(HELLO_GIF).queue();
            return Optional.empty();
        }

        if (PUPPY_PATTERN.matcher(msgContent).find()) {
            event.getChannel().sendMessage(PUPPIES_GIF).queue();
            return Optional.empty();
        }

        if (EmojiManager.containsEmoji(msgContent)) {
            Collection<Emoji> allEmoji = EmojiManager.getAll();
            Emoji emoji = pickRandom(allEmoji);
            return Optional.of(emoji.getUnicode());
        }

        return Optional.of(pickRandom(responses));
    }

}
