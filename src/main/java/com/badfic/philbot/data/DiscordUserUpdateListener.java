package com.badfic.philbot.data;

import java.time.LocalDateTime;
import org.springframework.data.relational.core.conversion.MutableAggregateChange;
import org.springframework.data.relational.core.mapping.event.BeforeSaveCallback;
import org.springframework.stereotype.Component;

@Component
public class DiscordUserUpdateListener implements BeforeSaveCallback<DiscordUser> {
    @Override
    public DiscordUser onBeforeSave(DiscordUser aggregate, MutableAggregateChange<DiscordUser> aggregateChange) {
        aggregate.setUpdateTime(LocalDateTime.now());
        return aggregate;
    }
}
