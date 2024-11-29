package com.badfic.philbot.commands.bang;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.Rank;
import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
class RankRefreshCommand extends BaseBangCommand {

    public RankRefreshCommand(final RestTemplate restTemplate, final BaseConfig baseConfig) {
        this.name = "rankRefresh";
        this.aliases = new String[] {"refreshRanks"};
        this.requiredRole = Constants.ADMIN_ROLE;
        this.help = "Manually refresh the ranks spreadsheet from Airtable";

        try {
            Rank.init(restTemplate, baseConfig.airtableApiToken);
        } catch (final Exception e) {
            log.error("Failed to load ranks from Airtable", e);
            throw new IllegalStateException("Failed to load ranks from Airtable", e);
        }
    }

    @Override
    public void execute(final CommandEvent event) {
        try {
            Rank.init(restTemplate, baseConfig.airtableApiToken);
            event.replySuccess("Refreshed ranks from Airtable");
        } catch (final Exception e) {
            log.error("Failed to load ranks from Airtable", e);
            event.replyError("Failed to load ranks from Airtable");
        }
    }
}
