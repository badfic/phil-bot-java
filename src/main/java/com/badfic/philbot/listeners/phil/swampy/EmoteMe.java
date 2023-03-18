package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.jagrosh.jdautilities.command.CommandEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
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
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.utils.FileUpload;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class EmoteMe extends BaseSwampy {

    private static final float ALPHA = 0.69f;
    private volatile boolean emojiImageListDownloadAttempted;
    private volatile Path emojiImageListFile;
    private volatile boolean emojiTestListDownloadAttempted;
    private volatile Path emojiTestListFile;

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
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    emojiImageListDownloadAttempted = true;
                    honeybadgerReporter.reportError(e, null, "Failed to download emoji image list from unicode.org");
                    emojiImageListFile = null;
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    Path tempFile = null;
                    try {
                        tempFile = Files.createTempFile(null, "html");
                        try (BufferedSink bufferedSink = Okio.buffer(Okio.sink(tempFile.toFile()))) {
                            bufferedSink.writeAll(response.body().source());
                        }
                        emojiImageListFile = tempFile;
                    } catch (Exception e) {
                        honeybadgerReporter.reportError(e, null, "Failed to download emoji image list from unicode.org");
                        emojiImageListFile = null;

                        if (tempFile != null) {
                            FileUtils.deleteQuietly(tempFile.toFile());
                        }
                    } finally {
                        emojiImageListDownloadAttempted = true;
                    }
                }
            });
        } catch (Exception e) {
            honeybadgerReporter.reportError(e, null, "Failed to download emoji image list from unicode.org");
            emojiImageListFile = null;
            emojiImageListDownloadAttempted = true;
        }

        try {
            Request request = new Request.Builder()
                    .url("https://unicode.org/Public/emoji/latest/emoji-test.txt")
                    .addHeader(HttpHeaders.USER_AGENT, Constants.USER_AGENT)
                    .get()
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    emojiTestListDownloadAttempted = true;
                    honeybadgerReporter.reportError(e, null, "Failed to download emoji test list from unicode.org");
                    emojiTestListFile = null;
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    Path tempFile = null;
                    try {
                        tempFile = Files.createTempFile(null, "txt");
                        try (BufferedSink bufferedSink = Okio.buffer(Okio.sink(tempFile.toFile()))) {
                            bufferedSink.writeAll(response.body().source());
                        }
                        emojiTestListFile = tempFile;
                    } catch (Exception e) {
                        honeybadgerReporter.reportError(e, null, "Failed to download emoji test list from unicode.org");
                        emojiTestListFile = null;

                        if (tempFile != null) {
                            FileUtils.deleteQuietly(tempFile.toFile());
                        }
                    } finally {
                        emojiTestListDownloadAttempted = true;
                    }
                }
            });
        } catch (Exception e) {
            honeybadgerReporter.reportError(e, null, "Failed to download emoji test list from unicode.org");
            emojiTestListFile = null;
            emojiTestListDownloadAttempted = true;
        }
    }

    @PreDestroy
    public void tearDown() {
        if (emojiImageListFile != null && Files.exists(emojiImageListFile)) {
            FileUtils.deleteQuietly(emojiImageListFile.toFile());
        }
    }

    @Override
    protected void execute(CommandEvent event) {
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

                overlayImage = ImageIO.read(new URL(emote.getImageUrl()));
            } else {
                String codePoints = Arrays.stream(args.trim().codePoints().toArray())
                        .mapToObj(Integer::toHexString)
                        .collect(Collectors.joining(" "))
                        .toUpperCase(Locale.ENGLISH);

                boolean argsIsAnEmoji = false;
                try (BufferedReader reader = Files.newBufferedReader(emojiTestListFile)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (StringUtils.startsWith(line, codePoints)) {
                            argsIsAnEmoji = true;
                            break;
                        }
                    }
                }

                if (!argsIsAnEmoji) {
                    event.replyError("Could not find an emoji in your !!emoteme command. If you think this is an error, contact Santiago \uD83D\uDE43");
                    return;
                }

                String emoji = args.trim();

                if (!emojiImageListDownloadAttempted || !emojiTestListDownloadAttempted) {
                    event.replyError("I haven't downloaded the emoji list yet, give me a minute");
                    return;
                }

                if (emojiImageListFile == null || !Files.exists(emojiImageListFile) || emojiTestListFile == null || !Files.exists(emojiTestListFile)) {
                    synchronized (EmoteMe.class) {
                        if (emojiImageListFile == null || !Files.exists(emojiImageListFile) || emojiTestListFile == null || !Files.exists(emojiTestListFile)) {
                            init();
                        }
                    }
                }

                if (emojiImageListFile == null || !Files.exists(emojiImageListFile) || emojiTestListFile == null || !Files.exists(emojiTestListFile)) {
                    event.replyError("Could not download emoji list from unicode.org");
                    return;
                }

                Pattern pattern = Pattern.compile(String.format("<img alt='%s' class='imga' src='data:image/png;base64,([^']+)'>", emoji));
                try (BufferedReader reader = Files.newBufferedReader(emojiImageListFile)) {
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
            }

            String effectiveAvatarUrl = member.getEffectiveAvatarUrl();
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
                    .addFiles(FileUpload.fromData(outputStream.toByteArray(), "emote.png"))
                    .queue();
        } catch (Exception e) {
            honeybadgerReporter.reportError(e, null, "Failed to emoteme user [" + member.getEffectiveName() + "], args: " + args);
            event.replyError("Failed to emoteme " + member.getAsMention());
        }
    }

}
