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

public abstract class BaseTwoUserImageMeme extends BaseSwampy {

    private final int authorScale;
    private final int authorX;
    private final int authorY;
    private final int recipientScale;
    private final int recipientX;
    private final int recipientY;
    private final BufferedImage mainImage;
    private final String memeName;

    public BaseTwoUserImageMeme(int authorScale, int authorX, int authorY, int recipientScale, int recipientX, int recipientY, String mainImageLocation,
                                String memeName) throws Exception {
        this.authorScale = authorScale;
        this.authorX = authorX;
        this.authorY = authorY;
        this.recipientScale = recipientScale;
        this.recipientX = recipientX;
        this.recipientY = recipientY;
        this.mainImage = ImageIO.read(Objects.requireNonNull(BasicResponsesBot.class.getClassLoader().getResourceAsStream(mainImageLocation)));
        this.memeName = memeName;
    }

    @Override
    protected void execute(CommandEvent event) {
        String authorFaceUrl = event.getMember().getEffectiveAvatarUrl();
        String recipientFaceUrl = authorFaceUrl;
        if (CollectionUtils.size(event.getMessage().getMentionedMembers()) == 1) {
            recipientFaceUrl = event.getMessage().getMentionedMembers().get(0).getEffectiveAvatarUrl();
        }

        try {
            byte[] bytes = makeMemeBytes(authorFaceUrl, recipientFaceUrl);

            event.getTextChannel()
                    .sendMessage(" ")
                    .addFile(bytes, memeName + ".png")
                    .queue();
        } catch (Exception e) {
            event.replyError("Failed to generate '" + memeName + "' meme");
            honeybadgerReporter.reportError(e, null, getClass().getSimpleName() + " could not " + memeName + " user " + event.getAuthor().getAsMention());
        }
    }

    public static byte[] makeTwoUserMemeImageBytes(String authorFaceUrl, int authorScale, int authorX, int authorY,
                                                   String recipientFaceUrl, int recipientScale, int recipientX, int recipientY,
                                                   BufferedImage mainImage) throws Exception {
        BufferedImage scaledAuthorImage = Constants.scaleImageUrlTo(authorScale, authorScale, authorFaceUrl);
        BufferedImage scaledRecipientImage = Constants.scaleImageUrlTo(recipientScale, recipientScale, recipientFaceUrl);

        BufferedImage outputImg = new BufferedImage(mainImage.getWidth(), mainImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = outputImg.createGraphics();

        graphics.setComposite(AlphaComposite.Clear);
        graphics.fillRect(0, 0, outputImg.getWidth(), outputImg.getHeight());

        if (scaledAuthorImage != null) {
            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.drawImage(scaledAuthorImage, authorX, authorY, null);
        }

        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.drawImage(scaledRecipientImage, recipientX, recipientY, null);

        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.drawImage(mainImage, 0, 0, null);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(outputImg, "png", outputStream);
        graphics.dispose();

        return outputStream.toByteArray();
    }

    private byte[] makeMemeBytes(String authorFaceUrl, String recipientFaceUrl) throws Exception {
        return makeTwoUserMemeImageBytes(authorFaceUrl, authorScale, authorX, authorY, recipientFaceUrl, recipientScale, recipientX, recipientY, mainImage);
    }

}
