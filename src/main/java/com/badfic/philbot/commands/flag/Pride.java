package com.badfic.philbot.commands.flag;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.Constants;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Pride extends BaseFlagCommand {

    private static final float ALPHA = 0.69f;

    public Pride() {
        name = "pride";
        aliases = new String[] {"flag"};
        help = "`!!pride\n" +
                Arrays.toString(FLAG_NAMES) +
                "\n`!!pride demi @Santiago`: apply demi flag to Santiago's profile picture";
    }

    @Override
    public void execute(CommandEvent event) {
        Member member = event.getMember();
        List<Member> mentionedMembers = event.getMessage().getMentions().getMembers();
        if (CollectionUtils.size(mentionedMembers) == 1) {
            member = mentionedMembers.get(0);
        }

        if (CollectionUtils.size(mentionedMembers) > 1) {
            event.replyError("Please only specify one user to pride");
            return;
        }

        try {
            String[] split = event.getArgs().split("\\s+");
            if ((CollectionUtils.size(mentionedMembers) == 1 && split.length == 1) || (StringUtils.isBlank(event.getArgs()))) {
                split = new String[] {"gay"};
            }

            BufferedImage prideImage;
            try (InputStream prideFlagStream = getClass().getClassLoader().getResourceAsStream("flags/" + split[0] + ".png")) {
                prideImage = ImageIO.read(Objects.requireNonNull(prideFlagStream));
            }

            String effectiveAvatarUrl = member.getEffectiveAvatarUrl();
            BufferedImage profilePic = Constants.scaleImageUrlTo(128, 128, effectiveAvatarUrl);

            BufferedImage newImg = new BufferedImage(profilePic.getWidth(), profilePic.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = newImg.createGraphics();

            graphics.setComposite(AlphaComposite.Clear);
            graphics.fillRect(0, 0, newImg.getWidth(), newImg.getHeight());

            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.drawImage(profilePic, 0, 0, null);

            graphics.setComposite(AlphaComposite.SrcOver.derive(ALPHA));
            graphics.drawImage(prideImage, 0, 0, null);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(newImg, "png", outputStream);
            graphics.dispose();

            event.getChannel().sendMessage(" ")
                    .addFiles(FileUpload.fromData(outputStream.toByteArray(), "pride.png"))
                    .queue();
        } catch (Exception e) {
            log.error("Failed to pride [user={}] [args={}]", member.getEffectiveName(), event.getArgs(), e);
            event.replyError("Failed to pride " + member.getAsMention());
        }
    }

}
