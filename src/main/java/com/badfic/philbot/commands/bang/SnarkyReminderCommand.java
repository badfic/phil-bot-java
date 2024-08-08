package com.badfic.philbot.commands.bang;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.SnarkyReminderResponse;
import com.badfic.philbot.data.SnarkyReminderResponseRepository;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
class SnarkyReminderCommand extends BaseBangCommand {

    private final SnarkyReminderResponseRepository snarkyReminderResponseRepository;

    SnarkyReminderCommand(final SnarkyReminderResponseRepository snarkyReminderResponseRepository) {
        name = "snarkyReminder";
        aliases = new String[] {"reminderResponse", "reminderResponses", "snarkyReminders", "snarkyResponses", "snarkyResponse"};
        help = """
                `!!snarkyReminder add whatever` Adds a snarky response for when you ask to be reminded of something.
                NOTE: you can (and should) use `<name>` in your responses for a personal touch.
                `!!snarkyReminder delete 1` deletes snarky reminder number 1
                `!!snarkyReminder 1` shows snarky reminder 1
                `!!snarkyReminder` shows a list of all snarky reminders""";
        requiredRole = Constants.ADMIN_ROLE;
        this.snarkyReminderResponseRepository = snarkyReminderResponseRepository;
    }

    @PostConstruct
    public void init() {
        if (snarkyReminderResponseRepository.count() < 1) {
            jdbcAggregateTemplate.insert(new SnarkyReminderResponse("sure thing, <name>"));
        }
    }

    @Override
    public void execute(final CommandEvent event) {
        if (StringUtils.containsIgnoreCase(event.getArgs(), "add")) {
            final var snark = event.getArgs().replace("add", "").trim();
            final var savedResponse = jdbcAggregateTemplate.insert(new SnarkyReminderResponse(snark));
            event.replySuccess("Saved Reminder Response #" + savedResponse.getId());
        } else if (StringUtils.isBlank(event.getArgs())) {
            final var reminders = snarkyReminderResponseRepository.findAll();

            final var description = new StringBuilder();
            for (final var reminder : reminders) {
                description.append('#')
                        .append(reminder.getId())
                        .append(' ')
                        .append(reminder.getResponse())
                        .append('\n');
            }

            final var finalString = description.toString();
            if (finalString.length() > 2_000) {
                event.getChannel().sendFiles(FileUpload.fromData(finalString.getBytes(), "snarky-responses.txt")).queue();
            } else {
                event.reply(finalString);
            }
        } else if (StringUtils.containsIgnoreCase(event.getArgs(), "delete")) {
            final var number = event.getArgs().replace("delete", "").trim();

            try {
                final var toDelete = Long.parseLong(number);
                if (snarkyReminderResponseRepository.existsById(toDelete)) {
                    snarkyReminderResponseRepository.deleteById(toDelete);
                    event.replySuccess("Deleted reminder response #" + toDelete);
                } else {
                    event.replySuccess("Reminder response #" + toDelete + " doesn't exist");
                }
            } catch (final NumberFormatException e) {
                event.replyError("Failed to parse number " + number);
            }
        } else if (StringUtils.isNotBlank(event.getArgs())) {
            final var number = event.getArgs().trim();

            try {
                final var toLookup = Long.parseLong(number);
                final var optionalReminder = snarkyReminderResponseRepository.findById(toLookup);

                if (optionalReminder.isPresent()) {
                    final var reminder = optionalReminder.get();
                    final var description = new StringBuilder();
                    description.append('#')
                            .append(reminder.getId())
                            .append(' ')
                            .append(reminder.getResponse());
                    event.reply(description.toString());
                } else {
                    event.replyError("Reminder Response #" + toLookup + " does not exist");
                }
            } catch (final NumberFormatException e) {
                event.replyError("Failed to parse number " + number);
            }
        } else {
            event.replyError("Unrecognized snarky reminder command");
        }
    }
}
