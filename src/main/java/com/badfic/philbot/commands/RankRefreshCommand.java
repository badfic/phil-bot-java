package com.badfic.philbot.commands;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class RankRefreshCommand extends BaseNormalCommand {

    private final RestTemplate restTemplate;
    private final BaseConfig baseConfig;

    public RankRefreshCommand(RestTemplate restTemplate, BaseConfig baseConfig) {
        this.restTemplate = restTemplate;
        this.baseConfig = baseConfig;
        this.name = "rankRefresh";
        this.aliases = new String[] {"refreshRanks"};
        this.requiredRole = Constants.ADMIN_ROLE;
        this.help = "Manually refresh the ranks spreadsheet from Airtable";

        try {
            Rank.init(restTemplate, baseConfig.airtableApiToken);
        } catch (Exception e) {
            log.error("Failed to load ranks from Airtable", e);
            throw new IllegalStateException("Failed to load ranks from Airtable", e);
        }
    }

    @Override
    public void execute(CommandEvent event) {
        try {
            Rank.init(restTemplate, baseConfig.airtableApiToken);
            event.replySuccess("Refreshed ranks from Airtable");
        } catch (Exception e) {
            log.error("Failed to load ranks from Airtable", e);
            event.replyError("Failed to load ranks from Airtable");
        }
    }
}
