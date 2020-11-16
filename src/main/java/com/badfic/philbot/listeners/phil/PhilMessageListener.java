package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.phil.Quote;
import com.badfic.philbot.data.phil.QuoteRepository;
import com.badfic.philbot.data.phil.Reminder;
import com.badfic.philbot.data.phil.ReminderRepository;
import com.badfic.philbot.data.phil.SnarkyReminderResponse;
import com.badfic.philbot.data.phil.SnarkyReminderResponseRepository;
import com.badfic.philbot.listeners.antonia.AntoniaMessageListener;
import com.badfic.philbot.listeners.behrad.BehradMessageListener;
import com.badfic.philbot.listeners.keanu.KeanuMessageListener;
import com.badfic.philbot.listeners.phil.swampy.SwampyCommand;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class PhilMessageListener extends ListenerAdapter implements PhilMarker {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Cache<String, Function<GuildMessageReactionAddEvent, Boolean>> OUTSTANDING_REACTION_TASKS = CacheBuilder.newBuilder()
            .maximumSize(200)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();
    private static final Pattern PHIL_PATTERN = Pattern.compile("\\b(phil|klemmer|phellen|cw|willip|schlemmer|pharole|klaskin|phreddie|klercury|philliam)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern REMINDER_PATTER = Pattern.compile("\\b(remind me in |remind <@![0-9]+> in )[0-9]+\\b", Pattern.CASE_INSENSITIVE);

    private final PhilCommand philCommand;
    private final SwampyCommand swampyCommand;
    private final CommandClient philCommandClient;

    @Resource
    @Lazy
    private BehradMessageListener behradMessageListener;

    @Resource
    @Lazy
    private KeanuMessageListener keanuMessageListener;

    @Resource
    @Lazy
    private AntoniaMessageListener antoniaMessageListener;

    @Resource(name = "behradJda")
    @Lazy
    private JDA behradJda;

    @Resource(name = "keanuJda")
    @Lazy
    private JDA keanuJda;

    @Resource(name = "antoniaJda")
    @Lazy
    private JDA antoniaJda;

    @Resource
    private QuoteRepository quoteRepository;

    @Resource
    private ReminderRepository reminderRepository;

    @Resource
    private SnarkyReminderResponseRepository snarkyReminderResponseRepository;

    @Autowired
    public PhilMessageListener(PhilCommand philCommand,
                               SwampyCommand swampyCommand,
                               @Qualifier("philCommandClient") CommandClient philCommandClient) {
        this.philCommand = philCommand;
        this.swampyCommand = swampyCommand;
        this.philCommandClient = philCommandClient;
    }

    public static void addReactionTask(String messageId, Function<GuildMessageReactionAddEvent, Boolean> function) {
        OUTSTANDING_REACTION_TASKS.put(messageId, function);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getChannelType() != ChannelType.TEXT) {
            return;
        }

        antoniaMessageListener.onMessageReceived(new MessageReceivedEvent(antoniaJda, event.getResponseNumber(), event.getMessage()));
        behradMessageListener.onMessageReceived(new MessageReceivedEvent(behradJda, event.getResponseNumber(), event.getMessage()));
        keanuMessageListener.onMessageReceived(new MessageReceivedEvent(keanuJda, event.getResponseNumber(), event.getMessage()));

        String msgContent = event.getMessage().getContentRaw();

        if (msgContent.startsWith("!!") || event.getAuthor().isBot()) {
            return;
        }

        if (REMINDER_PATTER.matcher(msgContent).find()) {
            addReminder(event.getMessage());
            return;
        }

        swampyCommand.execute(new CommandEvent(event, "", philCommandClient));

        if (PHIL_PATTERN.matcher(msgContent).find()) {
            philCommand.execute(new CommandEvent(event, null, philCommandClient));
            return;
        }
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        swampyCommand.voiceJoined(event.getMember());
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        swampyCommand.voiceLeft(event.getMember());
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (event.getReactionEmote().isEmoji() && "\uD83D\uDCAC".equalsIgnoreCase(event.getReactionEmote().getEmoji())) {
            if (!quoteRepository.existsByMessageId(event.getMessageIdLong())) {
                event.getChannel().retrieveMessageById(event.getMessageId()).queue(msg -> {
                    long msgId = msg.getIdLong();
                    long channelId = msg.getChannel().getIdLong();
                    Quote savedQuote = quoteRepository.save(new Quote(msgId, channelId, msg.getContentRaw(),
                            msg.getAuthor().getIdLong(), msg.getTimeCreated().toLocalDateTime()));

                    msg.addReaction("\uD83D\uDCAC").queue();

                    String msgLink = " [(jump)](https://discordapp.com/channels/" + event.getGuild().getIdLong() + '/' + channelId + '/' + msgId + ')';

                    MessageEmbed messageEmbed = new EmbedBuilder()
                            .setTitle("Quote #" + savedQuote.getId() + " Added")
                            .setDescription("<@!" + event.getUserId() + "> Added quote #" + savedQuote.getId() + msgLink)
                            .build();

                    event.getChannel().sendMessage(messageEmbed).queue();
                });
            }
        }

        OUTSTANDING_REACTION_TASKS.asMap().computeIfPresent(event.getMessageId(), (key, function) -> {
            boolean taskIsComplete = function.apply(event);

            if (taskIsComplete) {
                return null;
            }
            return function;
        });
        swampyCommand.emote(event);
    }

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        if (event.getRoles().stream().anyMatch(r -> r.getName().equals(Constants.CHAOS_CHILDREN_ROLE) || r.getName().equals(Constants.EIGHTEEN_PLUS_ROLE))) {
            String mention = event.getMember().getAsMention();
            event.getGuild().getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0).sendMessage(mention + " Congratulations on not being a newbie " +
                    "anymore my chaotic friend! Now you're entered into The Swampys, our server wide chaos games. " +
                    "You don't have to actively participate, but if you'd like to join the chaos with us, take a peek at what The Swampys has to offer.\n\n" +
                    "https://discordapp.com/channels/740999022340341791/761398315119280158/761411776561807410").queue();
        }
    }

    @Override
    public void onGuildBan(@NotNull GuildBanEvent event) {
        begone(event.getUser(), event);
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        begone(event.getUser(), event);
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        event.getGuild().loadMembers().onSuccess(members -> {
            if (CollectionUtils.size(members) == 100) {
                event.getGuild().getTextChannelsByName("announcements", false).stream().findAny().ifPresent(channel -> {
                    channel.sendMessage("Ah shit. I guess I should say something special since we hit 100 members..... uh......").queue(msg -> {
                        channel.sendMessage("https://media.giphy.com/media/xUOwGjPHOGcv9ddpYc/giphy.gif").queue();
                    });
                });
            }
        });
    }

    private void begone(User user, GenericGuildEvent event) {
        swampyCommand.removeFromGames(user.getId());
        Optional<TextChannel> announcementsChannel = event.getGuild().getTextChannelsByName("announcements", false).stream().findFirst();
        announcementsChannel.ifPresent(channel -> channel.sendMessage("Begone Bot " + user.getAsMention()).queue());
    }

    private void addReminder(Message message) {
        try {
            String reminder;
            int remindIdx = StringUtils.indexOf(message.getContentRaw(), "remind");

            Member member = message.getMember();
            if (CollectionUtils.isNotEmpty(message.getMentionedMembers())) {
                member = message.getMentionedMembers().get(0);

                reminder = message.getContentRaw().substring(remindIdx + ("remind <@!" + member.getIdLong() + "> in ").length());
            } else {
                reminder = message.getContentRaw().substring(remindIdx + "remind me in ".length());
            }
            String[] split = reminder.split("\\s+");
            int number = Integer.parseInt(split[0]);
            String temporal = split[1];
            reminder = reminder.substring((split[0] + split[1]).length() + 1).trim();

            if (StringUtils.startsWithIgnoreCase(reminder, "to")) {
                reminder = reminder.replace("to", "").trim();
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
            SnarkyReminderResponse snarkyReminderResponse = pickRandom(snarkyReminderResponseRepository.findAll());
            message.getChannel()
                    .sendMessage("(reminder #" + savedReminder.getId() + ") " +
                            snarkyReminderResponse.getResponse().replace("<name>", "<@!" + message.getAuthor().getId() + ">"))
                    .queue();
        } catch (Exception e) {
            logger.error("Exception trying to parse a reminder", e);
        }
    }

    private <T> T pickRandom(Collection<T> collection) {
        int index = ThreadLocalRandom.current().nextInt(collection.size());
        Iterator<T> iterator = collection.iterator();
        for (int i = 0; i < index; i++) {
            iterator.next();
        }
        return iterator.next();
    }

}
