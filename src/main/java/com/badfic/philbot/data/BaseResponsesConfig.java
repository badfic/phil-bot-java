package com.badfic.philbot.data;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

@Getter
@Setter
public abstract class BaseResponsesConfig {
    public static final Short SINGLETON_ID = 1;

    @Id
    private Short id;

    @Column
    private GenericBotResponsesConfigJson nsfwConfig;

    @Column
    private GenericBotResponsesConfigJson sfwConfig;

}
