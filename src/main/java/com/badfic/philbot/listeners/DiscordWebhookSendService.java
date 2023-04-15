package com.badfic.philbot.listeners;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.data.ChannelWebhookEntity;
import com.badfic.philbot.data.ChannelWebhookRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class DiscordWebhookSendService {
    private final BaseConfig baseConfig;
    private final RestTemplate restTemplate;
    private final ChannelWebhookRepository channelWebhookRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    public void sendMessage(long channelId, String username, String avatarUrl, String content) {
        Optional<ChannelWebhookEntity> optionalChannel = channelWebhookRepository.findById(channelId);

        ChannelWebhookEntity channelWebhook;
        if (optionalChannel.isEmpty()) {
            CreateWebhookResponse createWebhookResponse = createWebhook(channelId, "phil");
            channelWebhook = jdbcAggregateTemplate.insert(new ChannelWebhookEntity(channelId, createWebhookResponse.id(), createWebhookResponse.token()));
        } else {
            channelWebhook = optionalChannel.get();
        }

        publishWebhook(channelWebhook.getWebhookId(), channelWebhook.getToken(), username, avatarUrl, content);
    }

    private CreateWebhookResponse createWebhook(long channelId, String name) {
        String endpoint = "https://discordapp.com/api/channels/{channelId}/webhooks";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bot " + baseConfig.philBotToken);

        return restTemplate.exchange(endpoint, HttpMethod.POST, new HttpEntity<>(new CreateWebhookRequest(name), headers),
                CreateWebhookResponse.class, channelId).getBody();
    }

    private void publishWebhook(long webhookId, String token, String username, String avatarUrl, String content) {
        String endpoint = "https://discordapp.com/api/webhooks/{webhookId}/{webhookToken}";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bot " + baseConfig.philBotToken);

        restTemplate.exchange(endpoint, HttpMethod.POST, new HttpEntity<>(new ExecuteWebhook(content, username, avatarUrl), headers),
                String.class, webhookId, token);
    }

    record CreateWebhookRequest(String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CreateWebhookResponse(long id, String token) {}

    record ExecuteWebhook(String content, String username, @JsonProperty("avatar_url") String avatarUrl) {}
}
