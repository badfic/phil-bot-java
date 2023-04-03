package com.badfic.philbot.commands.slash;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import org.springframework.stereotype.Service;

@Service
public class SwampySlashCommand extends BaseSlashCommand {

    public SwampySlashCommand(LeaderboardSlashCommand leaderboardSlashCommand, UpvoteSlashCommand upvoteSlashCommand,
                              DownvoteSlashCommand downvoteSlashCommand, RankSlashCommand rankSlashCommand, SlotsSlashCommand slotsSlashCommand) {
        name = "swampys";
        children = new SlashCommand[] {leaderboardSlashCommand, upvoteSlashCommand, downvoteSlashCommand, rankSlashCommand, slotsSlashCommand};
        help = "Swampy SlashCommand: view leaderboard, up/downvote users, view rank, play slots";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply("You must pick a sub-command").queue();
    }

}
