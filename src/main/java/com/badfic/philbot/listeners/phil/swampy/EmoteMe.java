package com.badfic.philbot.listeners.phil.swampy;

import com.jagrosh.jdautilities.command.CommandEvent;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.List;
import javax.imageio.ImageIO;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class EmoteMe extends BaseSwampy {

    private static final float ALPHA = 0.69f;

    public EmoteMe() {
        name = "emoteme";
        help = "`!!emoteme :shrekphil: @Santiago`: apply shrek phil to Santiago's profile picture";
    }

    @Override
    protected void execute(CommandEvent event) {
        Member member = event.getMember();
        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();
        if (CollectionUtils.size(mentionedMembers) == 1) {
            member = mentionedMembers.get(0);
        }

        if (CollectionUtils.size(mentionedMembers) > 1) {
            event.replyError("Please only specify one user");
            return;
        }

        if (CollectionUtils.size(event.getMessage().getEmotes()) != 1) {
            event.replyError("Please only specify one emote");
        }

        Emote emote = event.getMessage().getEmotes().get(0);

        if (StringUtils.isBlank(emote.getImageUrl())) {
            event.replyError("Could not load url for emote");
            return;
        }

        try {
            String effectiveAvatarUrl = member.getUser().getEffectiveAvatarUrl();
            BufferedImage profilePic = ImageIO.read(new URL(effectiveAvatarUrl));
            BufferedImage overlayImage = ImageIO.read(new URL(emote.getImageUrl()));

            BufferedImage newImg = new BufferedImage(profilePic.getWidth(), profilePic.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = newImg.createGraphics();

            graphics.setComposite(AlphaComposite.Clear);
            graphics.fillRect(0, 0, newImg.getWidth(), newImg.getHeight());

            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.drawImage(profilePic, 0, 0, null);

            graphics.setComposite(AlphaComposite.SrcOver.derive(ALPHA));
            graphics.drawImage(overlayImage, 0, 0, null);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(newImg, "png", outputStream);
            graphics.dispose();

            event.getTextChannel().sendMessage(" ")
                    .addFile(outputStream.toByteArray(), "emote.png")
                    .queue();
        } catch (Exception e) {
            honeybadgerReporter.reportError(e, null, "Failed to emoteme user [" + member.getEffectiveName() + "], args: " + event.getArgs());
            event.replyError("Failed to emoteme " + member.getAsMention());
        }
    }

}
