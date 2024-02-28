package com.badfic.philbot.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmojiUtil {
    public static boolean isEmoji(IntStream codePoints) {
        String hexCodePoints = codePoints
                .mapToObj(Integer::toHexString)
                .map(String::toUpperCase)
                .collect(Collectors.joining(" "));

        try (InputStream emojiTestFileStream = EmojiUtil.class.getClassLoader().getResourceAsStream("unicode-org-emoji-test.txt");
             InputStreamReader inputStreamReader = new InputStreamReader(emojiTestFileStream, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(inputStreamReader)) {
            return reader.lines()
                    .map(line -> {
                        if (StringUtils.isBlank(line) || line.startsWith("#")) {
                            return null;
                        }
                        String[] split = line.split(";");
                        return split[0].trim();
                    })
                    .filter(Objects::nonNull)
                    .anyMatch(hex -> hex.equals(hexCodePoints));
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isEmoji(int[] codePoints) {
        return isEmoji(Arrays.stream(codePoints));
    }

    public static boolean isEmoji(String string) {
        return isEmoji(string.codePoints());
    }
}
