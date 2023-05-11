package com.badfic.philbot.commands.slash;

import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.data.SwampyGamesConfig;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Service;

@Service
public class DownvoteSlashCommand extends BaseSlashCommand {

    public DownvoteSlashCommand() {
        name = "downvote";
        options = List.of(new OptionData(OptionType.MENTIONABLE, "user", "User to downvote", true, false));
        help = "Downvote somebody!";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        CompletableFuture<InteractionHook> interactionHook = event.deferReply().submit();

        Member member = event.getMember();
        Member mentionedMember = event.getOption("user").getAsMember();

        if (isNotParticipating(mentionedMember)) {
            replyToInteractionHook(event, interactionHook, mentionedMember.getEffectiveName() + " is not participating in the swampys");
            return;
        }

        if (member.getId().equalsIgnoreCase(mentionedMember.getId())) {
            replyToInteractionHook(event, interactionHook, "You can't downvote yourself");
            return;
        }

        DiscordUser discordUser = getDiscordUserByMember(member);

        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextVoteTime = discordUser.getLastVote().plus(swampyGamesConfig.getDownvoteTimeoutMinutes(), ChronoUnit.MINUTES);
        if (now.isBefore(nextVoteTime)) {
            Duration duration = Duration.between(now, nextVoteTime);

            if (duration.getSeconds() < 60) {
                replyToInteractionHook(event, interactionHook, "You must wait " + (duration.getSeconds() + 1) + " seconds before up/down-voting again");
            } else {
                replyToInteractionHook(event, interactionHook, "You must wait " + (duration.toMinutes() + 1) + " minutes before up/down-voting again");
            }
            return;
        }
        discordUser.setLastVote(now);

        if (mentionedMember.getUser().isBot()) {
            takePointsFromMember(swampyGamesConfig.getDownvotePointsFromDownvotee(), member, discordUser, PointsStat.DOWNVOTED);
            givePointsToMember(swampyGamesConfig.getDownvotePointsToDownvoter(), mentionedMember, PointsStat.DOWNVOTER);

            replyToInteractionHook(event, interactionHook, "Successfully downvoted " + member.getEffectiveName() + " \uD83D\uDE08");
        } else {
            takePointsFromMember(swampyGamesConfig.getDownvotePointsFromDownvotee(), mentionedMember, PointsStat.DOWNVOTED);
            givePointsToMember(swampyGamesConfig.getDownvotePointsToDownvoter(), member, discordUser, PointsStat.DOWNVOTER);

            replyToInteractionHook(event, interactionHook, "Successfully downvoted " + mentionedMember.getEffectiveName());
        }
    }

}
