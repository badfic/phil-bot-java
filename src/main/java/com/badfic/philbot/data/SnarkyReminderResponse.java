package com.badfic.philbot.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("snarky_reminder_response")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class SnarkyReminderResponse {

    @Id
    private long id;

    @Column
    private String response;

    public SnarkyReminderResponse(String response) {
        this.response = response;
    }

}
