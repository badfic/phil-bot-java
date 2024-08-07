package com.badfic.philbot.commands.slash;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.Family;
import java.util.List;
import java.util.Objects;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Service;

@Service
class SwampmasFam extends BaseSlashCommand {
    SwampmasFam() {
        name = "swampmasfam";
        options = List.of(new OptionData(OptionType.MENTIONABLE, "user", "user to set image for", true),
                new OptionData(OptionType.STRING, "image", "url of image", true));
        help = "Set the swampmas image for a user";
        requiredRole = Constants.ADMIN_ROLE;
    }

    @Override
    public void execute(final SlashCommandInteractionEvent event) {
        final var interactionHook = event.deferReply().submit();

        final var userOption = event.getOption("user");
        final var imageOption = event.getOption("image");

        final var member = userOption.getAsMember();
        final var image = imageOption.getAsString();

        if (Objects.isNull(member)) {
            replyToInteractionHook(event, interactionHook, "Please supply a valid user for the user parameter");
            return;
        }

        if (!(hasRole(member, Constants.CHAOS_CHILDREN_ROLE) || hasRole(member, Constants.EIGHTEEN_PLUS_ROLE))) {
            replyToInteractionHook(event, interactionHook, member.getEffectiveName() + " is not 18+ or a chaos child, they cannot have a family.");
            return;
        }

        final var discordUser = getUserAndFamily(member);

        if (Constants.isUrl(image) && Constants.urlIsImage(image)) {
            discordUser.getFamily().setImage(image);
            discordUserRepository.save(discordUser);
            replyToInteractionHook(event, interactionHook, "Successfully set image");
        } else {
            replyToInteractionHook(event, interactionHook, "Could not recognize image as a url");
        }
    }

    private DiscordUser getUserAndFamily(final Member member) {
        var discordUser = getDiscordUserByMember(member);
        if (discordUser.getFamily() == null) {
            discordUser.setFamily(new Family());
            discordUser = discordUserRepository.save(discordUser);
        }
        return discordUser;
    }
}
