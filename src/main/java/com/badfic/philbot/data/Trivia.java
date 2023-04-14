package com.badfic.philbot.data;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("trivia")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Trivia {

    @Id
    private UUID id;

    @Column
    private String question;

    @Column("answer_a")
    private String answerA;

    @Column("answer_b")
    private String answerB;

    @Column("answer_c")
    private String answerC;

    @Column
    private short correctAnswer;

}
