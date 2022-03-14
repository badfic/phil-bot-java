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
public class Boop extends BaseSwampy {

    private final BufferedImage mainImage;

    public Boop() throws Exception {
        name = "boop";
        help = "`!!boop @someone` to boop them";

        mainImage = ImageIO.read(Objects.requireNonNull(BasicResponsesBot.class.getClassLoader().getResourceAsStream("flags/boop.png")));
    }

    @Override
    protected void execute(CommandEvent event) {
        String authorFaceUrl = event.getMember().getEffectiveAvatarUrl();
        String recipientFaceUrl = authorFaceUrl;
        if (CollectionUtils.size(event.getMessage().getMentionedMembers()) == 1) {
            recipientFaceUrl = event.getMessage().getMentionedMembers().get(0).getEffectiveAvatarUrl();
        }

        try {
            // Cat face coordinates: (265, 63) square 156px
            // Dog face coordinates: (459, 40) square 382px

            BufferedImage scaledAuthorImage = Constants.scaleImageUrlTo(156, 156, authorFaceUrl);
            BufferedImage scaledRecipientImage = Constants.scaleImageUrlTo(382, 382, recipientFaceUrl);

            BufferedImage outputImg = new BufferedImage(mainImage.getWidth(), mainImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = outputImg.createGraphics();

            graphics.setComposite(AlphaComposite.Clear);
            graphics.fillRect(0, 0, outputImg.getWidth(), outputImg.getHeight());

            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.drawImage(scaledAuthorImage, 265, 63, null);

            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.drawImage(scaledRecipientImage, 459, 40, null);

            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.drawImage(mainImage, 0, 0, null);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(outputImg, "png", outputStream);
            graphics.dispose();

            event.getTextChannel()
                    .sendMessage(" ")
                    .addFile(outputStream.toByteArray(), "boop.png")
                    .queue();
        } catch (Exception e) {
            event.replyError("Failed to generate 'boop' meme");
            honeybadgerReporter.reportError(e, null, getClass().getSimpleName() + " could not boop user " + event.getAuthor().getAsMention());
        }
    }
}
