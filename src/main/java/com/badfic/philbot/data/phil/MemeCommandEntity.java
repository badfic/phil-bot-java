package com.badfic.philbot.data.phil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "meme_command")
public class MemeCommandEntity {

    @Id
    private String name;

    @Column
    private String url;

    @Transient
    private Boolean urlIsImage;

    @Transient
    private Boolean urlIsList;

    public MemeCommandEntity() {
    }

    public MemeCommandEntity(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getUrlIsImage() {
        return urlIsImage;
    }

    public void setUrlIsImage(Boolean urlIsImage) {
        this.urlIsImage = urlIsImage;
    }

    public Boolean getUrlIsList() {
        return urlIsList;
    }

    public void setUrlIsList(Boolean urlIsList) {
        this.urlIsList = urlIsList;
    }
}
