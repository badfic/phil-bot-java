package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import java.awt.Color;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
public class Ao3MetadataParser extends BaseService {

    private static final long AO3_RATE_LIMIT_MILLIS = TimeUnit.SECONDS.toMillis(10);
    private final Lock rateLimitLock = new ReentrantLock();
    private volatile long lastAo3Request = System.currentTimeMillis();

    public boolean parseLink(final String link, final String channelName) {
        try {
            final var work = getWork(link);
            final var messageEmbed = parseWork(link, work);
            philJda.getTextChannelsByName(channelName, false).getFirst().sendMessageEmbeds(messageEmbed).queue();
            return true;
        } catch (final Exception e) {
            log.error("Failed to parse ao3 [link={}]", link, e);
            return false;
        }
    }

    public void sendSummaryToWebhook(final String link, final String webhookUrl) {
        executorService.submit(() -> {
            try {
                final var work = getWork(link);
                final var messageEmbed = parseWork(link, work);
                final var map = messageEmbed.toData().toMap();
                final var jsonNode = objectMapper.valueToTree(map);

                restTemplate.exchange(webhookUrl, HttpMethod.PATCH, new HttpEntity<>(new Data(new JsonNode[]{jsonNode})), String.class);
                log.info("Successfully sent AO3 Summary for [link={}]", link);
            } catch (final Exception e) {
                log.error("Failed to send AO3 Summary for [link={}]", link, e);
            }
        });
    }

    public <T> T doWithLock(final Callable<T> work) throws Exception {
        rateLimitLock.lock();
        try {
            var difference = System.currentTimeMillis() - lastAo3Request;
            while (difference < AO3_RATE_LIMIT_MILLIS) {
                final var waitTime = TimeUnit.MILLISECONDS.toNanos(AO3_RATE_LIMIT_MILLIS - difference);
                LockSupport.parkNanos(waitTime);
                difference = System.currentTimeMillis() - lastAo3Request;
            }

            return work.call();
        } finally {
            lastAo3Request = System.currentTimeMillis();
            rateLimitLock.unlock();
        }
    }

