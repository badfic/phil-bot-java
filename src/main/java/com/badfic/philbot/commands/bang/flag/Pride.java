package com.badfic.philbot.commands.bang.flag;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.bang.image.ImageUtils;
import com.badfic.philbot.config.Constants;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class Pride extends BaseFlagCommand {

    private static final float ALPHA = 0.69f;

    Pride() {
        super("pride");
        name = "pride";
        aliases = new String[] {"flag"};
        help = "`!!pride\n" +
                Arrays.toString(FLAG_NAMES) +
                "\n`!!pride demi @Santiago`: apply demi flag to Santiago's profile picture";
    }

    @Override
    public void execute(final CommandEvent event) {
        var member = event.getMember();
        final var mentionedMembers = event.getMessage().getMentions().getMembers();
        if (CollectionUtils.size(mentionedMembers) == 1) {
            member = mentionedMembers.getFirst();
        }

        if (CollectionUtils.size(mentionedMembers) > 1) {
            event.replyError("Please only specify one user to pride");
            return;
        }

        var split = event.getArgs().split("\\s+");
        if ((CollectionUtils.size(mentionedMembers) == 1 && split.length == 1) || (StringUtils.isBlank(event.getArgs()))) {
            split = new String[] {"gay"};
        }
        final var flagName = split[0];

        try {
            final BufferedImage prideImage;
            try (final var prideFlagStream = getClass().getClassLoader().getResourceAsStream("flags/" + flagName + ".png")) {
                prideImage = ImageIO.read(Objects.requireNonNull(prideFlagStream));
            }

            final var effectiveAvatarUrl = member.getEffectiveAvatarUrl();
            final var profilePic = Constants.scaleImageUrlTo(128, 128, effectiveAvatarUrl);

            final var imageBytes = ImageUtils.makeOverlaidImage(prideImage, profilePic, ALPHA);

            event.getChannel().sendMessage(" ")
                    .addFiles(FileUpload.fromData(imageBytes, "pride.png"))
                    .queue();
        } catch (final Exception e) {
            log.error("Failed to pride [user={}] [args={}]", member.getEffectiveName(), event.getArgs(), e);
            event.replyError("Failed to pride " + member.getAsMention());
        }
    }

}
