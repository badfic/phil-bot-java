package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.lang3.mutable.MutableLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BlackFriday extends BaseSwampy implements PhilMarker {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final long POINTS = 250L;
    private static final String IMAGE = "https://cdn.discordapp.com/attachments/752665380182425677/781712460122030090/bf-meme.png";
    private static final String THUMBNAIL = "https://cdn.discordapp.com/attachments/707453916882665552/781771531662655498/sheraton-macao-hotel.png";

    public BlackFriday() {
        name = "blackFriday";
        ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        event.replyError("blackFriday can't be triggered manually.");
    }

    @Scheduled(cron = "0 59 * * * ?", zone = "GMT")
    private void blackFriday() {
        MutableLong totalPoints = new MutableLong(0);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        StringBuilder description = new StringBuilder("Shrek gave " + POINTS + " points to every swampling who was active in the last hour\n\n");

        discordUserRepository.findAll()
                .stream()
                .sorted((u1, u2) -> Long.compare(u2.getXp(), u1.getXp()))
                .filter(u -> u.getXp() > SWEEP_OR_TAX_WINNER_ORGANIC_POINT_THRESHOLD && u.getUpdateTime().isAfter(LocalDateTime.now().minusHours(1)))
                .forEachOrdered(user -> {
                    try {
                        Member memberById = philJda.getGuilds().get(0).getMemberById(user.getId());
                        if (memberById != null) {

                            futures.add(givePointsToMember(POINTS, memberById));
                            totalPoints.add(POINTS);
                            description
                                    .append("<@!")
                                    .append(user.getId())
                                    .append(">\n");
                        }
                    } catch (Exception e) {
                        logger.error("Failed to black friday user [id={}]", user.getId(), e);
                        honeybadgerReporter.reportError(e, "Failed to black friday user: " + user.getId());
                    }
                });

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).stream().findAny().ifPresent(textChannel -> {
                textChannel.sendMessage(Constants.simpleEmbed("\uD83D\uDED2 SWAMP FRIDAY \uD83D\uDECD️", description.toString(), IMAGE,
                        "Shrek gave a total of " + NumberFormat.getIntegerInstance().format(totalPoints.getValue()) + " points to the swamplings",
                        Constants.SWAMP_GREEN, THUMBNAIL)).queue();
            });
        });
    }
}
