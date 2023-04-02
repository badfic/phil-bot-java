package com.badfic.philbot.commands.slash;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
public class UpvoteSlashCommand extends BaseSlashCommand {

    public UpvoteSlashCommand() {
        name = "upvote";
        options = List.of(new OptionData(OptionType.STRING, "user", "User(s) to upvote", true, false));
        help = "Upvote somebody(s)!";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        if (swampyGamesConfig == null) {
            return;
        }

        OptionMapping option = event.getOption("user");

        if (Objects.isNull(option)) {
            event.reply("You must mention at least one user to upvote").queue();
            return;
        }

        List<Member> mentionedMembers = option.getMentions().getMembers();

        StringBuilder description = new StringBuilder();
        List<Member> eligibleMembers = mentionedMembers.stream().filter(mentionedMember -> {
            if (isNotParticipating(mentionedMember)) {
                description.append(mentionedMember.getEffectiveName() + " is not participating in the swampys\n");
                return false;
            }

            if (event.getMember().getId().equalsIgnoreCase(mentionedMember.getId())) {
                description.append("You can't upvote yourself\n");
                return false;
            }

            return true;
        }).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(eligibleMembers)) {
            event.reply("Please mention at least one eligible user to upvote. Example `!!swampy up @Santiago`").queue();
            return;
        }

        DiscordUser discordUser = getDiscordUserByMember(event.getMember());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextVoteTime = discordUser.getLastVote().plus(swampyGamesConfig.getUpvoteTimeoutMinutes(), ChronoUnit.MINUTES);
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

        givePointsToMember(swampyGamesConfig.getUpvotePointsToUpvoter(), event.getMember(), PointsStat.UPVOTER);

        for (Member mentionedMember : eligibleMembers) {
            givePointsToMember(swampyGamesConfig.getUpvotePointsToUpvotee(), mentionedMember, PointsStat.UPVOTED);
            description.append(mentionedMember.getEffectiveName()).append('\n');
        }

        event.replyEmbeds(Constants.simpleEmbed("Successfully Upvoted...", description.toString())).queue();
    }

}
