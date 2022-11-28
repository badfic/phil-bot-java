package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.listeners.antonia.AntoniaMessageListener;
import com.badfic.philbot.listeners.behrad.BehradMessageListener;
import com.badfic.philbot.listeners.john.JohnMessageListener;
import com.badfic.philbot.listeners.keanu.KeanuMessageListener;
import com.badfic.philbot.listeners.phil.swampy.MemberCount;
import com.badfic.philbot.listeners.phil.swampy.NsfwQuoteCommand;
import com.badfic.philbot.listeners.phil.swampy.QuoteCommand;
import com.badfic.philbot.listeners.phil.swampy.SwampyCommand;
import com.badfic.philbot.service.Ao3MetadataParser;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateAvatarEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class PhilMessageListener extends ListenerAdapter {

    private static final Cache<String, Function<GuildMessageReactionAddEvent, Boolean>> OUTSTANDING_REACTION_TASKS = CacheBuilder.newBuilder()
            .maximumSize(200)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();
    private static final Pattern PHIL_PATTERN = Constants.compileWords("phil|klemmer|phellen|the cw|willip|schlemmer|pharole|klaskin|phreddie|klercury|philliam");
    private static final Pattern AO3_PATTERN = Pattern.compile("^(?:http(s)?://)?(archiveofourown\\.org/works/)([0-9]+).*$", Pattern.CASE_INSENSITIVE);
    private static final Multimap<String, Pair<Pattern, String>> USER_TRIGGER_WORDS = ImmutableMultimap.<String, Pair<Pattern, String>>builder()
            .put("594740276568784906", ImmutablePair.of(Constants.compileWords("hubby"), "Hi boo"))
            .put("323520695550083074", ImmutablePair.of(Constants.compileWords("child"), "Yes father?"))
            .build();

    @Autowired
    @Lazy
    private BehradMessageListener behradMessageListener;

    @Autowired
    @Lazy
    private KeanuMessageListener keanuMessageListener;

    @Autowired
    @Lazy
    private AntoniaMessageListener antoniaMessageListener;

    @Autowired
    @Lazy
    private JohnMessageListener johnMessageListener;

    @Autowired
    @Qualifier("behradJda")
    @Lazy
    private JDA behradJda;

    @Autowired
    @Qualifier("keanuJda")
    @Lazy
    private JDA keanuJda;

    @Autowired
    @Qualifier("antoniaJda")
    @Lazy
    private JDA antoniaJda;

    @Autowired
    @Qualifier("johnJda")
    @Lazy
    private JDA johnJda;

    @Autowired
    @Lazy
    private NsfwQuoteCommand nsfwQuoteCommand;

    @Autowired
    @Lazy
    private QuoteCommand quoteCommand;

    @Autowired
    private PhilCommand philCommand;

    @Autowired
    private SwampyCommand swampyCommand;

    @Autowired
    private MemeCommandsService memeCommandsService;

    @Autowired
    @Lazy
    private Ao3MetadataParser ao3MetadataParser;

    @Autowired
    @Lazy
    private MemberCount memberCount;

    @Autowired
    @Qualifier("philCommandClient")
    private CommandClient philCommandClient;

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
        johnMessageListener.onMessageReceived(new MessageReceivedEvent(johnJda, event.getResponseNumber(), event.getMessage()));

        String msgContent = event.getMessage().getContentRaw();

        if (StringUtils.isBlank(msgContent) || msgContent.startsWith(Constants.PREFIX) || event.getAuthor().isBot()) {
            return;
        }

        if (StringUtils.startsWith(msgContent, "!") && !StringUtils.trim(msgContent).equals("!")) {
            memeCommandsService.executeCustomCommand(StringUtils.trim(StringUtils.substring(msgContent, 1)), event.getTextChannel());
        }

        Constants.checkUserTriggerWords(event, USER_TRIGGER_WORDS);

        if (AO3_PATTERN.matcher(msgContent).find()) {
            ao3MetadataParser.parseLink(msgContent, event.getChannel().getName());
        }

        swampyCommand.execute(new CommandEvent(event, Constants.PREFIX, "", philCommandClient));

        if (PHIL_PATTERN.matcher(msgContent).find()) {
            philCommand.execute(new CommandEvent(event, Constants.PREFIX, null, philCommandClient));
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
        if (event.getReactionEmote().isEmoji()) {
            if (quoteCommand.getEmoji().equals(event.getReactionEmote().getEmoji())) {
                quoteCommand.saveQuote(event);
            }

            if (nsfwQuoteCommand.getEmoji().equals(event.getReactionEmote().getEmoji())) {
                nsfwQuoteCommand.saveQuote(event);
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
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        memberCount.updateCount();

        event.getGuild().getTextChannelsByName("landing-zone", false).stream().findFirst().ifPresent(landingZone -> {
            String guildName = event.getGuild().getName();
            landingZone.sendMessageEmbeds(Constants.simpleEmbed("Welcome to my humble swamp, swampling", String.format(
                    """
                    Hey %s, welcome to %s! Before you can be given full server access by a mod, there's a few things you need to do:
                                        
                    <:gayheart:741140553244082257> Select your #roles
                    ‼️ You can select multiple roles, so choose all the ones you like! These will unlock secret channels for you to chat in
                                        
                    <:aroheart:741141415287062617> Write your introduction in #introductions
                    
                    ‼️ Make sure to include:
                    7️⃣ Your age or age range
                    <:gayhandshake:749048088080941097> Your pronouns
                    😍 Any relevant information we need to know about you!
                                                
                    Otherwise, welcome and enjoy The Swamp!
                    """, event.getMember().getAsMention(), guildName))).queue();
        });
    }

    @Override
    public void onGuildBan(@NotNull GuildBanEvent event) {
        swampyCommand.removeFromGames(event.getGuild(), event.getUser().getId());

        event.getJDA().getTextChannelsByName(Constants.TEST_CHANNEL, false).stream().findAny().ifPresent(channel -> {
            channel.sendMessageEmbeds(Constants.simpleEmbedThumbnail(
                            event.getUser().getName() + " Was Banned",
                            String.format("%s\nWas Banned", event.getUser().getAsMention()),
                            event.getUser().getEffectiveAvatarUrl()))
                    .queue();
        });

        memberCount.updateCount();

        Optional<TextChannel> announcementsChannel = event.getGuild().getTextChannelsByName("event-reminders", false).stream().findFirst();
        announcementsChannel.ifPresent(channel -> channel.sendMessage("Begone Bot " + event.getUser().getName()).queue());
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        swampyCommand.removeFromGames(event.getGuild(), event.getUser().getId());

        event.getJDA().getTextChannelsByName(Constants.TEST_CHANNEL, false).stream().findAny().ifPresent(channel -> {
            channel.sendMessageEmbeds(Constants.simpleEmbedThumbnail(
                            event.getUser().getName() + " Has Left",
                            String.format("%s\nHas left the server", event.getUser().getAsMention()),
                            event.getUser().getEffectiveAvatarUrl()))
                    .queue();
        });

        memberCount.updateCount();

        if (event.getUser().isBot()) {
            Optional<TextChannel> announcementsChannel = event.getGuild().getTextChannelsByName("event-reminders", false).stream().findFirst();
            announcementsChannel.ifPresent(channel -> channel.sendMessage("Begone Bot " + event.getUser().getName()).queue());
        }
    }

    @Override
    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event) {
        event.getJDA().getTextChannelsByName(Constants.MOD_LOGS_CHANNEL, false).stream().findAny().ifPresent(channel -> {
            channel.sendMessageEmbeds(Constants.simpleEmbedThumbnail(
                            event.getUser().getName() + " Updated Nickname",
                            String.format("%s\nOld Nickname = %s\nNew Nickname = %s",
                                    event.getUser().getAsMention(), event.getOldNickname(), event.getNewNickname()),
                            event.getUser().getEffectiveAvatarUrl()))
                    .queue();
        });
    }

    @Override
    public void onUserUpdateName(@NotNull UserUpdateNameEvent event) {
        event.getJDA().getTextChannelsByName(Constants.MOD_LOGS_CHANNEL, false).stream().findAny().ifPresent(channel -> {
            channel.sendMessageEmbeds(Constants.simpleEmbedThumbnail(
                    event.getOldName() + " Updated Username",
                    String.format("%s\nOld Username = %s\nNew Username = %s", event.getUser().getAsMention(), event.getOldName(), event.getNewName()),
                    event.getUser().getEffectiveAvatarUrl()))
                    .queue();
        });
    }

    @Override
    public void onGuildMemberUpdateAvatar(@NotNull GuildMemberUpdateAvatarEvent event) {
        event.getJDA().getTextChannelsByName(Constants.MOD_LOGS_CHANNEL, false).stream().findAny().ifPresent(channel -> {
            channel.sendMessageEmbeds(Constants.simpleEmbedThumbnail(
                            event.getMember().getEffectiveName() + " Updated Avatar",
                            event.getUser().getAsMention() + "\nOld avatar is thumbnail (unless phil didn't have it cached)\nNew avatar is larger image",
                            event.getNewAvatarUrl(),
                            event.getOldAvatarUrl()))
                    .queue();
        });
    }

}
