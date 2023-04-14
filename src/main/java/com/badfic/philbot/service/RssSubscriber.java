package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.RssEntry;
import com.badfic.philbot.data.RssEntryRepository;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import java.io.ByteArrayInputStream;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

@Component
@Slf4j
public class RssSubscriber extends BaseService {
    private static final String[] FEEDS = {
            "https://archiveofourown.org/tags/39926683/feed.atom",
            "https://archiveofourown.org/tags/41072152/feed.atom"
    };

    private final RssEntryRepository rssEntryRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;
    private final Ao3MetadataParser ao3MetadataParser;

    public RssSubscriber(RssEntryRepository rssEntryRepository, JdbcAggregateTemplate jdbcAggregateTemplate, Ao3MetadataParser ao3MetadataParser) {
        this.rssEntryRepository = rssEntryRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
        this.ao3MetadataParser = ao3MetadataParser;
    }

    @Scheduled(cron = "${swampy.schedule.rss}", zone = "${swampy.schedule.timezone}")
    public void run() {
        threadPoolTaskExecutor.execute(this::refresh);
    }

    private void refresh() {
        log.info("Checking RSS feeds");
        TextChannel sfwChannel = philJda.getTextChannelsByName("fic-recs", false).get(0);
        TextChannel nsfwChannel = philJda.getTextChannelsByName("nsfw-fic-recs", false).get(0);

        for (String url : FEEDS) {
            try {
                LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
                headers.add(HttpHeaders.ACCEPT, "application/atom+xml");
                headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);
                ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
                SyndFeed feed = new SyndFeedInput().build(new XmlReader(new ByteArrayInputStream(response.getBody())));

                boolean initialLoad = rssEntryRepository.countByFeedUrl(url) == 0;
                long addedLinks = 0;

                for (SyndEntry entry : feed.getEntries()) {
                    String link = entry.getLink();

                    if (!rssEntryRepository.existsById(link)) {
                        jdbcAggregateTemplate.insert(new RssEntry(link, url));
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
                log.error("Failed to parse rss feed {}", url, e);
            }
        }
        log.info("Finished checking RSS feeds");
    }
}
