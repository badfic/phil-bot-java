package com.badfic.philbot.commands.slash;

import com.badfic.philbot.commands.bang.SwampyCommand;
import com.badfic.philbot.config.Constants;
import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Service;

@Service
@Slf4j
class LeaderboardSlashCommand extends BaseSlashCommand {
    LeaderboardSlashCommand() {
        name = "leaderboard";
        options = List.of(new OptionData(OptionType.STRING, "type", "Bastards or Chaos Children Leaderboard", true, true));
        help = "Show the Swampys leaderboard for either Bastards or Chaos Children";
    }

    @Override
    public void execute(final SlashCommandInteractionEvent event) {
        final var interactionHook = event.deferReply().submit();
        final var swampyUsers = discordUserRepository.findAll();
        final var type = event.getOption("type");
        final var guild = event.getGuild();

        if (Objects.isNull(type)) {
            replyToInteractionHook(event, interactionHook, "You must pick a leaderboard type");
            return;
        }

        final var place = new AtomicInteger(0);
        final var description = new StringBuilder();
        swampyUsers.stream().filter(u -> {
            try {
                final var member = Objects.requireNonNull(guild.getMemberById(u.getId()));

                return member.getRoles()
                        .stream()
                        .anyMatch(r -> r.getName().equalsIgnoreCase(type.getAsLong() == 1 ? Constants.EIGHTEEN_PLUS_ROLE : Constants.CHAOS_CHILDREN_ROLE));
            } catch (final Exception e) {
                log.error("Unable to lookup user [id={}] for leaderboard", u.getId(), e);
                return false;
            }
        }).sorted((u1, u2) -> Long.compare(u2.getXp(), u1.getXp())).limit(10).forEachOrdered(swampyUser -> {
            description.append(SwampyCommand.LEADERBOARD_MEDALS[place.getAndIncrement()])
                    .append(": <@")
                    .append(swampyUser.getId())
                    .append("> - ")
                    .append(NumberFormat.getIntegerInstance().format(swampyUser.getXp()))
                    .append('\n');
        });

        final var messageEmbed = Constants.simpleEmbed(type.getAsLong() == 1 ? "Bastard Leaderboard" : "Chaos Leaderboard",
                description.toString());

        replyToInteractionHook(event, interactionHook, messageEmbed);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        event.replyChoices(new Command.Choice("bastards", 1L), new Command.Choice("chaos", 0)).queue();
    }
}
