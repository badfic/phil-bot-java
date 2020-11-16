package com.badfic.philbot.data.phil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "snarky_reminder_response")
public class SnarkyReminderResponse {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    @Column
    private String response;

    public SnarkyReminderResponse(String response) {
        this.response = response;
    }

    public SnarkyReminderResponse() {
    }

    public long getId() {
        return id;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
