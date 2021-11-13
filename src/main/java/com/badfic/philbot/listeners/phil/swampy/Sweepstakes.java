package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Sweepstakes extends BaseSwampy {

    public Sweepstakes() {
        name = "sweepstakes";
        help = "!!sweepstakes\nTrigger a sweepstakes for the given role, you must specify a role\nExample: !!sweepstakes @18+";
        ownerCommand = true;
    }

    @Override
    public void execute(CommandEvent event) {
        List<Role> mentionedRoles = event.getMessage().getMentionedRoles();

        Role role = null;
        if (CollectionUtils.size(mentionedRoles) == 1) {
            role = mentionedRoles.get(0);
        } else {
            String trim = event.getArgs().trim();
            List<Role> rolesByName = event.getJDA().getRolesByName(trim, true);

            if (CollectionUtils.isNotEmpty(rolesByName)) {
                role = rolesByName.get(0);
            }
        }

        if (role == null) {
            event.replyError("Please mention a role");
            return;
        }

        doSweepstakes(role.getName());
    }

    @Scheduled(cron = "${swampy.schedule.events.sweepstakes18}", zone = "${swampy.schedule.timezone}")
    public void sweepstakes18() {
        doSweepstakes(Constants.EIGHTEEN_PLUS_ROLE);
    }

    @Scheduled(cron = "${swampy.schedule.events.sweepstakeschaos}", zone = "${swampy.schedule.timezone}")
    public void sweepstakesChaos() {
        doSweepstakes(Constants.CHAOS_CHILDREN_ROLE);
    }

    private void doSweepstakes(String role) {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        if (swampyGamesConfig == null) {
            return;
        }

        Guild guild = philJda.getGuilds().get(0);
        List<DiscordUser> allUsers = discordUserRepository.findAll();
        Collections.shuffle(allUsers);

        Member member = null;
        for (DiscordUser winningUser : allUsers) {
            try {
                Member memberById = guild.getMemberById(winningUser.getId());
                if (memberById != null
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
                    .sendMessageEmbeds(Constants.simpleEmbed(role + " Sweepstakes Results", "Unable to choose a winner, nobody wins"))
                    .queue();
            return;
        }

        final Member finalMember = member;

        givePointsToMember(swampyGamesConfig.getSweepstakesPoints(), member, PointsStat.SWEEPSTAKES).thenRun(() -> {
            MessageEmbed message = Constants.simpleEmbed(role + " Sweepstakes Results",
                    String.format("Congratulations %s you won today's sweepstakes worth %s points!",
                            finalMember.getAsMention(), NumberFormat.getIntegerInstance().format(swampyGamesConfig.getSweepstakesPoints())),
                    swampyGamesConfig.getSweepstakesImg(), null, null, finalMember.getUser().getEffectiveAvatarUrl());

            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessageEmbeds(message)
                    .queue();
        });
    }

}
