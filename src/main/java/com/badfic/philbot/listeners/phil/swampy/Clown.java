package com.badfic.philbot.listeners.phil.swampy;

import com.jagrosh.jdautilities.command.CommandEvent;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

@Component
public class Clown extends BaseFlagCommand {

    private final BufferedImage image;

    public Clown() {
        name = "clown";
        help = "Display the clown emote with various pride flags.\n" +
                Arrays.toString(FLAG_NAMES) +
                "\n`!!clown demi`: display a demi clown emote";

        try {
            image = ImageIO.read(ResourceUtils.getFile("classpath:flags/clown.png"));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load clown png", e);
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

            BufferedImage newImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = newImg.createGraphics();

            graphics.setComposite(AlphaComposite.Clear);
            graphics.fillRect(0, 0, newImg.getWidth(), newImg.getHeight());

            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.drawImage(prideImage, 0, 0, null);

            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.drawImage(image, 0, 0, null);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(newImg, "png", outputStream);
            graphics.dispose();

            event.getTextChannel().sendMessage(" ")
                    .addFiles(FileUpload.fromData(outputStream.toByteArray(), "clown.png"))
                    .queue();
        } catch (Exception e) {
            honeybadgerReporter.reportError(e, null, "Failed to make clown, args: " + event.getArgs());
            event.replyError("Failed to " + event.getArgs() + " clown");
        }
    }

}
