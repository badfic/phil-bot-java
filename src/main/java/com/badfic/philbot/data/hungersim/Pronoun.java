package com.badfic.philbot.data.hungersim;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "hg_pronoun")
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

    public Pronoun() {
    }

    public Pronoun(String subject, String object, String possessive, String self) {
        this.subject = subject;
        this.object = object;
        this.possessive = possessive;
        this.self = self;
    }

    public Long getId() {
        return id;
    }

    public String getSubject() {

        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getPossessive() {
        return possessive;
    }

    public void setPossessive(String possessive) {
        this.possessive = possessive;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pronoun pronoun = (Pronoun) o;
        return Objects.equals(id, pronoun.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
