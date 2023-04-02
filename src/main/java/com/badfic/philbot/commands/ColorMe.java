package com.badfic.philbot.commands;

import com.badfic.philbot.config.Constants;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.awt.Color;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.stereotype.Component;

@Component
public class ColorMe extends BaseCommand {

    public ColorMe() {
        name = "colorme";
        help = "`!!colorme #FFFFFF` to set your username color to the given hex code.\n " +
                "https://htmlcolorcodes.com/color-picker/ if you don't know what hex is.";
    }

    @Override
    protected void execute(CommandEvent event) {
        final Guild guild = event.getGuild();
        final Member member = event.getMember();
        final String userId = member.getUser().getId();

        String colorStr = event.getArgs().trim();

        Color color;
        try {
            color = Color.decode(colorStr);
        } catch (Exception e) {
            event.replyError("Could not parse color: " + colorStr);
            return;
        }

        Optional<Role> optionalRole = Constants.hasRole(member, userId);

        if (optionalRole.isPresent()) {
            Role role = optionalRole.get();

            if (color.equals(role.getColor())) {
                event.replySuccess("Your color is already set to: " + colorStr);
                return;
            }

            role.getManager()
                    .setColor(color)
                    .queue(success -> {
                        event.replySuccess("Your color has been set to: " + colorStr);
                    }, error -> {
                        event.replyError("Failed to set your color to: " + colorStr);
                    });
        } else {
            guild.createRole()
                    .setColor(color)
                    .setName(userId)
                    .setMentionable(false)
                    .setHoisted(false)
                    .submit()
                    .thenCompose(newRole -> {
                        return guild.addRoleToMember(member, newRole).submit();
                    }).whenComplete((success, err) -> {
                        if (err != null) {
                            event.replyError("Failed to set your color to: " + colorStr);
                        } else {
                            event.replySuccess("Your color has been set to: " + colorStr);
                        }
                    });
        }
    }
}
