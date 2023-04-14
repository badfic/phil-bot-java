package com.badfic.philbot.data.hungersim;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("hg_pronoun")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Pronoun {

    @Id
    private Long id;

    @Column
    private String subject;

    @Column
    private String object;

    @Column
    private String possessive;

    @Column
    private String self;

    public Pronoun(String subject, String object, String possessive, String self) {
        this.subject = subject;
        this.object = object;
        this.possessive = possessive;
        this.self = self;
    }

}
