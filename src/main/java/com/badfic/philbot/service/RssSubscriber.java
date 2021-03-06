package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.RssEntry;
import com.badfic.philbot.data.phil.RssEntryRepository;
import com.google.common.collect.ImmutableSet;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import java.io.ByteArrayInputStream;
import java.lang.invoke.MethodHandles;
import java.util.Set;
import javax.annotation.Resource;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

@Component
public class RssSubscriber extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Set<String> FEEDS = ImmutableSet.of(
            "https://archiveofourown.org/tags/39926683/feed.atom",
            "https://archiveofourown.org/tags/41072152/feed.atom"
    );

    @Resource
    private RssEntryRepository rssEntryRepository;

    @Resource
    private Ao3MetadataParser ao3MetadataParser;

    @Scheduled(cron = "0 0,30 * * * ?", zone = "GMT")
    public void run() {
        threadPoolTaskExecutor.submit(this::refresh);
    }

    private void refresh() {
        logger.info("Checking RSS feeds");
        TextChannel sfwChannel = philJda.getTextChannelsByName("fic-recs", false).get(0);
        TextChannel nsfwChannel = philJda.getTextChannelsByName("nsfw-fic-recs", false).get(0);

        for (String url : FEEDS) {
            try {
                LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
                headers.add(HttpHeaders.ACCEPT, "application/atom+xml");
                headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
                SyndFeed feed = new SyndFeedInput().build(new XmlReader(new ByteArrayInputStream(response.getBody().getBytes())));

                boolean initialLoad = rssEntryRepository.countByFeedUrl(url) == 0;
                long addedLinks = 0;

                for (SyndEntry entry : feed.getEntries()) {
                    String link = entry.getLink();

                    if (!rssEntryRepository.existsById(link)) {
                        rssEntryRepository.save(new RssEntry(link, url));
                        addedLinks++;

                        if (!initialLoad) {
                            if (StringUtils.containsIgnoreCase(entry.getDescription().getValue(), "teen and up audience")
                                    || StringUtils.containsIgnoreCase(entry.getDescription().getValue(), "general audience")) {
                                if (!ao3MetadataParser.parseLink(link, sfwChannel.getName())) {
                                    sfwChannel.sendMessage("\uD83D\uDCF0\n" + link).queue();
                                }
                            } else if (StringUtils.containsIgnoreCase(entry.getDescription().getValue(), "mature")
                                    || StringUtils.containsIgnoreCase(entry.getDescription().getValue(), "explicit")
                                    || StringUtils.containsIgnoreCase(entry.getDescription().getValue(), "not rated")) {
                                if (!ao3MetadataParser.parseLink(link, nsfwChannel.getName())) {
                                    nsfwChannel.sendMessage("\uD83D\uDCF0\n" + link).queue();
                                }
                            }
                        }
                    }
                }

                if (initialLoad) {
                    philJda.getTextChannelsByName(Constants.TEST_CHANNEL, false)
                            .get(0)
                            .sendMessage("Successfully loaded " + addedLinks + " rss entries into db from " + url)
                            .queue();
                }
            } catch (Exception e) {
                logger.error("Failed to parse rss feed {}", url, e);
                honeybadgerReporter.reportError(e, null, "Failed to parse rss feed " + url);
            }
        }
        logger.info("Finished checking RSS feeds");
    }
}
