package com.badfic.philbot.commands.bang;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.Constants;
import java.awt.Color;
import org.springframework.stereotype.Component;

@Component
class ColorMe extends BaseBangCommand {
    ColorMe() {
        name = "colorme";
        help = "`!!colorme #FFFFFF` to set your username color to the given hex code.\n " +
                "https://htmlcolorcodes.com/color-picker/ if you don't know what hex is.";
    }

    @Override
    public void execute(final CommandEvent event) {
        final var guild = event.getGuild();
        final var member = event.getMember();
        final var userId = member.getUser().getId();

        final var colorStr = event.getArgs().trim();

        final Color color;
        try {
            color = Color.decode(colorStr);
        } catch (final Exception e) {
            event.replyError("Could not parse color: " + colorStr);
            return;
        }

        final var optionalRole = Constants.hasRole(member, userId);

        if (optionalRole.isPresent()) {
            final var role = optionalRole.get();

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
