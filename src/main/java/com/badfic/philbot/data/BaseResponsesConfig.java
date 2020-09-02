package com.badfic.philbot.data;

import com.badfic.philbot.model.GenericBotResponsesConfigJson;
import com.badfic.philbot.model.GenericBotResponsesConfigJsonConverter;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
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

    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
    }

    public GenericBotResponsesConfigJson getNsfwConfig() {
        return nsfwConfig;
    }

    public void setNsfwConfig(GenericBotResponsesConfigJson nsfwConfig) {
        this.nsfwConfig = nsfwConfig;
    }

    public GenericBotResponsesConfigJson getSfwConfig() {
        return sfwConfig;
    }

    public void setSfwConfig(GenericBotResponsesConfigJson sfwConfig) {
        this.sfwConfig = sfwConfig;
    }
}
