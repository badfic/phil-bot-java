package com.badfic.philbot.commands;

import com.badfic.philbot.config.Constants;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

@Component
public class RngCommand extends BaseNormalCommand {

    private static final Set<String> THUMBNAILS = Set.of(
            "https://cdn.discordapp.com/attachments/707453916882665552/781484144425173012/giphy9.gif",
            "https://cdn.discordapp.com/attachments/707453916882665552/781484151189930014/giphy8.gif",
            "https://cdn.discordapp.com/attachments/707453916882665552/781484155120648202/4tenor.gif",
            "https://cdn.discordapp.com/attachments/707453916882665552/781484174506852352/giphy7.gif",
            "https://cdn.discordapp.com/attachments/707453916882665552/781484192856801300/giphy5.gif",
            "https://cdn.discordapp.com/attachments/707453916882665552/781484210929401856/giphy3.gif",
            "https://cdn.discordapp.com/attachments/707453916882665552/781484225597145098/giphy2.gif",
            "https://cdn.discordapp.com/attachments/707453916882665552/781484240260300820/giphy.gif",
            "https://cdn.discordapp.com/attachments/707453916882665552/781484257977696256/200.gif",
            "https://cdn.discordapp.com/attachments/707453916882665552/781484274078580756/confusedgif.gif"
    );

    public RngCommand() {
        name = "rng";
        help = """
                Generate a random number:
                `!!rng 1-10` Generates a random number between 1 (inclusive) and 10 (inclusive)
                (negative numbers do not work)""";
    }

    @Override
    protected void execute(CommandEvent event) {
        String args = event.getArgs();
        String[] split = args.split("-");

        if (ArrayUtils.getLength(split) != 2) {
            event.replyError("Please specify a lower and upper bounds. Example: `!!rng 1-10`. (negative numbers do not work)");
            return;
        }

        try {
            long left = Long.parseLong(split[0]);
            long right = Long.parseLong(split[1]);

            if (left > right) {
                event.replyError("Lower bound must be less than upper bound");
                return;
            }

            long result = ThreadLocalRandom.current().nextLong(left, right + 1);

            event.reply(Constants.simpleEmbedThumbnail("RNG",
                    "Your random number between " + left + " and " + right + " is\n\n**" + result + "**",
                    Constants.pickRandom(THUMBNAILS)));
        } catch (NumberFormatException nfe) {
            event.replyError("Failed to parse lower and upper bounds as numbers");
        }
    }

}
