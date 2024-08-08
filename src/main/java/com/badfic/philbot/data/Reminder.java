package com.badfic.philbot.data;

import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("reminder")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Reminder {

    @Id
    private long id;

    @Column
    private long userId;

    @Column
    private long channelId;

    @Column
    private String reminder;

    @Column
    private LocalDateTime dueDate;

    public Reminder(final long userId, final long channelId, final String reminder, final LocalDateTime dueDate) {
        this.userId = userId;
        this.channelId = channelId;
        this.reminder = reminder;
        this.dueDate = dueDate;
    }

}
