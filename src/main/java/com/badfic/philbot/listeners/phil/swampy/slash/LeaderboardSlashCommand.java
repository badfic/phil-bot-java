package com.badfic.philbot.listeners.phil.swampy.slash;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.listeners.phil.swampy.SwampyCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LeaderboardSlashCommand extends BaseSwampySlash {
    public LeaderboardSlashCommand() {
        name = "leaderboard";
        options = List.of(new OptionData(OptionType.STRING, "type", "Bastards or Chaos Children Leaderboard", true, true));
        help = "Show the Swampys leaderboard for either Bastards or Chaos Children";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        List<DiscordUser> swampyUsers = discordUserRepository.findAll();
        OptionMapping type = event.getOption("type");

        if (Objects.isNull(type)) {
            event.reply("You must pick a leaderboard type").queue();
            return;
        }

        AtomicInteger place = new AtomicInteger(0);
        StringBuilder description = new StringBuilder();
        swampyUsers.stream().filter(u -> {
            try {
                Member member = Objects.requireNonNull(event.getGuild().getMemberById(u.getId()));

                return member.getRoles()
                        .stream()
                        .anyMatch(r -> r.getName().equalsIgnoreCase(type.getAsLong() == 1 ? Constants.EIGHTEEN_PLUS_ROLE : Constants.CHAOS_CHILDREN_ROLE));
            } catch (Exception e) {
                log.error("Unable to lookup user [id={}] for leaderboard", u.getId(), e);
                honeybadgerReporter.reportError(e, "unable to lookup user for leaderboard: " + u.getId());
                return false;
            }
        }).sorted((u1, u2) -> Long.compare(u2.getXp(), u1.getXp())).limit(10).forEachOrdered(swampyUser -> {
            description.append(SwampyCommand.LEADERBOARD_MEDALS[place.getAndIncrement()])
                    .append(": <@!")
                    .append(swampyUser.getId())
                    .append("> - ")
                    .append(NumberFormat.getIntegerInstance().format(swampyUser.getXp()))
                    .append('\n');
        });

        MessageEmbed messageEmbed = Constants.simpleEmbed(type.getAsLong() == 1 ? "Bastard Leaderboard" : "Chaos Leaderboard",
                description.toString());

        event.replyEmbeds(messageEmbed).queue();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        event.replyChoices(new Command.Choice("bastards", 1L), new Command.Choice("chaos", 0)).queue();
    }
}
