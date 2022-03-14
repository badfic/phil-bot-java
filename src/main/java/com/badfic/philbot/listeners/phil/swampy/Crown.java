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
public class Crown extends BaseSwampy {

    private final BufferedImage mainImage;

    public Crown() throws Exception {
        name = "crown";
        help = "`!!crown @someone` to crown them";

        mainImage = ImageIO.read(Objects.requireNonNull(BasicResponsesBot.class.getClassLoader().getResourceAsStream("flags/crown.png")));
    }

    @Override
    protected void execute(CommandEvent event) {
        String authorFaceUrl = event.getMember().getEffectiveAvatarUrl();
        String recipientFaceUrl = authorFaceUrl;
        if (CollectionUtils.size(event.getMessage().getMentionedMembers()) == 1) {
            recipientFaceUrl = event.getMessage().getMentionedMembers().get(0).getEffectiveAvatarUrl();
        }

        try {
            // Mario face coordinates: (447, 274) square 304px
            // Crown face coordinates: (211, 699) square 351px

            BufferedImage scaledAuthorImage = Constants.scaleImageUrlTo(304, 304, authorFaceUrl);
            BufferedImage scaledRecipientImage = Constants.scaleImageUrlTo(351, 351, recipientFaceUrl);

            BufferedImage outputImg = new BufferedImage(mainImage.getWidth(), mainImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = outputImg.createGraphics();

            graphics.setComposite(AlphaComposite.Clear);
            graphics.fillRect(0, 0, outputImg.getWidth(), outputImg.getHeight());

            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.drawImage(scaledAuthorImage, 447, 274, null);

            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.drawImage(scaledRecipientImage, 211, 699, null);

            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.drawImage(mainImage, 0, 0, null);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(outputImg, "png", outputStream);
            graphics.dispose();

            event.getTextChannel()
                    .sendMessage(" ")
                    .addFile(outputStream.toByteArray(), "crown.png")
                    .queue();
        } catch (Exception e) {
            event.replyError("Failed to generate 'crown' meme");
            honeybadgerReporter.reportError(e, null, getClass().getSimpleName() + " could not crown user " + event.getAuthor().getAsMention());
        }
    }
}
