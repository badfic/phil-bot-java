package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.phil.Reminder;
import com.badfic.philbot.data.phil.ReminderRepository;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class RemindersCommand extends BaseSwampy implements PhilMarker {

    @Resource
    private ReminderRepository reminderRepository;

    public RemindersCommand() {
        name = "reminders";
        aliases = new String[] {"reminder"};
        help = "!!reminders\n" +
                "Type 'remind me in x minutes/hours/days to wash the garage' to be reminded\n" +
                "`!!reminders` lists all reminders\n" +
                "`!!reminders 5` Show reminder number 5\n" +
                "`!!reminders delete 5` Delete reminder number 5";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (StringUtils.isBlank(event.getArgs())) {
            List<Reminder> reminders = reminderRepository.findAll();

            StringBuilder description = new StringBuilder();
            for (Reminder reminder : reminders) {
                description.append('#')
                        .append(reminder.getId())
                        .append(" <@!")
                        .append(reminder.getUserId())
                        .append("> ")
                        .append(reminder.getReminder())
                        .append(" in ")
                        .append(Constants.prettyPrintDuration(Duration.between(LocalDateTime.now(), reminder.getDueDate())))
                        .append('\n');
            }

            event.reply(simpleEmbed("Reminders", description.toString()));
        } else if (StringUtils.containsIgnoreCase(event.getArgs(), "delete")) {
            String number = event.getArgs().replace("delete", "").trim();

            try {
                long toDelete = Long.parseLong(number);
                if (reminderRepository.existsById(toDelete)) {
                    reminderRepository.deleteById(toDelete);
                    event.replySuccess("Deleted reminder #" + toDelete);
                } else {
                    event.replySuccess("Reminder #" + toDelete + " doesn't exist");
                }
            } catch (NumberFormatException e) {
                event.replyError("Failed to parse number " + number);
            }
        } else if (StringUtils.isNotBlank(event.getArgs())) {
            String number = event.getArgs().trim();

            try {
                long toLookup = Long.parseLong(number);
                Optional<Reminder> optionalReminder = reminderRepository.findById(toLookup);

                if (optionalReminder.isPresent()) {
                    Reminder reminder = optionalReminder.get();
                    StringBuilder description = new StringBuilder();
                    description.append('#')
                            .append(reminder.getId())
                            .append(" <@!")
                            .append(reminder.getUserId())
                            .append("> ")
                            .append(reminder.getReminder())
                            .append(" in ")
                            .append(Constants.prettyPrintDuration(Duration.between(LocalDateTime.now(), reminder.getDueDate())));

                    String finalString = description.toString();
                    if (finalString.length() > 2_000) {
                        event.getChannel().sendFile(finalString.getBytes(), "active-reminders.txt").queue();
                    } else {
                        event.reply(finalString);
                    }
                } else {
                    event.replyError("Reminder #" + toLookup + " does not exist");;
                }
            } catch (NumberFormatException e) {
                event.replyError("Failed to parse number " + number);
            }
        } else {
            event.replyError("Unrecognized reminder command");
        }
    }
}
