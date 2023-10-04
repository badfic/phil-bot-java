package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.BaseNormalCommand;
import com.badfic.philbot.commands.NsfwQuoteCommand;
import com.badfic.philbot.commands.QuoteCommand;
import com.badfic.philbot.commands.events.MemberCount;
import com.badfic.philbot.commands.slash.BaseSlashCommand;
import com.badfic.philbot.commands.swampy.SwampyCommand;
import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.data.SwampyGamesConfigDal;
import com.badfic.philbot.listeners.DiscordWebhookSendService;
import com.badfic.philbot.listeners.antonia.AntoniaMessageListener;
import com.badfic.philbot.listeners.behrad.BehradMessageListener;
import com.badfic.philbot.listeners.john.JohnMessageListener;
import com.badfic.philbot.listeners.keanu.KeanuMessageListener;
import com.badfic.philbot.service.Ao3MetadataParser;
import com.badfic.philbot.service.HungerGamesWinnersService;
import com.badfic.philbot.service.MemeCommandsService;
import com.badfic.philbot.service.OnJdaReady;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateAvatarEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class PhilMessageListener extends ListenerAdapter {
    private static final Pattern PHIL_PATTERN = Constants.compileWords("phil|klemmer|phellen|the cw|willip|schlemmer|pharole|klaskin|phreddie|klercury|philliam");
    private static final Pattern AO3_PATTERN = Pattern.compile("^(?:http(s)?://)?(archiveofourown\\.org/works/)([0-9]+).*$", Pattern.CASE_INSENSITIVE);
    private static final Long2ObjectMap<List<Pair<Pattern, String>>> USER_TRIGGER_WORDS = new Long2ObjectArrayMap<>(Map.of(
            594740276568784906L, List.of(ImmutablePair.of(Constants.compileWords("hubby"), "Hi boo")),
            323520695550083074L, List.of(ImmutablePair.of(Constants.compileWords("child"), "Yes father?"))
    ));
    private static final Long2ObjectMap<Pair<String, Short>> LAST_WORD_MAP = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());

    private final BehradMessageListener behradMessageListener;
    private final KeanuMessageListener keanuMessageListener;
    private final AntoniaMessageListener antoniaMessageListener;
    private final JohnMessageListener johnMessageListener;
    private final NsfwQuoteCommand nsfwQuoteCommand;
    private final QuoteCommand quoteCommand;
    private final HungerGamesWinnersService hungerGamesWinnersService;
    private final SwampyCommand swampyCommand;
    private final MemeCommandsService memeCommandsService;
    private final Ao3MetadataParser ao3MetadataParser;
    private final MemberCount memberCount;
    private final DiscordWebhookSendService discordWebhookSendService;
    private final SwampyGamesConfigDal swampyGamesConfigDal;
    private final BaseConfig baseConfig;

    @Setter(onMethod_ = {@Autowired})
    private PhilCommand philCommand;

    @Setter(onMethod_ = {@Autowired})
    private ApplicationContext applicationContext;

    @Setter(onMethod_ = {@Autowired})
    private List<BaseNormalCommand> commands;

    @Setter(onMethod_ = {@Autowired})
    private List<BaseSlashCommand> slashCommands;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private JDA philJda;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getChannelType() != ChannelType.TEXT && event.getChannelType() != ChannelType.PRIVATE) {
            return;
        }

        final long channelId = event.getChannel().getIdLong();
        String msgContent = event.getMessage().getContentRaw();
        if (StringUtils.isBlank(msgContent) || event.getAuthor().isBot()) {
            return;
        }

        final boolean isCommand = StringUtils.startsWith(msgContent, Constants.PREFIX);

        if (!isCommand && event.getChannel().getType() == ChannelType.PRIVATE) {
            return;
        }

        SwampyGamesConfig swampyGamesConfig = swampyGamesConfigDal.get();

        if (swampyGamesConfig.getBoostPhrase() != null && StringUtils.containsIgnoreCase(msgContent, swampyGamesConfig.getBoostPhrase())) {
            swampyCommand.acceptedBoost(event.getMember());
        }

        if (swampyGamesConfig.getMapPhrase() != null
                && Pattern.compile(swampyGamesConfig.getMapPhrase(), Pattern.CASE_INSENSITIVE).matcher(msgContent).find()) {
            swampyCommand.acceptedMap(event.getMember());
        }

        final CommandEvent commandEvent = new CommandEvent(event);

        if (isCommand) {
            for (BaseNormalCommand command : commands) {
                if (command.getName().equalsIgnoreCase(commandEvent.getName())
                        || Arrays.stream(command.getAliases()).anyMatch(a -> a.equalsIgnoreCase(commandEvent.getName()))) {
                    // guild only commands
                    if (command.isGuildOnly() && event.getChannel().getType() == ChannelType.PRIVATE) {
                        commandEvent.replyError("This command can only be used in a channel, not in a private message");
                        return;
                    }

                    // owner commands
                    if (command.isOwnerCommand() && !event.getAuthor().getId().equals(baseConfig.ownerId)) {
                        commandEvent.replyError("This is an owner command, only Santiago can execute it");
                        return;
                    }

                    // required role check
                    if (Objects.nonNull(command.getRequiredRole())) {
                        String requiredRole = command.getRequiredRole();

                        if (Objects.isNull(event.getMember()) || !command.hasRole(event.getMember(), requiredRole)) {
                            commandEvent.replyError(String.format("You must have %s role to execute this command", requiredRole));
                            return;
                        }
                    }

                    // nsfw check
                    if (command.isNsfwOnly()) {
                        if (!(event.getChannel() instanceof IAgeRestrictedChannel ageRestrictedChannel) || !ageRestrictedChannel.isNSFW()) {
                            commandEvent.replyError("This is an nsfw only command, it cannot be executed in this channel");
                            return;
                        }
                    }

                    command.execute(commandEvent);

                    return;
                }
            }

            if (StringUtils.isNotBlank(commandEvent.getArgs())) {
                if (Stream.of("help", "rank", "up", "down", "slots")
                        .anyMatch(arg -> StringUtils.startsWithIgnoreCase(commandEvent.getArgs(), arg))) {
                    swampyCommand.execute(commandEvent);
                    return;
                }
            }

            memeCommandsService.executeCustomCommand(commandEvent.getName(), event.getChannel().asGuildMessageChannel());
            return;
        }

        if (StringUtils.startsWith(msgContent, "!") && !StringUtils.trim(msgContent).equals("!")) {
            memeCommandsService.executeCustomCommand(StringUtils.trim(StringUtils.substring(msgContent, 1)), event.getChannel().asGuildMessageChannel());
            return;
        }

        if (AO3_PATTERN.matcher(msgContent).find()) {
            ao3MetadataParser.parseLink(msgContent, event.getChannel().getName());
            return;
        }

        antoniaMessageListener.onMessageReceived(event, swampyGamesConfig);
        behradMessageListener.onMessageReceived(event, swampyGamesConfig);
        keanuMessageListener.onMessageReceived(event, swampyGamesConfig);
        johnMessageListener.onMessageReceived(event, swampyGamesConfig);

        if (PHIL_PATTERN.matcher(msgContent).find()) {
            philCommand.execute(commandEvent);
            return;
        }

        Constants.checkUserTriggerWords(event, USER_TRIGGER_WORDS, null, null, null);

        final SwampyGamesConfig finalConfig = swampyGamesConfig;
        LAST_WORD_MAP.compute(channelId, (key, oldValue) -> {
            if ("bird".equals(msgContent) || "word".equals(msgContent) || "mattgrinch".equals(msgContent)) {
                if (oldValue == null) {
                    return new ImmutablePair<>(msgContent, (short) 1);
                }

                if (oldValue.getLeft().equals(msgContent)) {
                    if (oldValue.getRight() + 1 >= 3 && "bird".equals(msgContent)) {
                        discordWebhookSendService.sendMessage(channelId, finalConfig.getAntoniaNickname(), finalConfig.getAntoniaAvatar(),
                                "the bird is the word");
                        return null;
                    }
                    if (oldValue.getRight() + 1 >= 3 && "word".equals(msgContent)) {
                        discordWebhookSendService.sendMessage(channelId, finalConfig.getAntoniaNickname(), finalConfig.getAntoniaAvatar(),
                                "the word is the bird");
                        return null;
                    }
                    if (oldValue.getRight() + 1 >= 3 && "mattgrinch".equals(msgContent)) {
                        discordWebhookSendService.sendMessage(channelId, finalConfig.getAntoniaNickname(), finalConfig.getAntoniaAvatar(),
                                "https://cdn.discordapp.com/attachments/707453916882665552/914409167610056734/unknown.png");
                        return null;
                    }

                    return new ImmutablePair<>(msgContent, (short) (oldValue.getRight() + 1));
                } else {
                    return new ImmutablePair<>(msgContent, (short) 1);
                }
            }

            return null;
        });

        swampyCommand.execute(commandEvent);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Message.suppressContentIntentWarning(); // This is specifically for all the non-phil bots, they'll print a warning without this.

        log.info("Received ready event for [user={}]", event.getJDA().getSelfUser());
        MessageEmbed messageEmbed = Constants.simpleEmbed("Restarted",
                String.format("I just restarted\ngit sha: %s\ncommit msg: %s", baseConfig.commitSha, baseConfig.commitMessage), new Color(89, 145, 17));
        event.getJDA().getTextChannelsByName(Constants.TEST_CHANNEL, false).get(0).sendMessageEmbeds(messageEmbed).queue();

        Guild guildById = philJda.getGuildById(baseConfig.guildId);

        for (BaseSlashCommand slashCommand : slashCommands) {
            CommandCreateAction commandCreateAction = guildById.upsertCommand(slashCommand.getName(), slashCommand.getHelp());

            if (CollectionUtils.isNotEmpty(slashCommand.getOptions())) {
                commandCreateAction = commandCreateAction.addOptions(slashCommand.getOptions());
            }

            commandCreateAction.queue();
        }

        applicationContext.getBeansOfType(OnJdaReady.class)
                .forEach((name, bean) -> bean.run());
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        for (BaseSlashCommand slashCommand : slashCommands) {
            if (event.getName().equals(slashCommand.getName())) {
                slashCommand.execute(event);
                return;
            }
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        for (BaseSlashCommand slashCommand : slashCommands) {
            if (event.getName().equals(slashCommand.getName())) {
                slashCommand.onAutoComplete(event);
                return;
            }
        }
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (event.getChannelJoined() != null) {
            swampyCommand.voiceJoined(event.getMember());
        } else if (event.getChannelLeft() != null) {
            swampyCommand.voiceLeft(event.getMember());
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser() != null && event.getUser().isBot()) {
            return;
        }

        EmojiUnion emoji = event.getReaction().getEmoji();
        if (emoji.getType() == Emoji.Type.UNICODE) {
            if (quoteCommand.getEmoji().equals(emoji.asUnicode().getName())) {
                quoteCommand.saveQuote(event);
            }

            if (nsfwQuoteCommand.getEmoji().equals(emoji.asUnicode().getName())) {
                nsfwQuoteCommand.saveQuote(event);
            }

            if (hungerGamesWinnersService.getEmoji().equals(emoji.asUnicode().getName())) {
                hungerGamesWinnersService.addWinner(event);
            }
        }

        Constants.computeReactionTask(event);

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
    public void onUserUpdateAvatar(UserUpdateAvatarEvent event) {
        logAvatarUpdate(event.getUser(), event.getNewAvatarUrl(), event.getOldAvatarUrl());
    }

    @Override
    public void onGuildMemberUpdateAvatar(@NotNull GuildMemberUpdateAvatarEvent event) {
        logAvatarUpdate(event.getUser(), event.getNewAvatarUrl(), event.getOldAvatarUrl());
    }

    private void logAvatarUpdate(User user, String oldAvatar, String newAvatar) {
        philJda.getTextChannelsByName(Constants.MOD_LOGS_CHANNEL, false).stream().findAny().ifPresent(channel -> {
            channel.sendMessageEmbeds(Constants.simpleEmbedThumbnail(
                            user.getEffectiveName() + " Updated Avatar",
                            user.getAsMention() + "\nOld avatar is thumbnail (unless phil didn't have it cached)\nNew avatar is larger image",
                            newAvatar,
                            oldAvatar))
                    .queue();
        });
    }

}
