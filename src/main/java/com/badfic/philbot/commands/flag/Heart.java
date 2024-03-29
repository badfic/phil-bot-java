package com.badfic.philbot.commands.flag;

import com.badfic.philbot.CommandEvent;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Heart extends BaseFlagCommand {

    public Heart() {
        name = "heart";
        help = "`Display the heart emote with various pride flags.\n" +
                Arrays.toString(FLAG_NAMES) +
                "\n`!!heart demi`: display a demi heart emote";
    }

    @Override
    public void execute(CommandEvent event) {
        try {
            String[] split = event.getArgs().split("\\s+");
            if (StringUtils.isBlank(event.getArgs())) {
                split = new String[] {"gay"};
            }

            BufferedImage prideImage;
            try (InputStream prideFlagStream = getClass().getClassLoader().getResourceAsStream("flags/" + split[0] + ".png")) {
                prideImage = ImageIO.read(Objects.requireNonNull(prideFlagStream));
            }

            BufferedImage heart;
            try (InputStream stream = getClass().getClassLoader().getResourceAsStream("flags/heart.png")) {
                heart = ImageIO.read(Objects.requireNonNull(stream));
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

            event.getChannel().sendMessage(" ")
                    .addFiles(FileUpload.fromData(outputStream.toByteArray(), "heart.png"))
                    .queue();
        } catch (Exception e) {
            log.error("Failed to make heart, [args={}]", event.getArgs(), e);
            event.replyError("Failed to " + event.getArgs() + " heart");
        }
    }

}
