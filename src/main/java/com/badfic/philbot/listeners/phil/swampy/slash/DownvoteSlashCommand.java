package com.badfic.philbot.listeners.phil.swampy.slash;

import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.badfic.philbot.listeners.phil.swampy.PointsStat;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Service;

@Service
public class DownvoteSlashCommand extends BaseSwampySlash {

    public DownvoteSlashCommand() {
        name = "downvote";
        options = List.of(new OptionData(OptionType.MENTIONABLE, "user", "User to downvote", true, false));
        help = "Downvote somebody!";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Member mentionedMember = event.getOption("user").getAsMember();
        if (isNotParticipating(mentionedMember)) {
            event.reply(mentionedMember.getEffectiveName() + " is not participating in the swampys").queue();
            return;
        }

        if (event.getMember().getId().equalsIgnoreCase(mentionedMember.getId())) {
            event.reply("You can't downvote yourself").queue();
            return;
        }

        DiscordUser discordUser = getDiscordUserByMember(event.getMember());

        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        if (swampyGamesConfig == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextVoteTime = discordUser.getLastVote().plus(swampyGamesConfig.getDownvoteTimeoutMinutes(), ChronoUnit.MINUTES);
        if (now.isBefore(nextVoteTime)) {
            Duration duration = Duration.between(now, nextVoteTime);

            if (duration.getSeconds() < 60) {
                event.reply("You must wait " + (duration.getSeconds() + 1) + " seconds before up/down-voting again").queue();
            } else {
                event.reply("You must wait " + (duration.toMinutes() + 1) + " minutes before up/down-voting again").queue();
            }
            return;
        }
        discordUser.setLastVote(now);
        discordUserRepository.save(discordUser);

        if (mentionedMember.getUser().isBot()) {
            takePointsFromMember(swampyGamesConfig.getDownvotePointsFromDownvotee(), event.getMember(), PointsStat.DOWNVOTED);
            givePointsToMember(swampyGamesConfig.getDownvotePointsToDownvoter(), mentionedMember, PointsStat.DOWNVOTER);
            event.reply("Successfully downvoted " + event.getMember().getEffectiveName() + " \uD83D\uDE08").queue();
        } else {
            takePointsFromMember(swampyGamesConfig.getDownvotePointsFromDownvotee(), mentionedMember, PointsStat.DOWNVOTED);
            givePointsToMember(swampyGamesConfig.getDownvotePointsToDownvoter(), event.getMember(), PointsStat.DOWNVOTER);
            event.reply("Successfully downvoted " + mentionedMember.getEffectiveName()).queue();
        }
    }

}
