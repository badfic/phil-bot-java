package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.google.common.collect.ImmutableSet;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

@Component
public class BingBong extends BaseSwampy {

    private static final Set<Pair<String, String>> RESPONSES = ImmutableSet.<Pair<String, String>>builder()
            .add(new ImmutablePair<>("FUCK YO LIFE", "https://cdn.discordapp.com/attachments/707453916882665552/917685342985916446/bing-bong-fuck-yo-life.gif"))
            .add(new ImmutablePair<>("What do you wanna tell Joe Byron right now?", "https://cdn.discordapp.com/attachments/707453916882665552/917685371658203146/byron-dinner.gif"))
            .add(new ImmutablePair<>("AYOOO", "https://cdn.discordapp.com/attachments/707453916882665552/917689293680562176/sidetalk-ayo.gif"))
            .add(new ImmutablePair<>("I stole your life!", "https://cdn.discordapp.com/attachments/836326671052963880/919378104592523275/unknown.png"))
            .build();

    public BingBong() {
        name = "bingBong";
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        Pair<String, String> pair = Constants.pickRandom(RESPONSES);
        commandEvent.reply(Constants.simpleEmbed("BING BONG", pair.getLeft(), pair.getRight()));
    }

}
