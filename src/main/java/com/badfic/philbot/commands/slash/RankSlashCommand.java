package com.badfic.philbot.commands.slash;

import com.badfic.philbot.commands.Rank;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Service;

@Service
public class RankSlashCommand extends BaseSlashCommand {

    public RankSlashCommand() {
        name = "rank";
        options = List.of(new OptionData(OptionType.MENTIONABLE, "user", "user to show rank of"));
        help = "Show your swampy level (rank)";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Member member = event.getMember();

        OptionMapping userOption = event.getOption("user");
        if (Objects.nonNull(userOption)) {
            member = userOption.getAsMember();
        }

        if (isNotParticipating(member)) {
            event.replyEmbeds(Constants.simpleEmbed("Your Rank", "Is not participating in the games")).queue();
            return;
        }

        DiscordUser user = getDiscordUserByMember(member);

        List<Rank> allRanks = Rank.getAllRanks();
        Rank rank = Rank.byXp(user.getXp());
        Rank nextRank = (rank.getOrdinal() >= allRanks.size() - 1) ? rank : allRanks.get(rank.getOrdinal() + 1);

        String description = rank.getRankUpMessage().replace("<name>", member.getAsMention()).replace("<rolename>", rank.getRoleName()) +
                "\n\nYou have " + NumberFormat.getIntegerInstance().format(user.getXp()) + " total points.\n\n" +
                "The next level is level " +
                (rank == nextRank
                        ? " LOL NVM YOU'RE THE TOP LEVEL."
                        : nextRank.getLevel() + ": " + nextRank.getRoleName() + ".") +
                "\n You have " + (rank == nextRank
                ? "LOL NVM YOU'RE THE TOP LEVEL"
                : NumberFormat.getIntegerInstance().format((nextRank.getLevel() * Rank.LVL_MULTIPLIER) - user.getXp()))
                + " points to go.\n\nBest of Luck in the Swampys!";

        MessageEmbed messageEmbed = Constants.simpleEmbed("Level " + rank.getLevel() + ": " + rank.getRoleName(),
                description,
                rank.getRankUpImage(),
                rank.getColor());

        event.replyEmbeds(messageEmbed).queue();
    }
}
