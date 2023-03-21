package com.badfic.philbot.data;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseResponsesConfig {
    public static final Short SINGLETON_ID = 1;

    @Id
    private Short id;

    @Column
    @Convert(converter = GenericBotResponsesConfigJsonConverter.class)
    private GenericBotResponsesConfigJson nsfwConfig;

    @Column
    @Convert(converter = GenericBotResponsesConfigJsonConverter.class)
    private GenericBotResponsesConfigJson sfwConfig;

}
