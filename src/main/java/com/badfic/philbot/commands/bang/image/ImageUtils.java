package com.badfic.philbot.commands.bang.image;

import com.badfic.philbot.config.Constants;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageUtils {
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

    public static byte[] makeOverlaidImage(BufferedImage bottomImage, BufferedImage topImage, float alpha) throws IOException {
        BufferedImage newImg = new BufferedImage(bottomImage.getWidth(), bottomImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = newImg.createGraphics();

        graphics.setComposite(AlphaComposite.Clear);
        graphics.fillRect(0, 0, newImg.getWidth(), newImg.getHeight());

        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.drawImage(bottomImage, 0, 0, null);

        graphics.setComposite(AlphaComposite.SrcOver.derive(alpha));
        graphics.drawImage(topImage, 0, 0, null);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(newImg, "png", outputStream);
            graphics.dispose();
            return outputStream.toByteArray();
        }
    }
}
