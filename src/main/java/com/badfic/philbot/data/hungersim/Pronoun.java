package com.badfic.philbot.data.hungersim;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "hg_pronoun")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Pronoun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
