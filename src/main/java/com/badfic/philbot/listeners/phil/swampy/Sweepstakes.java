package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.time.LocalDateTime;
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

    private static final String SWEEPSTAKES = "https://cdn.discordapp.com/attachments/752665380182425677/782811729599528960/swampy_monday_every_image_AHHHH.png";

    public Sweepstakes() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "sweepstakes";
        help = "!!sweepstakes\nTrigger a sweepstakes for the given role, you must specify a role\nExample: !!sweepstakes @18+";
    }

    @Override
    public void execute(CommandEvent event) {
        List<Role> mentionedRoles = event.getMessage().getMentionedRoles();

        if (CollectionUtils.size(mentionedRoles) != 1) {
            event.replyError("Please mention a role. Example `!!sweepstakes @18+`");
            return;
        }

        Role role = mentionedRoles.get(0);

        switch (role.getName()) {
            case Constants.EIGHTEEN_PLUS_ROLE:
                doSweepstakes(Constants.EIGHTEEN_PLUS_ROLE);
                break;
            case Constants.CHAOS_CHILDREN_ROLE:
                doSweepstakes(Constants.CHAOS_CHILDREN_ROLE);
                break;
            default:
                event.replyError("Please mention either chaos children role or 18+ role");
                break;
        }
    }

    @Scheduled(cron = "0 3 2 * * ?", zone = "GMT")
    public void sweepstakes18() {
        doSweepstakes(Constants.EIGHTEEN_PLUS_ROLE);
    }

    @Scheduled(cron = "0 7 2 * * ?", zone = "GMT")
    public void sweepstakesChaos() {
        doSweepstakes(Constants.CHAOS_CHILDREN_ROLE);
    }

    private void doSweepstakes(String role) {
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
                        && !memberById.getUser().isBot()
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
                    .sendMessage(Constants.simpleEmbed(role + " Sweepstakes Results", "Unable to choose a winner, nobody wins"))
                    .queue();
            return;
        }

        givePointsToMember(swampyGamesConfig.getSweepstakesPoints(), member);

        MessageEmbed message = Constants.simpleEmbed(role + " Sweepstakes Results",
                String.format("Congratulations %s you won today's sweepstakes worth %d points!", member.getAsMention(), swampyGamesConfig.getSweepstakesPoints()),
                SWEEPSTAKES);

        philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                .get(0)
                .sendMessage(message)
                .queue();
    }

}
