package com.badfic.philbot.commands.slash;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.PointsStat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
class UpvoteSlashCommand extends BaseSlashCommand {
    UpvoteSlashCommand() {
        name = "upvote";
        options = List.of(new OptionData(OptionType.STRING, "user", "User(s) to upvote", true, false));
        help = "Upvote somebody(s)!";
    }

    @Override
    public void execute(final SlashCommandInteractionEvent event) {
        final var interactionHook = event.deferReply().submit();
        final var member = event.getMember();
        final var option = event.getOption("user");

        if (Objects.isNull(option)) {
            replyToInteractionHook(event, interactionHook, "You must mention at least one user to upvote");
            return;
        }

        final var mentionedMembers = option.getMentions().getMembers();

        final var description = new StringBuilder();
        final var eligibleMembers = mentionedMembers.stream().filter(mentionedMember -> {
            if (isNotParticipating(mentionedMember)) {
                description.append(mentionedMember.getEffectiveName())
                        .append(" is not participating in the swampys\n");
                return false;
            }

            if (member.getId().equalsIgnoreCase(mentionedMember.getId())) {
                description.append("You can't upvote yourself\n");
                return false;
            }

            return true;
        }).toList();

        if (CollectionUtils.isEmpty(eligibleMembers)) {
            replyToInteractionHook(event, interactionHook, "Please mention at least one eligible user to upvote. Example `!!swampy up @Santiago`");
            return;
        }

        final var discordUser = getDiscordUserByMember(member);

        final var swampyGamesConfig = getSwampyGamesConfig();

        final var now = LocalDateTime.now();
        final var nextVoteTime = discordUser.getLastVote().plus(swampyGamesConfig.getUpvoteTimeoutMinutes(), ChronoUnit.MINUTES);
        if (now.isBefore(nextVoteTime)) {
            final var duration = Duration.between(now, nextVoteTime);

            if (duration.getSeconds() < 60) {
                replyToInteractionHook(event, interactionHook, "You must wait " + (duration.getSeconds() + 1) + " seconds before up/down-voting again");
            } else {
                replyToInteractionHook(event, interactionHook, "You must wait " + (duration.toMinutes() + 1) + " minutes before up/down-voting again");
            }
            return;
        }
        discordUser.setLastVote(now);

        givePointsToMember(swampyGamesConfig.getUpvotePointsToUpvoter(), member, discordUser, PointsStat.UPVOTER);

        for (final var mentionedMember : eligibleMembers) {
            givePointsToMember(swampyGamesConfig.getUpvotePointsToUpvotee(), mentionedMember, PointsStat.UPVOTED);
            description.append(mentionedMember.getEffectiveName()).append('\n');
        }

        replyToInteractionHook(event, interactionHook, Constants.simpleEmbed("Successfully Upvoted...", description.toString()));
    }
}
