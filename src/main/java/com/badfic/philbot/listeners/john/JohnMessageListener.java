package com.badfic.philbot.listeners.john;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.Reminder;
import com.badfic.philbot.data.ReminderRepository;
import com.badfic.philbot.data.SnarkyReminderResponse;
import com.badfic.philbot.data.SnarkyReminderResponseRepository;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JohnMessageListener {
    private static final Pattern JOHN_PATTERN = Constants.compileWords("john|constantine|johnno|johnny|hellblazer");
    private static final Pattern REMINDER_PATTER = Pattern.compile("\\b(remind me in |remind <@![0-9]+> in )[0-9]+\\b", Pattern.CASE_INSENSITIVE);
    private static final ConcurrentMap<Long, Pair<String, Long>> LAST_WORD_MAP = new ConcurrentHashMap<>();
    private static final Set<String> UWU = ImmutableSet.of(
            "https://tenor.com/bbmRv.gif",
            "https://tenor.com/X7Nq.gif",
            "https://tenor.com/bhbNA.gif",
            "https://tenor.com/baaJk.gif",
            "https://tenor.com/bedWY.gif",
            "https://tenor.com/bpXCH.gif",
            "https://tenor.com/bjCS0.gif",
            "https://tenor.com/bcse7.gif",
            "https://tenor.com/bcsfc.gif",
            "https://tenor.com/bcse9.gif",
            "https://tenor.com/bcsfb.gif",
            "https://tenor.com/bcsfd.gif",
            "https://tenor.com/bcsfe.gif",
            "https://tenor.com/bcse6.gif",
            "https://tenor.com/bh7al.gif",
            "https://tenor.com/bgP9d.gif",
            "https://tenor.com/beotA.gif",
            "https://tenor.com/bfoYa.gif",
            "https://tenor.com/bhzQJ.gif",
            "https://tenor.com/bhngy.gif",
            "https://tenor.com/POHh.gif",
            "https://cdn.discordapp.com/attachments/530193785351569423/777962192083353611/zaricat.gif",
            "https://emoji.gg/assets/emoji/8178_arainblob.gif",
            "https://emoji.gg/assets/emoji/8195_agooglecat.gif",
            "https://emoji.gg/assets/emoji/8569_ablobmeltsoblove.gif",
            "https://emoji.gg/assets/emoji/2231_ablobhearteyes.gif",
            "https://emoji.gg/assets/emoji/2171_ablobaww.gif",
            "https://emoji.gg/assets/emoji/1279_Flying_Hearts_Red.gif",
            "https://cdn.discordapp.com/attachments/707453916882665552/793431107289743370/giphy.gif",
            "https://cdn.discordapp.com/attachments/707453916882665552/793431132963733514/giphy2.gif",
            "https://cdn.discordapp.com/attachments/707453916882665552/793431157717991464/chimmy.gif",
            "https://cdn.discordapp.com/attachments/707453916882665552/793431180099452948/tata.gif",
            "https://cdn.discordapp.com/attachments/761398315119280158/867804756621131786/chris-evans-steve-rogers-heart-Favim.png");
    private static final Set<String> GOOD_BOT = ImmutableSet.of(
            ":D!!!",
            "I know",
            "Yeah, yeah",
            "Everyone who puts their trust in me dies",
            "I'm damned is what I am",
            "Thanks, luv",
            "Quite right",
            "Ey up",
            "**Oi**",
            "You must be off yer trolley, mate",
            "Shut up yer biff",
            "Oi, just geg out will ya",
            "Are you messin?",
            "Give it a rest ya muppet",
            "Yeah, sound mate",
            "That's right boss innit",
            "Ain't you a proper meff, then?",
            "Thanks, luv. I'm absolutely made up.",
            "What a beut!",
            "Ta, mate",
            "https://emoji.gg/assets/emoji/1554_ablobderpyhappy.gif",
            "https://emoji.gg/assets/emoji/8783_ablobhop.gif"
    );
    private static final Multimap<String, Pair<Pattern, String>> USER_TRIGGER_WORDS = ImmutableMultimap.<String, Pair<Pattern, String>>builder()
            .put("323520695550083074", ImmutablePair.of(Constants.compileWords("child"), "Yes father?"))
            .build();

    private final JohnCommand johnCommand;
    private final ReminderRepository reminderRepository;
    private final SnarkyReminderResponseRepository snarkyReminderResponseRepository;

    public JohnMessageListener(JohnCommand johnCommand, ReminderRepository reminderRepository, SnarkyReminderResponseRepository snarkyReminderResponseRepository) {
        this.johnCommand = johnCommand;
        this.reminderRepository = reminderRepository;
        this.snarkyReminderResponseRepository = snarkyReminderResponseRepository;
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String msgContent = event.getMessage().getContentRaw();

        if (StringUtils.isBlank(msgContent) || msgContent.startsWith(Constants.PREFIX) || event.getAuthor().isBot()) {
            return;
        }

        long channelId = event.getMessage().getChannel().getIdLong();
        LAST_WORD_MAP.compute(channelId, (key, oldValue) -> {
            if (oldValue == null) {
                return new ImmutablePair<>(msgContent, 1L);
            }
            if (oldValue.getLeft().equalsIgnoreCase(msgContent)) {
                if (oldValue.getRight() + 1 >= 5) {
                    johnCommand.getJohnJda().getTextChannelById(channelId).sendMessage(msgContent).queue();
                    return new ImmutablePair<>(msgContent, 0L);
                }

                return new ImmutablePair<>(msgContent, oldValue.getRight() + 1);
            } else {
                return new ImmutablePair<>(msgContent, 1L);
            }
        });

        if (REMINDER_PATTER.matcher(msgContent).find()) {
            addReminder(event.getMessage());
            return;
        }

        Constants.checkUserTriggerWords(event, USER_TRIGGER_WORDS);

        if ("ayy".equalsIgnoreCase(msgContent)) {
            johnCommand.getJohnJda().getTextChannelById(channelId).sendMessage("lmao").queue();
            return;
        }
        if ("slang".equalsIgnoreCase(msgContent)) {
            johnCommand.getJohnJda().getTextChannelById(channelId).sendMessage("You're an absolute whopper").queue();
        }
        if ("stughead".equalsIgnoreCase(msgContent)) {
            johnCommand.getJohnJda().getTextChannelById(channelId).sendMessage("\uD83D\uDC40").queue();
        }
        if ("Africa".equalsIgnoreCase(msgContent)) {
            johnCommand.getJohnJda().getTextChannelById(channelId).sendMessage("I BLESS THE RAINS DOWN IN AAAAFRICAAA").queue();
        }
        if ("cool".equalsIgnoreCase(msgContent)) {
            johnCommand.getJohnJda().getTextChannelById(channelId).sendMessage("cool cool cool").queue();
        }
        if ("uwu".equalsIgnoreCase(msgContent)) {
            johnCommand.getJohnJda().getTextChannelById(channelId).sendMessage(Constants.pickRandom(UWU)).queue();
        }
        if ("good bot".equalsIgnoreCase(msgContent)) {
            if (601043580945170443L == event.getAuthor().getIdLong()) {
                johnCommand.getJohnJda().getTextChannelById(channelId).sendMessage(":D!!!").queue();
            } else {
                johnCommand.getJohnJda().getTextChannelById(channelId).sendMessage(Constants.pickRandom(GOOD_BOT)).queue();
            }
        }

        if (JOHN_PATTERN.matcher(msgContent).find()) {
            johnCommand.execute(new CommandEvent(event, Constants.PREFIX, null, null));
            return;
        }
    }

    private void addReminder(Message message) {
        try {
            String reminder;
            int remindIdx = StringUtils.indexOfIgnoreCase(message.getContentRaw(), "remind");

            Member member = message.getMember();
            if (CollectionUtils.isNotEmpty(message.getMentions().getMembers())) {
                member = message.getMentions().getMembers().get(0);

                reminder = message.getContentRaw().substring(remindIdx + ("remind <@!" + member.getIdLong() + "> in ").length());
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

            LocalDateTime dueDate = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

            if (StringUtils.containsIgnoreCase(temporal, "minute")) {
                dueDate = dueDate.plus(number, ChronoUnit.MINUTES);
            } else if (StringUtils.containsIgnoreCase(temporal, "hour")) {
                dueDate = dueDate.plus(number, ChronoUnit.HOURS);
            } else if (StringUtils.containsIgnoreCase(temporal, "day")) {
                dueDate = dueDate.plus(number, ChronoUnit.DAYS);
            }

            Reminder savedReminder = reminderRepository.save(new Reminder(member.getIdLong(), message.getChannel().getIdLong(), reminder, dueDate));
            SnarkyReminderResponse snarkyReminderResponse = Constants.pickRandom(snarkyReminderResponseRepository.findAll());
            johnCommand.getJohnJda().getTextChannelById(message.getChannel().getIdLong())
                    .sendMessage("(reminder #" + savedReminder.getId() + ") " +
                            snarkyReminderResponse.getResponse().replace("<name>", "<@!" + message.getAuthor().getId() + ">"))
                    .queue();
        } catch (Exception e) {
            log.error("Exception trying to parse a reminder. [msgText={}]", message.getContentRaw(), e);
            johnCommand.getJohnJda().getTextChannelById(message.getChannel().getIdLong())
                    .sendMessage("Error: Could not understand your reminder, " + message.getAuthor().getAsMention())
                    .queue();
        }
    }

}
