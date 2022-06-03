package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Parade extends BaseSwampy {

    @Value("${swampy.schedule.timezone}")
    private String swampyTimezone;

    private static final Map<Integer, ParadeItem> PARADE_ITEMS = new HashMap<>();

    public Parade() {
        name = "parade";
        requiredRole = Constants.ADMIN_ROLE;
    }

    @PostConstruct
    public void init() throws Exception {
        List<ParadeItem> paradeItemsArray =
                Arrays.asList(objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("parade.json"), ParadeItem[].class));

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
