package com.badfic.philbot.commands.bang.image;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.bang.BaseBangCommand;
import java.awt.image.BufferedImage;
import java.util.Objects;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.collections4.CollectionUtils;

@Slf4j
@RequiredArgsConstructor
abstract class BaseTwoUserImageMeme extends BaseBangCommand {

    private final int authorScale;
    private final int authorX;
    private final int authorY;
    private final int recipientScale;
    private final int recipientX;
    private final int recipientY;
    private final String mainImageLocation;
    private final String memeName;

    @Override
    public void execute(final CommandEvent event) {
        final var authorFaceUrl = event.getMember().getEffectiveAvatarUrl();
        var recipientFaceUrl = authorFaceUrl;
        if (CollectionUtils.size(event.getMessage().getMentions().getMembers()) == 1) {
            recipientFaceUrl = event.getMessage().getMentions().getMembers().getFirst().getEffectiveAvatarUrl();
        }

        try {
            final BufferedImage mainImage;
            try (final var stream = getClass().getClassLoader().getResourceAsStream(mainImageLocation)) {
                mainImage = ImageIO.read(Objects.requireNonNull(stream));
            }
            final var bytes = ImageUtils.makeTwoUserMemeImageBytes(authorFaceUrl, authorScale, authorX, authorY,
                    recipientFaceUrl, recipientScale, recipientX, recipientY, mainImage);

            event.getChannel()
                    .sendMessage(" ")
                    .addFiles(FileUpload.fromData(bytes, memeName + ".png"))
                    .queue();
        } catch (final Exception e) {
            log.error("{} could not {} [user={}]", getClass().getSimpleName(), memeName, event.getAuthor().getAsMention(), e);
            event.replyError("Failed to generate '" + memeName + "' meme");
        }
    }

}
