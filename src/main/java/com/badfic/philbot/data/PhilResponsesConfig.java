package com.badfic.philbot.data;

import com.badfic.philbot.model.PhilConfigJson;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "phil_responses_config")
public class PhilResponsesConfig {
    public static final Short SINGLETON_ID = 1;

    @Id
    private Short id;

    @Column
    @Convert(converter = PhilConfigJsonConverter.class)
    private PhilConfigJson nsfwConfig;

    @Column
    @Convert(converter = PhilConfigJsonConverter.class)
    private PhilConfigJson sfwConfig;

    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
    }

    public PhilConfigJson getNsfwConfig() {
        return nsfwConfig;
    }

    public void setNsfwConfig(PhilConfigJson nsfwConfig) {
        this.nsfwConfig = nsfwConfig;
    }

    public PhilConfigJson getSfwConfig() {
        return sfwConfig;
    }

    public void setSfwConfig(PhilConfigJson sfwConfig) {
        this.sfwConfig = sfwConfig;
    }
}
