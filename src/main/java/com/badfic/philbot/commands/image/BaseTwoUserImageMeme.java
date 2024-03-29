package com.badfic.philbot.commands.image;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.BaseNormalCommand;
import com.badfic.philbot.config.Constants;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Objects;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.collections4.CollectionUtils;

@Slf4j
public abstract class BaseTwoUserImageMeme extends BaseNormalCommand {

    private final int authorScale;
    private final int authorX;
    private final int authorY;
    private final int recipientScale;
    private final int recipientX;
    private final int recipientY;
    private final String mainImageLocation;
    private final String memeName;

    public BaseTwoUserImageMeme(int authorScale, int authorX, int authorY, int recipientScale, int recipientX, int recipientY, String mainImageLocation,
                                String memeName) {
        this.authorScale = authorScale;
        this.authorX = authorX;
        this.authorY = authorY;
        this.recipientScale = recipientScale;
        this.recipientX = recipientX;
        this.recipientY = recipientY;
        this.mainImageLocation = mainImageLocation;
        this.memeName = memeName;
    }

    @Override
    public void execute(CommandEvent event) {
        String authorFaceUrl = event.getMember().getEffectiveAvatarUrl();
        String recipientFaceUrl = authorFaceUrl;
        if (CollectionUtils.size(event.getMessage().getMentions().getMembers()) == 1) {
            recipientFaceUrl = event.getMessage().getMentions().getMembers().get(0).getEffectiveAvatarUrl();
        }

        try {
            byte[] bytes = makeMemeBytes(authorFaceUrl, recipientFaceUrl);

            event.getChannel()
                    .sendMessage(" ")
                    .addFiles(FileUpload.fromData(bytes, memeName + ".png"))
                    .queue();
        } catch (Exception e) {
            log.error("{} could not {} [user={}]", getClass().getSimpleName(), memeName, event.getAuthor().getAsMention(), e);
            event.replyError("Failed to generate '" + memeName + "' meme");
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
        BufferedImage mainImage;
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(mainImageLocation)) {
            mainImage = ImageIO.read(Objects.requireNonNull(stream));
        }
        return makeTwoUserMemeImageBytes(authorFaceUrl, authorScale, authorX, authorY, recipientFaceUrl, recipientScale, recipientX, recipientY, mainImage);
    }

}
