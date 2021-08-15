package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import com.google.common.annotations.VisibleForTesting;
import java.awt.Color;
import java.lang.invoke.MethodHandles;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

@Component
public class Ao3MetadataParser extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public boolean parseLink(String link, String channelName) {
        try {
            URIBuilder uriBuilder = new URIBuilder(link);
            uriBuilder.clearParameters();
            uriBuilder.addParameter("view_adult", "true");
            uriBuilder.addParameter("view_full_work", "true");
            LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            headers.add(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE);
            headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);
            ResponseEntity<String> loginResponse = restTemplate.exchange(uriBuilder.build().toString(), HttpMethod.GET, new HttpEntity<>(headers), String.class);
            String work = loginResponse.getBody();
            return parseWork(link, work, channelName);
        } catch (Exception e) {
            logger.error("Failed to download ao3 [link={}]", link, e);
            honeybadgerReporter.reportError(e, null, "Failed to download ao3 link: " + link);
            return false;
        }
    }

    @VisibleForTesting
    boolean parseWork(String originalLink, String work, String channelName) {
        TextChannel channel = philJda.getTextChannelsByName(channelName, false).get(0);

        try {
            Document document = Jsoup.parse(work);

            String title = document.getElementsByClass("title heading").get(0).text();

            Elements authorElement = document.getElementsByAttributeValueContaining("rel", "author");
            String authorLink = authorElement.get(0).attr("href");
            String authorName = authorElement.get(0).text();

            StringBuilder description = new StringBuilder();
            description.append("[**")
                    .append(title)
                    .append("**](")
                    .append(originalLink)
                    .append(")\nby [")
                    .append(authorName)
                    .append("](https://archiveofourown.org")
                    .append(authorLink)
                    .append(")\n\n**Rating**: ");

            String rating = "Explicit";
            Element workMetaGroup = document.getElementsByClass("work meta group").get(0);

            for (Element tags : workMetaGroup.getElementsByClass("rating tags")) {
                if (tags.tagName().equalsIgnoreCase("dd")) {
                    Elements tagList = tags.getElementsByTag("ul");
                    for (Element li : tagList.get(0).getElementsByTag("li")) {
                        rating = li.getElementsByTag("a").get(0).text();
                        description.append(rating);
                        break;
                    }
                }
            }

            description.append("\n\n**Warnings**: ");
            for (Element tags : workMetaGroup.getElementsByClass("warning tags")) {
                if (tags.tagName().equalsIgnoreCase("dd")) {
                    Elements tagList = tags.getElementsByTag("ul");
                    for (Element li : tagList.get(0).getElementsByTag("li")) {
                        String tag = li.getElementsByTag("a").get(0).text();
                        description.append(tag).append(", ");
                    }
                }
            }

            if (StringUtils.endsWith(description.toString(), ", ")) {
                description.delete(description.length() - 2, description.length());
            }

            description.append("\n\n**Categories**: ");
            for (Element tags : workMetaGroup.getElementsByClass("category tags")) {
                if (tags.tagName().equalsIgnoreCase("dd")) {
                    Elements tagList = tags.getElementsByTag("ul");
                    for (Element li : tagList.get(0).getElementsByTag("li")) {
                        String tag = li.getElementsByTag("a").get(0).text();
                        description.append(tag).append(", ");
                    }
                }
            }

            if (StringUtils.endsWith(description.toString(), ", ")) {
                description.delete(description.length() - 2, description.length());
            }

            description.append("\n\n**Summary**: ");
            Elements summaryModule = document.getElementsByClass("summary module");
            String summary = summaryModule.get(0).getElementsByTag("blockquote").text();
            description.append(summary.length() <= 800 ? summary : (summary.substring(0, 797) + "..."));

            description.append("\n\n**Fandoms**: ");
            for (Element tags : workMetaGroup.getElementsByClass("fandom tags")) {
                if (tags.tagName().equalsIgnoreCase("dd")) {
                    Elements tagList = tags.getElementsByTag("ul");
                    for (Element li : tagList.get(0).getElementsByTag("li")) {
                        String tag = li.getElementsByTag("a").get(0).text();
                        description.append(tag).append(", ");
                    }
                }
            }

            if (StringUtils.endsWith(description.toString(), ", ")) {
                description.delete(description.length() - 2, description.length());
            }

            description.append("\n\n**Relationships**: ");
            for (Element tags : workMetaGroup.getElementsByClass("relationship tags")) {
                if (tags.tagName().equalsIgnoreCase("dd")) {
                    Elements tagList = tags.getElementsByTag("ul");
                    for (Element li : tagList.get(0).getElementsByTag("li")) {
                        String tag = li.getElementsByTag("a").get(0).text();
                        description.append(tag).append(", ");
                    }
                }
            }

            if (StringUtils.endsWith(description.toString(), ", ")) {
                description.delete(description.length() - 2, description.length());
            }

            description.append("\n\n**Characters**: ");
            for (Element tags : workMetaGroup.getElementsByClass("character tags")) {
                if (tags.tagName().equalsIgnoreCase("dd")) {
                    Elements tagList = tags.getElementsByTag("ul");
                    for (Element li : tagList.get(0).getElementsByTag("li")) {
                        String tag = li.getElementsByTag("a").get(0).text();
                        description.append(tag).append(", ");
                    }
                }
            }

            if (StringUtils.endsWith(description.toString(), ", ")) {
                description.delete(description.length() - 2, description.length());
            }

            description.append("\n\n**Tags**: ");
            for (Element tags : workMetaGroup.getElementsByClass("freeform tags")) {
                if (tags.tagName().equalsIgnoreCase("dd")) {
                    Elements tagList = tags.getElementsByTag("ul");
                    for (Element li : tagList.get(0).getElementsByTag("li")) {
                        String tag = li.getElementsByTag("a").get(0).text();
                        description.append(tag).append(", ");
                    }
                }
            }

            if (StringUtils.endsWith(description.toString(), ", ")) {
                description.delete(description.length() - 2, description.length());
            }

            String words = null;
            String chapters = null;
            for (Element outerStats : workMetaGroup.getElementsByClass("stats")) {
                if (outerStats.tagName().equalsIgnoreCase("dd")) {
                    Element innerStats = outerStats.getElementsByTag("dl").get(0);
                    for (Element ddStats : innerStats.getElementsByTag("dd")) {
                        if (ddStats.className().equalsIgnoreCase("words")) {
                            words = ddStats.text();
                        }
                        if (ddStats.className().equalsIgnoreCase("chapters")) {
                            chapters = ddStats.text();
                        }
                    }
                }
            }

            StringBuilder language = new StringBuilder("Launguage: ");
            for (Element tags : workMetaGroup.getElementsByClass("language")) {
                if (tags.tagName().equalsIgnoreCase("dd")) {
                    language.append(tags.text());
                    break;
                }
            }

            Color color = new Color(144, 7, 7);
            if (StringUtils.containsIgnoreCase(rating, "teen")) {
                color = new Color(214, 197, 10);
            } else if (StringUtils.containsIgnoreCase(rating, "general")) {
                color = new Color(118, 163, 5);
            } else if (StringUtils.containsIgnoreCase(rating, "not rated")) {
                color = new Color(234, 234, 229);
            } else if (StringUtils.containsIgnoreCase(rating, "mature")) {
                color = new Color(212, 108, 5);
            }

            String footer = "%s | %s | %s";
            MessageEmbed messageEmbed = Constants.simpleEmbed(
                    "Phil's AO3 Summary Report",
                    description.toString(),
                    null,
                    String.format(footer, "Words: " + words, "Chapters: " + chapters, language),
                    color,
                    "https://cdn.discordapp.com/attachments/707453916882665552/780261925212520508/wITXDY67Xw1sAAAAABJRU5ErkJggg.png");

            channel.sendMessageEmbeds(messageEmbed).queue();
            return true;
        } catch (Exception e) {
            logger.error("Failed to parse ao3 [link={}]", originalLink, e);
            honeybadgerReporter.reportError(e, null, "Failed to parse ao3 link " + originalLink);
            return false;
        }
    }

}
