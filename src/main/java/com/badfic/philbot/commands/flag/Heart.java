package com.badfic.philbot.commands.flag;

import com.jagrosh.jdautilities.command.CommandEvent;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import javax.imageio.ImageIO;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class Heart extends BaseFlagCommand {

    private final BufferedImage heart;

    public Heart() {
        name = "heart";
        help = "`Display the heart emote with various pride flags.\n" +
                Arrays.toString(FLAG_NAMES) +
                "\n`!!heart demi`: display a demi heart emote";

        try {
            try (InputStream stream = getClass().getClassLoader().getResourceAsStream("flags/heart.png")) {
                heart = ImageIO.read(Objects.requireNonNull(stream));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load heart png", e);
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String[] split = event.getArgs().split("\\s+");
            if (StringUtils.isBlank(event.getArgs())) {
                split = new String[] {"gay"};
            }

            BufferedImage prideImage = PRIDE_IMAGES.get(split[0]);

            if (prideImage == null) {
                event.replyError("Could not find flag for: " + split[0]);
                return;
            }

            BufferedImage newImg = new BufferedImage(heart.getWidth(), heart.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = newImg.createGraphics();

            graphics.setComposite(AlphaComposite.Clear);
            graphics.fillRect(0, 0, newImg.getWidth(), newImg.getHeight());

            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.drawImage(prideImage, 0, 0, null);

            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.drawImage(heart, 0, 0, null);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(newImg, "png", outputStream);
            graphics.dispose();

            event.getTextChannel().sendMessage(" ")
                    .addFiles(FileUpload.fromData(outputStream.toByteArray(), "heart.png"))
                    .queue();
        } catch (Exception e) {
            honeybadgerReporter.reportError(e, null, "Failed to make heart, args: " + event.getArgs());
            event.replyError("Failed to " + event.getArgs() + " heart");
        }
    }

}
