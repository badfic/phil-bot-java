package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Sweepstakes extends BaseSwampy implements PhilMarker {

    public Sweepstakes() {
        name = "sweepstakes";
        help = "!!sweepstakes\nTrigger a sweepstakes for the given role, you must specify a role\nExample: !!sweepstakes @18+";
        ownerCommand = true;
    }

    @Override
    public void execute(CommandEvent event) {
        List<Role> mentionedRoles = event.getMessage().getMentionedRoles();

        if (CollectionUtils.size(mentionedRoles) != 1) {
            event.replyError("Please mention a role. Example `!!sweepstakes @18+`");
            return;
        }

        Role role = mentionedRoles.get(0);
        doSweepstakes(role.getName(), role.getName());
    }

    @Scheduled(cron = "0 3 2 * * ?", zone = "GMT")
    public void sweepstakesDry() {
        doSweepstakes(Constants.pickRandom(Arrays.asList(Constants.DRY_BASTARDS_ROLE, Constants.DRY_CINNAMON_ROLE)), "Dry");
    }

    @Scheduled(cron = "0 7 2 * * ?", zone = "GMT")
    public void sweepstakesSwampy() {
        doSweepstakes(Constants.pickRandom(Arrays.asList(Constants.SWAMPY_BASTARDS_ROLE, Constants.SWAMPY_CINNAMON_ROLE)), "Swampy");
    }

    private void doSweepstakes(String role, String dryOrSwampy) {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        if (swampyGamesConfig == null) {
            return;
        }

        List<DiscordUser> allUsers = discordUserRepository.findAll();
        Collections.shuffle(allUsers);

        Member member = null;
        for (DiscordUser winningUser : allUsers) {
            try {
                Member memberById = philJda.getGuilds().get(0).getMemberById(winningUser.getId());
                if (memberById != null
                        && !isNotParticipating(memberById)
                        && hasRole(memberById, role)
                        && winningUser.getXp() > SWEEP_OR_TAX_WINNER_ORGANIC_POINT_THRESHOLD
                        && winningUser.getUpdateTime().isAfter(LocalDateTime.now().minusHours(22))) {
                    member = memberById;
                }
            } catch (Exception ignored) {}
        }

        if (member == null) {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(Constants.simpleEmbed(dryOrSwampy + " Sweepstakes Results", "Unable to choose a winner, nobody wins"))
                    .queue();
            return;
        }

        givePointsToMember(swampyGamesConfig.getSweepstakesPoints(), member);

        MessageEmbed message = Constants.simpleEmbed(dryOrSwampy + " Sweepstakes Results",
                String.format("Congratulations %s you won today's sweepstakes worth %d points!", member.getAsMention(), swampyGamesConfig.getSweepstakesPoints()),
                swampyGamesConfig.getSweepstakesImg());

        philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                .get(0)
                .sendMessage(message)
                .queue();
    }

}
