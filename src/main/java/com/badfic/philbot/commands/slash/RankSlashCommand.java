package com.badfic.philbot.commands.slash;

import com.badfic.philbot.commands.Rank;
import com.badfic.philbot.config.Constants;
import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Service;

@Service
class RankSlashCommand extends BaseSlashCommand {
    RankSlashCommand() {
        name = "rank";
        options = List.of(new OptionData(OptionType.MENTIONABLE, "user", "user to show rank of"));
        help = "Show your swampy level (rank)";
    }

    @Override
    public void execute(final SlashCommandInteractionEvent event) {
        final var interactionHook = event.deferReply().submit();
        var member = event.getMember();

        final var userOption = event.getOption("user");
        if (Objects.nonNull(userOption)) {
            member = userOption.getAsMember();
        }

        if (isNotParticipating(member)) {
            replyToInteractionHook(event, interactionHook, Constants.simpleEmbed("Your Rank", "Is not participating in the games"));
            return;
        }

        final var user = getDiscordUserByMember(member);

        final var allRanks = Rank.getAllRanks();
        final var rank = Rank.byXp(user.getXp());
        final var nextRank = (rank.getOrdinal() >= allRanks.size() - 1) ? rank : allRanks.get(rank.getOrdinal() + 1);

        final var description = rank.getRankUpMessage().replace("<name>", member.getAsMention()).replace("<rolename>", rank.getRoleName()) +
                "\n\nYou have " + NumberFormat.getIntegerInstance().format(user.getXp()) + " total points.\n\n" +
                "The next level is level " +
                (rank == nextRank
                        ? " LOL NVM YOU'RE THE TOP LEVEL."
                        : nextRank.getLevel() + ": " + nextRank.getRoleName() + ".") +
                "\n You have " + (rank == nextRank
                ? "LOL NVM YOU'RE THE TOP LEVEL"
                : NumberFormat.getIntegerInstance().format(Rank.xpRequiredForLevel(user.getXp(), nextRank.getLevel())))
                + " points to go.\n\nBest of Luck in the Swampys!";

        final var messageEmbed = Constants.simpleEmbed("Level " + rank.getLevel() + " | " + rank.getRoleName(),
                description,
                null,
                null,
                rank.getColor(),
                rank.getRankUpImage());

        replyToInteractionHook(event, interactionHook, messageEmbed);
    }
}
