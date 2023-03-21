package com.badfic.philbot.data.phil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "meme_command")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "name")
public class MemeCommandEntity {

    @Id
    private String name;

    @Column
    private String url;

    @Transient
    private Boolean urlIsImage;

    @Transient
    private Boolean urlIsList;

    public MemeCommandEntity(String name, String url) {
        this.name = name;
        this.url = url;
    }

}
