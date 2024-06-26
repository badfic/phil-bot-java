package com.badfic.philbot.commands.bang.image;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.bang.BaseBangCommand;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Objects;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.collections4.CollectionUtils;

@Slf4j
public abstract class BaseTwoUserImageMeme extends BaseBangCommand {

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
            recipientFaceUrl = event.getMessage().getMentions().getMembers().getFirst().getEffectiveAvatarUrl();
        }

        try {
            BufferedImage mainImage;
            try (InputStream stream = getClass().getClassLoader().getResourceAsStream(mainImageLocation)) {
                mainImage = ImageIO.read(Objects.requireNonNull(stream));
            }
            byte[] bytes = ImageUtils.makeTwoUserMemeImageBytes(authorFaceUrl, authorScale, authorX, authorY,
                    recipientFaceUrl, recipientScale, recipientX, recipientY, mainImage);

            event.getChannel()
                    .sendMessage(" ")
                    .addFiles(FileUpload.fromData(bytes, memeName + ".png"))
                    .queue();
        } catch (Exception e) {
            log.error("{} could not {} [user={}]", getClass().getSimpleName(), memeName, event.getAuthor().getAsMention(), e);
            event.replyError("Failed to generate '" + memeName + "' meme");
        }
    }

}
