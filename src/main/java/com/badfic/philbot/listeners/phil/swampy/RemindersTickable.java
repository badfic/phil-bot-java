package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.data.phil.Reminder;
import com.badfic.philbot.data.phil.ReminderRepository;
import com.badfic.philbot.service.MinuteTickable;
import java.time.LocalDateTime;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class RemindersTickable implements MinuteTickable {

    private final ReminderRepository reminderRepository;
    private final JDA johnJda;

    public RemindersTickable(ReminderRepository reminderRepository, @Qualifier("johnJda") JDA johnJda) {
        this.reminderRepository = reminderRepository;
        this.johnJda = johnJda;
    }

    @Override
    public void runMinutelyTask() {
        LocalDateTime now = LocalDateTime.now();
        for (Reminder reminder : reminderRepository.findByDueDateBefore(now)) {
            TextChannel textChannelById = johnJda.getTextChannelById(reminder.getChannelId());

            if (textChannelById != null) {
                textChannelById.sendMessage("(reminder #" + reminder.getId() + ") <@!" + reminder.getUserId() + "> " + reminder.getReminder()).queue();
                reminderRepository.deleteById(reminder.getId());
            }
        }
    }

}
