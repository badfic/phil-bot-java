package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.data.phil.Reminder;
import com.badfic.philbot.data.phil.ReminderRepository;
import com.badfic.philbot.service.MinuteTickable;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class RemindersTickable implements MinuteTickable {

    @Resource
    private ReminderRepository reminderRepository;

    @Resource(name = "johnJda")
    @Lazy
    private JDA johnJda;

    @Override
    public void run() {
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
