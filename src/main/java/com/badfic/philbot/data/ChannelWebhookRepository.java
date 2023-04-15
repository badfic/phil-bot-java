package com.badfic.philbot.data;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelWebhookRepository extends ListCrudRepository<ChannelWebhookEntity, Long> {
}
