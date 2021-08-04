package com.badfic.philbot.listeners.phil.swampy;

import com.jagrosh.jdautilities.command.CommandEvent;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class Pride extends BaseFlagCommand {

    private static final float ALPHA = 0.69f;

    public Pride() {
        name = "pride";
        help = "`!!pride\n" +
                Arrays.toString(FLAG_NAMES) +
                "\n`!!pride demi @Santiago`: apply demi flag to Santiago's profile picture";
    }

    @Override
    protected void execute(CommandEvent event) {
        Member member = event.getMember();
        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();
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

            BufferedImage prideImage = PRIDE_IMAGES.get(split[0]);

            if (prideImage == null) {
                event.replyError("Could not find flag for: " + split[0]);
                return;
            }

            String effectiveAvatarUrl = member.getUser().getEffectiveAvatarUrl();
            BufferedImage profilePic = ImageIO.read(new URL(effectiveAvatarUrl));

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

            event.getTextChannel().sendMessage(" ")
                    .addFile(outputStream.toByteArray(), "pride.png")
                    .queue();
        } catch (Exception e) {
            honeybadgerReporter.reportError(e, null, "Failed to pride user [" + member.getEffectiveName() + "], args: " + event.getArgs());
            event.replyError("Failed to pride " + member.getAsMention());
        }
    }

}
