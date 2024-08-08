package com.badfic.philbot.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("meme_command")
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

    public MemeCommandEntity(final String name, final String url) {
        this.name = name;
        this.url = url;
    }

}
