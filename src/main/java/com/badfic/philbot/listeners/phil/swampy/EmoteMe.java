package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.vdurmont.emoji.EmojiParser;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.imageio.ImageIO;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class EmoteMe extends BaseSwampy {

    private static final float ALPHA = 0.69f;
    private volatile boolean emojiDownloadAttempted;
    private volatile Path emojiFile;

    public EmoteMe() {
        name = "emoteme";
        help = "`!!emoteme :shrekphil: @Santiago`: apply shrek phil to Santiago's profile picture";
    }

    @PostConstruct
    public void init() {
        try {
            Request request = new Request.Builder()
                    .url("https://unicode.org/emoji/charts/full-emoji-list.html")
                    .addHeader(HttpHeaders.USER_AGENT, Constants.USER_AGENT)
                    .get()
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    emojiDownloadAttempted = true;
                    honeybadgerReporter.reportError(e, null, "Failed to download emoji list from unicode.org");
                    emojiFile = null;
                }

                @Override
                public void onResponse(Call call, Response response) {
                    Path tempFile = null;
                    try {
                        tempFile = Files.createTempFile(null, "html");
                        try (BufferedSink bufferedSink = Okio.buffer(Okio.sink(tempFile.toFile()))) {
                            bufferedSink.writeAll(response.body().source());
                        }
                        emojiFile = tempFile;
                    } catch (Exception e) {
                        honeybadgerReporter.reportError(e, null, "Failed to download emoji list from unicode.org");
                        emojiFile = null;

                        if (tempFile != null) {
                            FileUtils.deleteQuietly(tempFile.toFile());
                        }
                    } finally {
                        emojiDownloadAttempted = true;
                    }
                }
            });
        } catch (Exception e) {
            honeybadgerReporter.reportError(e, null, "Failed to download emoji list from unicode.org");
            emojiFile = null;
            emojiDownloadAttempted = true;
        }
    }

    @PreDestroy
    public void tearDown() {
        if (emojiFile != null && Files.exists(emojiFile)) {
            FileUtils.deleteQuietly(emojiFile.toFile());
        }
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

        List<String> emojis = EmojiParser.extractEmojis(event.getMessage().getContentRaw());
        if (CollectionUtils.size(emojis) > 1) {
            event.replyError("Please only specify one emoji");
            return;
        }

        try {
            BufferedImage overlayImage = null;
            if (CollectionUtils.size(emojis) == 1) {
                if (!emojiDownloadAttempted) {
                    event.replyError("I haven't downloaded the emoji list yet, give me a minute");
                    return;
                }

                if (emojiFile == null || !Files.exists(emojiFile)) {
                    synchronized (EmoteMe.class) {
                        if (emojiFile == null || !Files.exists(emojiFile)) {
                            init();
                        }
                    }
                }

                if (emojiFile == null || !Files.exists(emojiFile)) {
                    event.replyError("Could not download emoji list from unicode.org");
                    return;
                }

                String emoji = emojis.get(0);

                Pattern pattern = Pattern.compile(String.format("<img alt='%s' class='imga' src='data:image/png;base64,([^']+)'>", emoji));
                try (BufferedReader reader = Files.newBufferedReader(emojiFile)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            if (matcher.groupCount() < 1) {
                                event.replyError("Could not load image for given emoji, matches groupcount less than 1: " + emoji);
                                return;
                            }

                            byte[] decodedEmojiBytes = Base64.getDecoder().decode(matcher.group(1));
                            overlayImage = ImageIO.read(new ByteArrayInputStream(decodedEmojiBytes));
                            break;
                        }
                    }
                }

                if (overlayImage == null) {
                    event.replyError("Could not find image for given emoji: " + emoji);
                    return;
                }
            } else {
                if (CollectionUtils.size(event.getMessage().getEmotes()) != 1) {
                    event.replyError("Please only specify one emote");
                    return;
                }

                Emote emote = event.getMessage().getEmotes().get(0);

                if (StringUtils.isBlank(emote.getImageUrl())) {
                    event.replyError("Could not load url for emote");
                    return;
                }

                overlayImage = ImageIO.read(new URL(emote.getImageUrl()));
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
