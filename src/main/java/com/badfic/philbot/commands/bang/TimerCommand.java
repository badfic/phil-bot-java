package com.badfic.philbot.commands.bang;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.Constants;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

@Component
class TimerCommand extends BaseBangCommand {

    TimerCommand() {
        name = "timer";
        help = "`!!timer` a simple timer, only works in seconds. Max 5 minutes (300 seconds). Example:\n"+
                "`!!timer 30` a timer for 30 seconds, phil will let you know when it's complete.";
    }

    @Override
    public void execute(final CommandEvent event) {
        final var args = event.getArgs().split("\\s+");

        try {
            final var time = Integer.parseInt(args[0]);

            if (time > 300) {
                event.replyError("Max is 300 seconds (5 minutes)");
                return;
            }

            final var id = RandomStringUtils.randomAlphabetic(4);
            event.reply(Constants.simpleEmbed("Timer started for " + time + " seconds", null,
                    "https://cdn.discordapp.com/attachments/752665408770801737/777011911647690752/Webp.net-resizeimage.png", "timer id = " + id));
            event.getChannel().sendMessageEmbeds(Constants.simpleEmbed("Time's up", null,
                    "https://cdn.discordapp.com/attachments/752665408770801737/777011404536414228/Webp.net-resizeimage.jpg", "timer id = " + id))
                    .queueAfter(time, TimeUnit.SECONDS);
        } catch (final NumberFormatException e) {
            event.replyError("Badly formatted command. Example `!!timer 30` for a 30 second timer.");
        }
    }
}
