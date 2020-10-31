package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Stonks extends BaseSwampy implements PhilMarker {

    private static final Set<String> STONKS = new HashSet<>(Arrays.asList(
            "https://cdn.discordapp.com/attachments/741053845098201099/771913890731655188/image0.jpg",
            "https://cdn.discordapp.com/attachments/741053845098201099/771913707403083836/image0.jpg",
            "https://cdn.discordapp.com/attachments/741053845098201099/771914968701599744/image0.jpg",
            "https://cdn.discordapp.com/attachments/741053845098201099/771916094091952138/image0.jpg",
            "https://cdn.discordapp.com/attachments/741053845098201099/771917366660694036/image0.jpg",
            "https://cdn.discordapp.com/attachments/741053845098201099/771917366904750160/image1.jpg"
    ));

    public Stonks() {
        name = "stonks";
        requiredRole = Constants.ADMIN_ROLE;
    }

    @Override
    protected void execute(CommandEvent event) {
        stonks();
    }

    @Scheduled(cron = "0 37 3 * * ?", zone = "GMT")
    public void stonks() {
        philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).stream().findAny().ifPresent(channel -> {
           channel.sendMessage(simpleEmbed("Stonks", "Coming soon to a swamp near you...", pickRandom(STONKS))).queue();
        });
    }
}
