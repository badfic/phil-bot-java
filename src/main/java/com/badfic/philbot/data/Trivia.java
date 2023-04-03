package com.badfic.philbot.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trivia")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Trivia {

    @Id
    private UUID id;

    @Column
    private String question;

    @Column(name = "answer_a")
    private String answerA;

    @Column(name = "answer_b")
    private String answerB;

    @Column(name = "answer_c")
    private String answerC;

    @Column
    private short correctAnswer;

}
