package com.badfic.philbot.data;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("channel_webhook")
@EqualsAndHashCode(of = "channelId")
@NoArgsConstructor
@AllArgsConstructor
public class ChannelWebhookEntity {
    @Id
    private long channelId;

    @Column
    private long webhookId;

    @Column
    private String token;
}
