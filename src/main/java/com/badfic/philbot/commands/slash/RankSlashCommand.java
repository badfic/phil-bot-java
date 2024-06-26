package com.badfic.philbot.commands.slash;

import com.badfic.philbot.commands.Rank;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
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
    public void execute(SlashCommandInteractionEvent event) {
        CompletableFuture<InteractionHook> interactionHook = event.deferReply().submit();
        Member member = event.getMember();

        OptionMapping userOption = event.getOption("user");
        if (Objects.nonNull(userOption)) {
            member = userOption.getAsMember();
        }

        if (isNotParticipating(member)) {
            replyToInteractionHook(event, interactionHook, Constants.simpleEmbed("Your Rank", "Is not participating in the games"));
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
                : NumberFormat.getIntegerInstance().format(Rank.xpRequiredForLevel(user.getXp(), nextRank.getLevel())))
                + " points to go.\n\nBest of Luck in the Swampys!";

        MessageEmbed messageEmbed = Constants.simpleEmbed("Level " + rank.getLevel() + " | " + rank.getRoleName(),
                description,
                null,
                null,
                rank.getColor(),
                rank.getRankUpImage());

        replyToInteractionHook(event, interactionHook, messageEmbed);
    }
}
