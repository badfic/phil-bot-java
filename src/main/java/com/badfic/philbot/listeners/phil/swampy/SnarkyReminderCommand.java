package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.phil.SnarkyReminderResponse;
import com.badfic.philbot.data.phil.SnarkyReminderResponseRepository;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class SnarkyReminderCommand extends BaseSwampy implements PhilMarker {

    @Resource
    private SnarkyReminderResponseRepository snarkyReminderResponseRepository;

    public SnarkyReminderCommand() {
        name = "snarkyReminder";
        help = "!!snarkyReminder add Something Something Something\n" +
                "Adds a snarky reponse for when you ask to be reminded of something";
        requiredRole = Constants.ADMIN_ROLE;
    }

    @PostConstruct
    public void init() {
        if (snarkyReminderResponseRepository.count() < 1) {
            snarkyReminderResponseRepository.save(new SnarkyReminderResponse("sure thing, <name>"));
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        if (StringUtils.containsIgnoreCase(event.getArgs(), "add")) {
            String snark = event.getArgs().replace("add", "").trim();
            SnarkyReminderResponse savedResponse = snarkyReminderResponseRepository.save(new SnarkyReminderResponse(snark));
            event.replySuccess("Saved Reminder Response #" + savedResponse.getId());
        } else if (StringUtils.isBlank(event.getArgs())) {
            List<SnarkyReminderResponse> reminders = snarkyReminderResponseRepository.findAll();

            StringBuilder description = new StringBuilder();
            for (SnarkyReminderResponse reminder : reminders) {
                description.append('#')
                        .append(reminder.getId())
                        .append(' ')
                        .append(reminder.getResponse())
                        .append('\n');
            }

            event.reply(simpleEmbed("Reminder Responses", description.toString()));
        } else if (StringUtils.containsIgnoreCase(event.getArgs(), "delete")) {
            String number = event.getArgs().replace("delete", "").trim();

            try {
                long toDelete = Long.parseLong(number);
                if (snarkyReminderResponseRepository.existsById(toDelete)) {
                    snarkyReminderResponseRepository.deleteById(toDelete);
                    event.replySuccess("Deleted reminder response #" + toDelete);
                } else {
                    event.replySuccess("Reminder response #" + toDelete + " doesn't exist");
                }
            } catch (NumberFormatException e) {
                event.replyError("Failed to parse number " + number);
            }
        } else if (StringUtils.isNotBlank(event.getArgs())) {
            String number = event.getArgs().trim();

            try {
                long toLookup = Long.parseLong(number);
                Optional<SnarkyReminderResponse> optionalReminder = snarkyReminderResponseRepository.findById(toLookup);

                if (optionalReminder.isPresent()) {
                    SnarkyReminderResponse reminder = optionalReminder.get();
                    StringBuilder description = new StringBuilder();
                    description.append('#')
                            .append(reminder.getId())
                            .append(' ')
                            .append(reminder.getResponse());
                    event.reply(description.toString());
                } else {
                    event.replyError("Reminder Response #" + toLookup + " does not exist");;
                }
            } catch (NumberFormatException e) {
                event.replyError("Failed to parse number " + number);
            }
        } else {
            event.replyError("Unrecognized snarky reminder command");
        }
    }
}
