package com.badfic.philbot.commands.bang.flag;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.bang.BaseBangCommand;
import com.badfic.philbot.commands.bang.image.ImageUtils;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import javax.imageio.ImageIO;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.lang3.StringUtils;

abstract class BaseFlagCommand extends BaseBangCommand {
    protected static final String[] FLAG_NAMES = new String[] {
            "lesbian",
            "gay",
            "bi",
            "questioning",
            "pan",
            "ace",
            "aro",
            "aroace",
            "demi",
            "trans",
            "enby",
            "intersex",
            "auto",
            "trixic",
            "genderfluid",
            "vincian",
            "sapphic",
            "achillean",
            "poly"
    };

    BaseFlagCommand(String name) {
        this.name = name;
        this.help = "Display the %s emote with various pride flags.\n%s\n`!!%s demi`: display a demi %s emote"
                .formatted(name, Arrays.toString(FLAG_NAMES), name, name);
    }

    @Override
    public void execute(CommandEvent event) {
        try {
            String[] split = event.getArgs().split("\\s+");
            if (StringUtils.isBlank(event.getArgs())) {
                split = new String[] {"gay"};
            }

            BufferedImage prideImage;
            try (InputStream prideFlagStream = getClass().getClassLoader().getResourceAsStream("flags/%s.png".formatted(split[0]))) {
                prideImage = ImageIO.read(Objects.requireNonNull(prideFlagStream));
            }

            BufferedImage emoji;
            try (InputStream stream = getClass().getClassLoader().getResourceAsStream("flags/%s.png".formatted(name))) {
                emoji = ImageIO.read(Objects.requireNonNull(stream));
            }

            byte[] imageBytes = ImageUtils.makeOverlaidImage(prideImage, emoji, 1f);

            event.getChannel().sendMessage(" ")
                    .addFiles(FileUpload.fromData(imageBytes, name + ".png"))
                    .queue();
        } catch (Exception e) {
            event.replyError("Failed to %s %s".formatted(event.getArgs(), name));
        }
    }
}
