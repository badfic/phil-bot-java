package com.badfic.philbot.commands.slash;

import com.badfic.philbot.commands.bang.image.ImageUtils;
import com.badfic.philbot.config.Constants;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

@Component
@Slf4j
class EmoteMe extends BaseSlashCommand {

    private static final float ALPHA = 0.69f;

    EmoteMe() {
        name = "emoteme";
        options = List.of(
                new OptionData(OptionType.STRING, "emote", "Emote/Emoji to apply to user's profile picture", true),
                new OptionData(OptionType.MENTIONABLE, "user", "User to emote", false)
        );
        help = "`/emoteme emote: :shrekphil: user: @Santiago`: apply shrek phil to Santiago's profile picture";
    }

    @Override
    public void execute(final SlashCommandInteractionEvent event) {
        final var interactionHook = event.deferReply().submit();

        var member = event.getMember();
        final var optionalMember = event.getOption("user");

        if (optionalMember != null && optionalMember.getAsMember() != null) {
            member = optionalMember.getAsMember();
        }

        final var emojiString = event.getOption("emote").getAsString();

        try {
            final var emojiUnion = Emoji.fromFormatted(emojiString);

            final var overlayImage = switch (emojiUnion.getType()) {
                case UNICODE -> {
                    final var hexCodePoints = emojiString.codePoints()
                            .mapToObj(Integer::toHexString)
                            .map(String::toUpperCase)
                            .collect(Collectors.joining(" "));

                    var isEmoji = false;
                    try (final var emojiTestFileStream = this.getClass().getClassLoader().getResourceAsStream("unicode-org-emoji-test.txt");
                         final var inputStreamReader = new InputStreamReader(emojiTestFileStream, StandardCharsets.UTF_8);
                         final var reader = new BufferedReader(inputStreamReader)) {
                        isEmoji = reader.lines()
                                .map(line -> {
                                    if (StringUtils.isBlank(line) || line.startsWith("#")) {
                                        return null;
                                    }
                                    final var split = line.split(";");
                                    return split[0].trim();
                                })
                                .filter(Objects::nonNull)
                                .anyMatch(hex -> hex.equals(hexCodePoints));
                    } catch (final IOException ignored) {}

                    if (!isEmoji) {
                        replyToInteractionHook(event, interactionHook,
                                "Could not find an emoji in your /emoteme command. If you think this is an error, contact Santiago \uD83D\uDE43");
                        yield null;
                    }

                    final var codesArray = emojiString.codePoints().toArray();
                    var codePoints = Arrays.stream(codesArray)
                            .mapToObj(Integer::toHexString)
                            .collect(Collectors.joining("-"))
                            .toLowerCase(Locale.ENGLISH);

                    var result = (BufferedImage) null;
                    try {
                        result = codePointsToBufferedImage(codePoints);
                    } catch (final Exception e) {
                        if (e instanceof HttpStatusCodeException sce
                                && sce.getStatusCode() == HttpStatus.NOT_FOUND
                                && ArrayUtils.getLength(codesArray) > 1) {
                            codePoints = Integer.toHexString(codesArray[0]).toLowerCase(Locale.ENGLISH);

                            try {
                                result = codePointsToBufferedImage(codePoints);
                            } catch (final Exception innerException) {
                                replyToInteractionHook(event, interactionHook,
                                        "Could not find an emoji in your /emoteme command. If you think this is an error, contact Santiago \uD83D\uDE43");
                                yield null;
                            }
                        } else {
                            replyToInteractionHook(event, interactionHook,
                                    "Could not find an emoji in your /emoteme command. If you think this is an error, contact Santiago \uD83D\uDE43");
                            yield null;
                        }
                    }

                    if (result == null) {
                        replyToInteractionHook(event, interactionHook, "Could not find an image for given emoji: " + codePoints);
                        yield null;
                    }

                    yield result;
                }
                case CUSTOM -> {
                    final var emote = emojiUnion.asCustom();

                    if (StringUtils.isBlank(emote.getImageUrl())) {
                        replyToInteractionHook(event, interactionHook, "Could not load url for emote: " + emote.getAsMention());
                        yield null;
                    }

                    yield ImageIO.read(URI.create(emote.getImageUrl()).toURL());
                }
            };

            if (overlayImage == null) {
                return;
            }

            final var effectiveAvatarUrl = member.getEffectiveAvatarUrl();
            final var profilePic = ImageIO.read(URI.create(effectiveAvatarUrl).toURL());

            final var imageBytes = ImageUtils.makeOverlaidImage(overlayImage, profilePic, ALPHA);

            replyToInteractionHook(event, interactionHook, FileUpload.fromData(imageBytes, "emote.png"));
        } catch (final Exception e) {
            log.error("Failed to emoteme [user={}] [args={}]", member.getEffectiveName(), event, e);
            replyToInteractionHook(event, interactionHook, "Failed to emoteme " + member.getAsMention());
        }
    }

    private BufferedImage codePointsToBufferedImage(final String codePoints) throws IOException {
        final var url = "https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/" + codePoints + ".png";

        final var headers = new HttpHeaders();
        headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);

        final var emojiBytes = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
        final var emojiImageBytes = emojiBytes.getBody();
        return ImageIO.read(new FastByteArrayInputStream(emojiImageBytes));
    }
}
