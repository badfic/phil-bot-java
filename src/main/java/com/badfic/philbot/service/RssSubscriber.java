package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.RssEntry;
import com.badfic.philbot.data.RssEntryRepository;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
class RssSubscriber extends BaseService {
    private static final String[] FEEDS = {
            "https://archiveofourown.org/tags/39926683/feed.atom",
            "https://archiveofourown.org/tags/41072152/feed.atom"
    };

    private final RssEntryRepository rssEntryRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;
    private final Ao3MetadataParser ao3MetadataParser;

    @Scheduled(cron = "${swampy.schedule.rss}", zone = "${swampy.schedule.timezone}")
    void run() {
        executorService.execute(this::refresh);
    }

    private void refresh() {
        log.info("Checking RSS feeds");
        final var sfwChannel = philJda.getTextChannelsByName("fic-recs", false).getFirst();
        final var nsfwChannel = philJda.getTextChannelsByName("nsfw-fic-recs", false).getFirst();

        for (final var url : FEEDS) {
            try {
                final var headers = new HttpHeaders();
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_ATOM_XML));
                headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);

                final var response = ao3MetadataParser.doWithLock(() ->
                        restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class));

                final var feed = new SyndFeedInput().build(new XmlReader(new FastByteArrayInputStream(response.getBody())));

                final var initialLoad = rssEntryRepository.countByFeedUrl(url) == 0;

                var addedLinks = 0L;
                for (final var entry : feed.getEntries()) {
                    final var link = entry.getLink();

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
                            .getFirst()
                            .sendMessage("Successfully loaded " + addedLinks + " rss entries into db from " + url)
                            .queue();
                }
            } catch (final Exception e) {
                log.error("Failed to parse rss feed {}", url, e);
            }
        }
        log.info("Finished checking RSS feeds");
    }
}
