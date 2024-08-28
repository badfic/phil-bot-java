package com.badfic.philbot.commands.slash;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
class AnimeSlashCommand extends BaseSlashCommand {
    private enum Category {
        bite, blush, bored, cry, cuddle, dance, facepalm, feed, handhold, handshake, happy, highfive, hug, kick, kiss, laugh, lurk, nod, nom, nope, pat,
        peck, poke, pout, punch, shoot, shrug, slap, sleep, smile, smug, stare, think, thumbsup, wave, wink, yawn, yeet
    }

    private final Long2ObjectMap<Command.Choice[]> userCategoryCache;

    AnimeSlashCommand() {
        name = "anime";
        options = List.of(new OptionData(OptionType.STRING, "category", "Category", true, true));
        help = "Show anime gif for the given category";
        userCategoryCache = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
    }

    @Override
    public void execute(final SlashCommandInteractionEvent event) {
        userCategoryCache.remove(event.getUser().getIdLong());

        final var interactionHook = event.deferReply().submit();
        final var categoryString = event.getOption("category");

        if (Objects.isNull(categoryString)) {
            replyToInteractionHook(event, interactionHook, "You must pick a category");
            return;
        }

        final var category = EnumUtils.getEnumIgnoreCase(Category.class, categoryString.getAsString());

        try {
            final var result = restTemplate.getForObject("https://nekos.best/api/v2/" + category.name(), AnimeResultList.class).results()[0];
            final var imageBytes = restTemplate.getForObject(result.url(), byte[].class);
            replyToInteractionHook(event, interactionHook, FileUpload.fromData(imageBytes, "anime.gif"));
        } catch (final Exception e) {
            log.error("Failed to find anime gif for [category={}]", category, e);
            replyToInteractionHook(event, interactionHook, "Could not find anime gif \uD83D\uDE14");
        }
    }

    @Override
    public void onAutoComplete(final CommandAutoCompleteInteractionEvent event) {
        final var userId = event.getUser().getIdLong();
        final var choices = userCategoryCache.computeIfAbsent(userId, m -> {
            return Arrays.stream(Category.values())
                    .map(c -> new Command.Choice(c.name(), c.name()))
                    .sorted((a, b) -> randomNumberService.nextInt(-1, 2))
                    .limit(25)
                    .toArray(Command.Choice[]::new);
        });

        event.replyChoices(
                Arrays.stream(choices)
                .filter(a -> StringUtils.startsWithIgnoreCase(a.getName(), event.getOption("category").getAsString()))
                .toArray(Command.Choice[]::new)
        ).queue();
    }

    private record AnimeResultList(AnimeResult[] results){}
    private record AnimeResult(String url){}
}
