package com.badfic.philbot.data;

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
@Table(name = "snarky_reminder_response")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class SnarkyReminderResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String response;

    public SnarkyReminderResponse(String response) {
        this.response = response;
    }

}
