package com.badfic.philbot.data;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("hunger_games_winner")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "messageId")
public class HungerGamesWinnerEntity {

    @Id
    private long messageId;

    @Column
    private String message;

    @Column
    private LocalDateTime timeCreated;

    @Column
    private LocalDateTime timeEdited;

}
