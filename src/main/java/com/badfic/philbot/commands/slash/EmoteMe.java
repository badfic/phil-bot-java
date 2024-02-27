package com.badfic.philbot.commands.slash;

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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmoteMe extends BaseSlashCommand {

    private static final float ALPHA = 0.69f;

    public EmoteMe() {
        name = "emoteme";
        options = List.of(
                new OptionData(OptionType.STRING, "emote", "Emote/Emoji to apply to user's profile picture", true),
                new OptionData(OptionType.MENTIONABLE, "user", "User to emote", false)
        );
        help = "`!!emoteme :shrekphil: @Santiago`: apply shrek phil to Santiago's profile picture";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        CompletableFuture<InteractionHook> interactionHook = event.deferReply().submit();

        Member member = event.getMember();
        OptionMapping optionalMember = event.getOption("user");

        if (optionalMember != null && optionalMember.getAsMember() != null) {
            member = optionalMember.getAsMember();
        }

        String emojiString = event.getOption("emote").getAsString();

        try {
            EmojiUnion emojiUnion = Emoji.fromFormatted(emojiString);

            BufferedImage overlayImage = switch (emojiUnion.getType()) {
                case UNICODE -> {
                    String codePoints = Arrays.stream(emojiUnion.asUnicode().getName().codePoints().toArray())
                            .mapToObj(Integer::toHexString)
                            .collect(Collectors.joining("-"))
                            .toLowerCase(Locale.ENGLISH);

                    BufferedImage result = null;
                    try {
                        String url = "https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/" + codePoints + ".png";

                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);

                        ResponseEntity<byte[]> emojiBytes = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
                        byte[] emojiImageBytes = emojiBytes.getBody();
                        result = ImageIO.read(new ByteArrayInputStream(emojiImageBytes));
                    } catch (Exception e) {
                        replyToInteractionHook(event, interactionHook, "Could not find an emoji in your !!emoteme command. If you think this is an error, contact Santiago \uD83D\uDE43");
                        yield null;
                    }

                    if (result == null) {
                        replyToInteractionHook(event, interactionHook, "Could not find an image for given emoji: " + codePoints);
                        yield null;
                    }

                    yield result;
                }
                case CUSTOM -> {
                    CustomEmoji emote = emojiUnion.asCustom();

                    if (StringUtils.isBlank(emote.getImageUrl())) {
                        replyToInteractionHook(event, interactionHook, "Could not load url for emote: " + emote.getAsMention());
                        yield null;
                    }

                    yield ImageIO.read(URI.create(emote.getImageUrl()).toURL());
                }
            };

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

            replyToInteractionHook(event, interactionHook, FileUpload.fromData(outputStream.toByteArray(), "emote.png"));
        } catch (Exception e) {
            log.error("Failed to emoteme [user={}] [args={}]", member.getEffectiveName(), event, e);
            replyToInteractionHook(event, interactionHook, "Failed to emoteme " + member.getAsMention());
        }
    }

}
