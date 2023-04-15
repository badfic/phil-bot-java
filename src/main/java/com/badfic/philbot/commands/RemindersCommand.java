package com.badfic.philbot.commands;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.Reminder;
import com.badfic.philbot.data.ReminderDao;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.service.MinuteTickable;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class RemindersCommand extends BaseNormalCommand implements MinuteTickable {

    private final ReminderDao reminderDao;

    public RemindersCommand(ReminderDao reminderDao) {
        name = "reminders";
        aliases = new String[] {"reminder"};
        help = """
                Type "remind me in x minutes/hours/days to wash the garage" to be reminded by John.
                `!!reminders` lists all reminders
                `!!reminders 5` Show reminder number 5
                `!!reminders delete 5` Delete reminder number 5""";
        this.reminderDao = reminderDao;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (StringUtils.isBlank(event.getArgs())) {
            Collection<Reminder> reminders = reminderDao.findAll();

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

            event.reply(Constants.simpleEmbed("Reminders", description.toString()));
        } else if (StringUtils.containsIgnoreCase(event.getArgs(), "delete")) {
            String number = event.getArgs().replace("delete", "").trim();

            try {
                long toDelete = Long.parseLong(number);
                if (reminderDao.existsById(toDelete)) {
                    reminderDao.deleteById(toDelete);
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
                Optional<Reminder> optionalReminder = reminderDao.findById(toLookup);

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
                        event.getChannel().sendFiles(FileUpload.fromData(finalString.getBytes(), "active-reminders.txt")).queue();
                    } else {
                        event.reply(finalString);
                    }
                } else {
                    event.replyError("Reminder #" + toLookup + " does not exist");
                }
            } catch (NumberFormatException e) {
                event.replyError("Failed to parse number " + number);
            }
        } else {
            event.replyError("Unrecognized reminder command");
        }
    }

    @Override
    public void runMinutelyTask() {
        LocalDateTime now = LocalDateTime.now();
        for (Reminder reminder : reminderDao.findAll()) {
            if (now.isEqual(reminder.getDueDate()) || now.isAfter(reminder.getDueDate())) {
                TextChannel textChannelById = philJda.getTextChannelById(reminder.getChannelId());

                if (textChannelById != null) {
                    SwampyGamesConfig swampyGamesConfig = swampyGamesConfigDao.get();
                    discordWebhookSendService.sendMessage(textChannelById.getIdLong(), swampyGamesConfig.getJohnNickname(), swampyGamesConfig.getJohnAvatar(),
                            "(reminder #" + reminder.getId() + ") <@!" + reminder.getUserId() + "> " + reminder.getReminder());
                    reminderDao.deleteById(reminder.getId());
                }
            }
        }
    }
}
