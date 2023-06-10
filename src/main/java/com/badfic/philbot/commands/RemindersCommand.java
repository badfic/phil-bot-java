package com.badfic.philbot.commands;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.Reminder;
import com.badfic.philbot.data.ReminderDal;
import com.badfic.philbot.data.SnarkyReminderResponse;
import com.badfic.philbot.data.SnarkyReminderResponseRepository;
import com.badfic.philbot.data.SwampyGamesConfig;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RemindersCommand extends BaseNormalCommand {

    private final ReminderDal reminderDal;
    private final SnarkyReminderResponseRepository snarkyReminderResponseRepository;

    public RemindersCommand(ReminderDal reminderDal, SnarkyReminderResponseRepository snarkyReminderResponseRepository) {
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

    @PostConstruct
    public void init() {
        for (Reminder reminder : reminderDal.findAll()) {
            taskScheduler.schedule(() -> remind(reminder.getId()), reminder.getDueDate().toInstant(ZoneOffset.UTC));
        }
    }

    @Override
    public void execute(CommandEvent event) {
        if (StringUtils.isBlank(event.getArgs())) {
            Collection<Reminder> reminders = reminderDal.findAll();

            StringBuilder description = new StringBuilder();
            for (Reminder reminder : reminders) {
                description.append('#')
                        .append(reminder.getId())
                        .append(" <@")
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
                if (reminderDal.existsById(toDelete)) {
                    reminderDal.deleteById(toDelete);
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
                Optional<Reminder> optionalReminder = reminderDal.findById(toLookup);

                if (optionalReminder.isPresent()) {
                    Reminder reminder = optionalReminder.get();
                    StringBuilder description = new StringBuilder();
                    description.append('#')
                            .append(reminder.getId())
                            .append(" <@")
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

    public void addReminder(Message message, SwampyGamesConfig swampyGamesConfig) {
        try {
            String reminder;
            int remindIdx = StringUtils.indexOfIgnoreCase(message.getContentRaw(), "remind");

            Member member = message.getMember();
            if (CollectionUtils.isNotEmpty(message.getMentions().getMembers())) {
                member = message.getMentions().getMembers().get(0);

                reminder = message.getContentRaw().substring(remindIdx + ("remind <@" + member.getIdLong() + "> in ").length());
            } else {
                reminder = message.getContentRaw().substring(remindIdx + "remind me in ".length());
            }
            String[] split = reminder.split("\\s+");
            int number = Integer.parseInt(split[0]);
            String temporal = split[1];
            reminder = reminder.substring((split[0] + split[1]).length() + 1).trim();

            if (StringUtils.startsWithIgnoreCase(reminder, "to")) {
                reminder = reminder.substring(2).trim();
            }

            LocalDateTime dueDate = LocalDateTime.now();

            if (StringUtils.containsIgnoreCase(temporal, "minute")) {
                dueDate = dueDate.plus(number, ChronoUnit.MINUTES);
            } else if (StringUtils.containsIgnoreCase(temporal, "hour")) {
                dueDate = dueDate.plus(number, ChronoUnit.HOURS);
            } else if (StringUtils.containsIgnoreCase(temporal, "day")) {
                dueDate = dueDate.plus(number, ChronoUnit.DAYS);
            }

            Reminder savedReminder = reminderDal.insert(new Reminder(member.getIdLong(), message.getChannel().getIdLong(), reminder, dueDate));

            taskScheduler.schedule(() -> remind(savedReminder.getId()), dueDate.toInstant(ZoneOffset.UTC));

            SnarkyReminderResponse snarkyReminderResponse = Constants.pickRandom(snarkyReminderResponseRepository.findAll());

            discordWebhookSendService.sendMessage(message.getChannel().getIdLong(), swampyGamesConfig.getJohnNickname(), swampyGamesConfig.getJohnAvatar(),
                    "(reminder #" + savedReminder.getId() + ") " + snarkyReminderResponse.getResponse().replace("<name>", "<@" + message.getAuthor().getId() + ">"));
        } catch (Exception e) {
            log.error("Exception trying to parse a reminder. [msgText={}]", message.getContentRaw(), e);

            discordWebhookSendService.sendMessage(message.getChannel().getIdLong(), swampyGamesConfig.getJohnNickname(), swampyGamesConfig.getJohnAvatar(),
                    "Error: Could not understand your reminder, " + message.getAuthor().getAsMention());
        }
    }

    public void remind(long reminderId) {
        reminderDal.findById(reminderId).ifPresent(reminder -> {
            TextChannel textChannelById = philJda.getTextChannelById(reminder.getChannelId());

            if (textChannelById != null) {
                SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
                discordWebhookSendService.sendMessage(textChannelById.getIdLong(), swampyGamesConfig.getJohnNickname(), swampyGamesConfig.getJohnAvatar(),
                        "(reminder #" + reminder.getId() + ") <@" + reminder.getUserId() + "> " + reminder.getReminder());
                reminderDal.deleteById(reminder.getId());
            }
        });
    }
}
