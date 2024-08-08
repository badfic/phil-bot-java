package com.badfic.philbot.commands.bang.image;

import com.badfic.philbot.config.Constants;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import java.awt.AlphaComposite;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageUtils {
    public static byte[] makeTwoUserMemeImageBytes(final String authorFaceUrl, final int authorScale, final int authorX, final int authorY,
                                                   final String recipientFaceUrl, final int recipientScale, final int recipientX, final int recipientY,
                                                   final BufferedImage mainImage) throws Exception {
        final var scaledAuthorImage = Constants.scaleImageUrlTo(authorScale, authorScale, authorFaceUrl);
        final var scaledRecipientImage = Constants.scaleImageUrlTo(recipientScale, recipientScale, recipientFaceUrl);

        final var outputImg = new BufferedImage(mainImage.getWidth(), mainImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final var graphics = outputImg.createGraphics();

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

        try (final var outputStream = new FastByteArrayOutputStream()) {
            ImageIO.write(outputImg, "png", outputStream);
            graphics.dispose();

            return outputStream.array;
        }
    }

    public static byte[] makeOverlaidImage(final BufferedImage bottomImage, final BufferedImage topImage, final float alpha) throws IOException {
        final var newImg = new BufferedImage(bottomImage.getWidth(), bottomImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final var graphics = newImg.createGraphics();

        graphics.setComposite(AlphaComposite.Clear);
        graphics.fillRect(0, 0, newImg.getWidth(), newImg.getHeight());

        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.drawImage(bottomImage, 0, 0, null);

        graphics.setComposite(AlphaComposite.SrcOver.derive(alpha));
        graphics.drawImage(topImage, 0, 0, null);

        try (final var outputStream = new FastByteArrayOutputStream()) {
            ImageIO.write(newImg, "png", outputStream);
            graphics.dispose();
            return outputStream.array;
        }
    }
}
