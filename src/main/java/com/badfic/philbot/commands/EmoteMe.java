package com.badfic.philbot.commands;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.Constants;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmoteMe extends BaseNormalCommand {

    private static final float ALPHA = 0.69f;

    public EmoteMe() {
        name = "emoteme";
        help = "`!!emoteme :shrekphil: @Santiago`: apply shrek phil to Santiago's profile picture";
    }

    @Override
    public void execute(CommandEvent event) {
        Member member = event.getMember();
        List<Member> mentionedMembers = event.getMessage().getMentions().getMembers();
        if (CollectionUtils.isNotEmpty(mentionedMembers)) {
            event.replyError("You can only !!emoteme yourself right now, Santiago is working on fixing this command to work with mentions again.");
            return;
        }
        String args = event.getArgs();

        try {
            BufferedImage overlayImage = null;
            if (CollectionUtils.size(event.getMessage().getMentions().getCustomEmojis()) == 1) {
                CustomEmoji emote = event.getMessage().getMentions().getCustomEmojis().get(0);

                if (StringUtils.isBlank(emote.getImageUrl())) {
                    event.replyError("Could not load url for emote");
                    return;
                }

                overlayImage = ImageIO.read(URI.create(emote.getImageUrl()).toURL());
            } else {
                String codePoints = Arrays.stream(args.trim().codePoints().toArray())
                        .mapToObj(Integer::toHexString)
                        .collect(Collectors.joining("-"))
                        .toLowerCase(Locale.ENGLISH);

                try {
                    String url = "https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/" + codePoints + ".png";

                    HttpHeaders headers = new HttpHeaders();
                    headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);

                    ResponseEntity<byte[]> emojiBytes = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
                    byte[] emojiImageBytes = emojiBytes.getBody();
                    overlayImage = ImageIO.read(new ByteArrayInputStream(emojiImageBytes));
                } catch (Exception e) {
                    event.replyError("Could not find an emoji in your !!emoteme command. If you think this is an error, contact Santiago \uD83D\uDE43");
                    return;
                }

                if (overlayImage == null) {
                    event.replyError("Could not find an image for given emoji: " + codePoints);
                    return;
                }
            }

            String effectiveAvatarUrl = member.getEffectiveAvatarUrl();
            BufferedImage profilePic = ImageIO.read(URI.create(effectiveAvatarUrl).toURL());

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

            event.getChannel().sendMessage(" ")
                    .addFiles(FileUpload.fromData(outputStream.toByteArray(), "emote.png"))
                    .queue();
        } catch (Exception e) {
            log.error("Failed to emoteme [user={}] [args={}]", member.getEffectiveName(), args, e);
            event.replyError("Failed to emoteme " + member.getAsMention());
        }
    }

}
