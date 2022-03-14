package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.listeners.BasicResponsesBot;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
public class Rip extends BaseSwampy {

    private final BufferedImage mainImage;

    public Rip() throws Exception {
        name = "rip";
        aliases = new String[] {"grave"};
        help = "`!!rip @someone` to display the barry allen grave meme on their profile picture";

        mainImage = ImageIO.read(Objects.requireNonNull(BasicResponsesBot.class.getClassLoader().getResourceAsStream("flags/rip.png")));
    }

    @Override
    protected void execute(CommandEvent event) {
        String authorFaceUrl = event.getMember().getEffectiveAvatarUrl();
        String recipientFaceUrl = authorFaceUrl;
        if (CollectionUtils.size(event.getMessage().getMentionedMembers()) == 1) {
            recipientFaceUrl = event.getMessage().getMentionedMembers().get(0).getEffectiveAvatarUrl();
        }

        try {
            // Barry face coordinates: (264, 185) square 74px
            // Grave face coordinates: (74, 115) square 128px

            BufferedImage scaledAuthorImage = Constants.scaleImageUrlTo(74, 74, authorFaceUrl);
            BufferedImage scaledRecipientImage = Constants.scaleImageUrlTo(128, 128, recipientFaceUrl);

            BufferedImage outputImg = new BufferedImage(mainImage.getWidth(), mainImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = outputImg.createGraphics();

            graphics.setComposite(AlphaComposite.Clear);
            graphics.fillRect(0, 0, outputImg.getWidth(), outputImg.getHeight());

            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.drawImage(scaledAuthorImage, 264, 185, null);

            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.drawImage(scaledRecipientImage, 74, 115, null);

            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.drawImage(mainImage, 0, 0, null);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(outputImg, "png", outputStream);
            graphics.dispose();

            event.getTextChannel()
                    .sendMessage(" ")
                    .addFile(outputStream.toByteArray(), "rip.png")
                    .queue();
        } catch (Exception e) {
            event.replyError("Failed to generate 'rip' meme");
            honeybadgerReporter.reportError(e, null, getClass().getSimpleName() + " could not rip user " + event.getAuthor().getAsMention());
        }
    }
}