    // Visible For Testing
    MessageEmbed parseWork(final String originalLink, final String work) {
        final var document = Jsoup.parse(work);

        final var titleElement = document.getElementsByClass("title heading");
        if (CollectionUtils.isEmpty(titleElement)) {
            return Constants.simpleEmbed(
                    "AO3 Summary Report",
                    "AO3 Summary Bot does not support works that you must be logged in to view.",
                    null,
                    "rip",
                    Constants.colorOfTheMonth(),
                    "https://cdn.discordapp.com/attachments/707453916882665552/780261925212520508/wITXDY67Xw1sAAAAABJRU5ErkJggg.png",
                    false);
        }
        final var title = titleElement.getFirst().text();

        final var authorElement = document.getElementsByAttributeValueContaining("rel", "author");
        final var authorLink = CollectionUtils.isEmpty(authorElement) ? "" : authorElement.getFirst().attr("href");
        final var authorName = CollectionUtils.isEmpty(authorElement) ? "Anonymous" : authorElement.getFirst().text();

        final var description = new StringBuilder();
        description.append("[**")
                .append(title)
                .append("**](")
                .append(originalLink)
                .append(")\nby [")
                .append(authorName)
                .append("](https://archiveofourown.org")
                .append(authorLink)
                .append(")\n\n**Rating**: ");

        final var workMetaGroup = document.getElementsByClass("work meta group").getFirst();

        var rating = "Explicit";
        for (final var tags : workMetaGroup.getElementsByClass("rating tags")) {
            if ("dd".equalsIgnoreCase(tags.tagName())) {
                final var tagList = tags.getElementsByTag("ul");
                for (final var li : tagList.getFirst().getElementsByTag("li")) {
                    rating = li.getElementsByTag("a").getFirst().text();
                    description.append(rating);
                    break;
                }
            }
        }

        description.append("\n\n**Warnings**: ");
        for (final var tags : workMetaGroup.getElementsByClass("warning tags")) {
            if ("dd".equalsIgnoreCase(tags.tagName())) {
                final var tagList = tags.getElementsByTag("ul");
                for (final var li : tagList.getFirst().getElementsByTag("li")) {
                    final var tag = li.getElementsByTag("a").getFirst().text();
                    description.append(tag).append(", ");
                }
            }
        }

        if (StringUtils.endsWith(description.toString(), ", ")) {
            description.delete(description.length() - 2, description.length());
        }

        description.append("\n\n**Categories**: ");
        for (final var tags : workMetaGroup.getElementsByClass("category tags")) {
            if ("dd".equalsIgnoreCase(tags.tagName())) {
                final var tagList = tags.getElementsByTag("ul");
                for (final var li : tagList.getFirst().getElementsByTag("li")) {
                    final var tag = li.getElementsByTag("a").getFirst().text();
                    description.append(tag).append(", ");
                }
            }
        }

        if (StringUtils.endsWith(description.toString(), ", ")) {
            description.delete(description.length() - 2, description.length());
        }

        description.append("\n\n**Summary**: ");
        final var summaryModule = document.getElementsByClass("summary module");
        if (summaryModule.isEmpty()) {
            description.append("No Summary Provided");
        } else {
            final var summary = summaryModule.getFirst().getElementsByTag("blockquote").text();
            description.append(summary.length() <= 800 ? summary : (summary.substring(0, 797) + "..."));
        }

        description.append("\n\n**Fandoms**: ");
        for (final var tags : workMetaGroup.getElementsByClass("fandom tags")) {
            if ("dd".equalsIgnoreCase(tags.tagName())) {
                final var tagList = tags.getElementsByTag("ul");
                for (final var li : tagList.getFirst().getElementsByTag("li")) {
                    final var tag = li.getElementsByTag("a").getFirst().text();
                    description.append(tag).append(", ");
                }
            }
        }

        if (StringUtils.endsWith(description.toString(), ", ")) {
            description.delete(description.length() - 2, description.length());
        }

        description.append("\n\n**Relationships**: ");
        for (final var tags : workMetaGroup.getElementsByClass("relationship tags")) {
            if ("dd".equalsIgnoreCase(tags.tagName())) {
                final var tagList = tags.getElementsByTag("ul");
                for (final var li : tagList.getFirst().getElementsByTag("li")) {
                    final var tag = li.getElementsByTag("a").getFirst().text();
                    description.append(tag).append(", ");
                }
            }
        }

        if (StringUtils.endsWith(description.toString(), ", ")) {
            description.delete(description.length() - 2, description.length());
        }

        description.append("\n\n**Characters**: ");
        for (final var tags : workMetaGroup.getElementsByClass("character tags")) {
            if ("dd".equalsIgnoreCase(tags.tagName())) {
                final var tagList = tags.getElementsByTag("ul");
                for (final var li : tagList.getFirst().getElementsByTag("li")) {
                    final var tag = li.getElementsByTag("a").getFirst().text();
                    description.append(tag).append(", ");
                }
            }
        }

        if (StringUtils.endsWith(description.toString(), ", ")) {
            description.delete(description.length() - 2, description.length());
        }

        description.append("\n\n**Tags**: ");
        for (final var tags : workMetaGroup.getElementsByClass("freeform tags")) {
            if ("dd".equalsIgnoreCase(tags.tagName())) {
                final var tagList = tags.getElementsByTag("ul");
                for (final var li : tagList.getFirst().getElementsByTag("li")) {
                    final var tag = li.getElementsByTag("a").getFirst().text();
                    description.append(tag).append(", ");
                }
            }
        }

        if (StringUtils.endsWith(description.toString(), ", ")) {
            description.delete(description.length() - 2, description.length());
        }

        String words = null;
        String chapters = null;
        for (final var outerStats : workMetaGroup.getElementsByClass("stats")) {
            if ("dd".equalsIgnoreCase(outerStats.tagName())) {
                final var innerStats = outerStats.getElementsByTag("dl").getFirst();
                for (final var ddStats : innerStats.getElementsByTag("dd")) {
                    if ("words".equalsIgnoreCase(ddStats.className())) {
                        words = ddStats.text();
                    }
                    if ("chapters".equalsIgnoreCase(ddStats.className())) {
                        chapters = ddStats.text();
                    }
                }
            }
        }

        final var language = new StringBuilder("Language: ");
        for (final var tags : workMetaGroup.getElementsByClass("language")) {
            if ("dd".equalsIgnoreCase(tags.tagName())) {
                language.append(tags.text());
                break;
            }
        }

        var color = new Color(144, 7, 7);
        if (StringUtils.containsIgnoreCase(rating, "teen")) {
            color = new Color(214, 197, 10);
        } else if (StringUtils.containsIgnoreCase(rating, "general")) {
            color = new Color(118, 163, 5);
        } else if (StringUtils.containsIgnoreCase(rating, "not rated")) {
            color = new Color(234, 234, 229);
        } else if (StringUtils.containsIgnoreCase(rating, "mature")) {
            color = new Color(212, 108, 5);
        }

        return Constants.simpleEmbed(
                "AO3 Summary Report",
                description.toString(),
                null,
                "%s | %s | %s".formatted("Words: " + words, "Chapters: " + chapters, language),
                color,
                "https://cdn.discordapp.com/attachments/707453916882665552/780261925212520508/wITXDY67Xw1sAAAAABJRU5ErkJggg.png",
                false);
    }

    private String getWork(final String link) throws Exception {
        final var url = UriComponentsBuilder.fromUriString(link)
                .replaceQuery(StringUtils.EMPTY)
                .queryParam("view_adult", "true")
                .queryParam("view_full_work", "true")
                .build()
                .toString();

        final var headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE);
        headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);

        final var response = doWithLock(() -> restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class));
        return response.getBody();
    }

    private record Data(JsonNode[] embeds) {}

}
