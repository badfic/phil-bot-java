package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.jagrosh.jdautilities.command.CommandEvent;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Parade extends BaseSwampy {

    @Value("${swampy.schedule.timezone}")
    private String swampyTimezone;

    @Autowired
    @Qualifier("antoniaJda")
    @Lazy
    private JDA antoniaJda;

    private static final Map<Integer, ParadeItem> PARADE_ITEMS = new HashMap<>();

    public Parade() {
        name = "parade";
        requiredRole = Constants.ADMIN_ROLE;
    }

    @PostConstruct
    public void init() throws Exception {
        ParadeItem[] paradeItemsArray = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("parade.json"), ParadeItem[].class);

        for (ParadeItem paradeItem : paradeItemsArray) {
            PARADE_ITEMS.put(paradeItem.day, paradeItem);
        }
    }

    @Scheduled(cron = "${swampy.schedule.events.parade}", zone = "${swampy.schedule.timezone}")
    public void runParade() {
        LocalDate today = LocalDate.now(ZoneId.of(swampyTimezone));
        int dayOfMonth = today.getDayOfMonth();

        ParadeItem paradeItem = PARADE_ITEMS.get(dayOfMonth);

        if (paradeItem != null) {
            antoniaJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).stream().findAny().ifPresent(swampysChannel -> {
                swampysChannel.sendMessageEmbeds(
                        Constants.simpleEmbedThumbnail(paradeItem.name, paradeItem.description, paradeItem.flag, paradeItem.thumbnail))
                        .queue();
            });

        }
    }

    @Override
    protected void execute(CommandEvent event) {
        runParade();
    }

    public record ParadeItem(Integer day, String name, String description, String thumbnail, String flag) {}
}
