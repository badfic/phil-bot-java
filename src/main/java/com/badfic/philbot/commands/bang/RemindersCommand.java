package com.badfic.philbot.commands.bang;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.Reminder;
import com.badfic.philbot.data.ReminderDal;
import com.badfic.philbot.data.SnarkyReminderResponseRepository;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.service.OnJdaReady;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RemindersCommand extends BaseBangCommand implements OnJdaReady {

    private final ReminderDal reminderDal;
    private final SnarkyReminderResponseRepository snarkyReminderResponseRepository;

    public RemindersCommand(final ReminderDal reminderDal, final SnarkyReminderResponseRepository snarkyReminderResponseRepository) {
        name = "reminders";
        aliases = new String[] {"reminder"};
        help = """
                Type "remind me in x minutes/hours/days to wash the garage" to be reminded by John.
                `!!reminders` lists all reminders
                `!!reminders 5` Show reminder number 5
                `!!reminders delete 5` Delete reminder number 5""";
        this.reminderDal = reminderDal;
        this.snarkyReminderResponseRepository = snarkyReminderResponseRepository;
    }

    @Override
    public void run() {
        for (final var reminder : reminderDal.findAll()) {
            scheduleTask(() -> remind(reminder.getId()), reminder.getDueDate());
        }
    }

    @Override
    public void execute(final CommandEvent event) {
        final var now = LocalDateTime.now();

        if (StringUtils.isBlank(event.getArgs())) {
            final var reminders = reminderDal.findAll();

            final var description = new StringBuilder();
            for (final var reminder : reminders) {
                description.append('#')
                        .append(reminder.getId())
                        .append(" <@")
                        .append(reminder.getUserId())
                        .append("> ")
                        .append(reminder.getReminder())
                        .append(" in ")
                        .append(Constants.prettyPrintDuration(Duration.between(now, reminder.getDueDate())))
                        .append('\n');
            }

            event.reply(Constants.simpleEmbed("Reminders", description.toString()));
        } else if (StringUtils.containsIgnoreCase(event.getArgs(), "delete")) {
            final var number = event.getArgs().replace("delete", "").trim();

            try {
                final var toDelete = Long.parseLong(number);
                if (reminderDal.existsById(toDelete)) {
                    reminderDal.deleteById(toDelete);
                    event.replySuccess("Deleted reminder #" + toDelete);
                } else {
                    event.replySuccess("Reminder #" + toDelete + " doesn't exist");
                }
            } catch (final NumberFormatException e) {
                event.replyError("Failed to parse number " + number);
            }
        } else if (StringUtils.isNotBlank(event.getArgs())) {
            final var number = event.getArgs().trim();

            try {
                final var toLookup = Long.parseLong(number);
                final var optionalReminder = reminderDal.findById(toLookup);

                if (optionalReminder.isPresent()) {
                    final var reminder = optionalReminder.get();
                    final var description = new StringBuilder();
                    description.append('#')
                            .append(reminder.getId())
                            .append(" <@")
                            .append(reminder.getUserId())
                            .append("> ")
                            .append(reminder.getReminder())
                            .append(" in ")
                            .append(Constants.prettyPrintDuration(Duration.between(now, reminder.getDueDate())));

                    final var finalString = description.toString();
                    if (finalString.length() > 2_000) {
                        event.getChannel().sendFiles(FileUpload.fromData(finalString.getBytes(), "active-reminders.txt")).queue();
                    } else {
                        event.reply(finalString);
                    }
                } else {
                    event.replyError("Reminder #" + toLookup + " does not exist");
                }
            } catch (final NumberFormatException e) {
                event.replyError("Failed to parse number " + number);
            }
        } else {
            event.replyError("Unrecognized reminder command");
        }
    }

    public void addReminder(final Message message, final SwampyGamesConfig swampyGamesConfig) {
        try {
            String reminder;
            final var remindIdx = StringUtils.indexOfIgnoreCase(message.getContentRaw(), "remind");

            var member = message.getMember();
            if (CollectionUtils.isNotEmpty(message.getMentions().getMembers())) {
                member = message.getMentions().getMembers().getFirst();

                reminder = message.getContentRaw().substring(remindIdx + ("remind <@" + member.getIdLong() + "> in ").length());
            } else {
                reminder = message.getContentRaw().substring(remindIdx + "remind me in ".length());
            }
            final var split = reminder.split("\\s+");
            final var number = Integer.parseInt(split[0]);
            final var temporal = split[1];
            reminder = reminder.substring((split[0] + split[1]).length() + 1).trim();

            if (StringUtils.startsWithIgnoreCase(reminder, "to")) {
                reminder = reminder.substring(2).trim();
            }

            var dueDate = LocalDateTime.now();
            if (StringUtils.containsIgnoreCase(temporal, "minute")) {
                dueDate = dueDate.plus(number, ChronoUnit.MINUTES);
            } else if (StringUtils.containsIgnoreCase(temporal, "hour")) {
                dueDate = dueDate.plus(number, ChronoUnit.HOURS);
            } else if (StringUtils.containsIgnoreCase(temporal, "day")) {
                dueDate = dueDate.plus(number, ChronoUnit.DAYS);
            }

            final var savedReminder = reminderDal.insert(new Reminder(member.getIdLong(), message.getChannel().getIdLong(), reminder, dueDate));

            scheduleTask(() -> remind(savedReminder.getId()), dueDate);

            final var snarkyReminderResponse = Constants.pickRandom(snarkyReminderResponseRepository.findAll());

            discordWebhookSendService.sendMessage(message.getChannel().getIdLong(), swampyGamesConfig.getJohnNickname(), swampyGamesConfig.getJohnAvatar(),
                    "(reminder #" + savedReminder.getId() + ") " + snarkyReminderResponse.getResponse().replace("<name>", "<@" + message.getAuthor().getId() + ">"));
        } catch (final Exception e) {
            log.error("Exception trying to parse a reminder. [msgText={}]", message.getContentRaw(), e);

            discordWebhookSendService.sendMessage(message.getChannel().getIdLong(), swampyGamesConfig.getJohnNickname(), swampyGamesConfig.getJohnAvatar(),
                    "Error: Could not understand your reminder, " + message.getAuthor().getAsMention());
        }
    }

    private void remind(final long reminderId) {
        reminderDal.findById(reminderId).ifPresent(reminder -> {
            final var textChannelById = philJda.getTextChannelById(reminder.getChannelId());

            if (textChannelById != null) {
                final var swampyGamesConfig = getSwampyGamesConfig();
                discordWebhookSendService.sendMessage(textChannelById.getIdLong(), swampyGamesConfig.getJohnNickname(), swampyGamesConfig.getJohnAvatar(),
                        "(reminder #" + reminder.getId() + ") <@" + reminder.getUserId() + "> " + reminder.getReminder());
                reminderDal.deleteById(reminder.getId());
            }
        });
    }
}
