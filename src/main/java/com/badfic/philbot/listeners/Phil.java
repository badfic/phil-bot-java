package com.badfic.philbot.listeners;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.bang.BaseBangCommand;
import com.badfic.philbot.commands.bang.NsfwQuoteCommand;
import com.badfic.philbot.commands.bang.QuoteCommand;
import com.badfic.philbot.commands.bang.SwampyCommand;
import com.badfic.philbot.commands.bang.events.MemberCount;
import com.badfic.philbot.commands.slash.BaseSlashCommand;
import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.SwampyGamesConfigDal;
import com.badfic.philbot.data.phil.PhilResponsesConfig;
import com.badfic.philbot.data.phil.PhilResponsesConfigRepository;
import com.badfic.philbot.service.Ao3MetadataParser;
import com.badfic.philbot.service.HungerGamesWinnersService;
import com.badfic.philbot.service.MemeCommandsService;
import com.badfic.philbot.service.OnJdaReady;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectShortPair;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
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
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

public class Phil {
    @Component
    static class Command extends BasicResponsesBot<PhilResponsesConfig> {
        Command(final PhilResponsesConfigRepository philResponsesConfigRepository, final JdbcAggregateTemplate jdbcAggregateTemplate) {
            super(philResponsesConfigRepository, jdbcAggregateTemplate, "phil", PhilResponsesConfig::new, null, null);
        }
    }

    @Component
    static class Talk extends BaseTalk {
        Talk() {
            super("philTalk");
        }
    }

    @RequiredArgsConstructor
    @Component
    @Slf4j
    public static class MessageListener extends ListenerAdapter {
        private static final Pattern PHIL_PATTERN = Constants.compileWords("phil|klemmer|phellen|the cw|willip|schlemmer|pharole|klaskin|phreddie|klercury|philliam");
        private static final Pattern AO3_PATTERN = Pattern.compile("^(?:http(s)?://)?(archiveofourown\\.org/works/)([0-9]+).*$", Pattern.CASE_INSENSITIVE);
        private static final Long2ObjectMap<List<Pair<Pattern, String>>> USER_TRIGGER_WORDS = new Long2ObjectArrayMap<>(Map.of(
                594740276568784906L, List.of(Pair.of(Constants.compileWords("hubby"), "Hi boo")),
                323520695550083074L, List.of(Pair.of(Constants.compileWords("child"), "Yes father?"))
        ));
        private static final Long2ObjectMap<ObjectShortPair<String>> LAST_WORD_MAP = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());

        private final Behrad.MessageListener behradMessageListener;
        private final Keanu.MessageListener keanuMessageListener;
        private final Antonia.MessageListener antoniaMessageListener;
        private final John.MessageListener johnMessageListener;
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
        private final RestTemplate restTemplate;

        @Setter(onMethod_ = {@Autowired(required = false)})
        private GitProperties gitProperties;

        @Setter(onMethod_ = {@Autowired})
        private Command philCommand;

        @Setter(onMethod_ = {@Autowired})
        private ApplicationContext applicationContext;

        @Setter(onMethod_ = {@Autowired})
        private List<BaseBangCommand> commands;

        @Setter(onMethod_ = {@Autowired})
        private List<BaseSlashCommand> slashCommands;

        @Setter(onMethod_ = {@Autowired, @Lazy})
        private JDA philJda;

        @Scheduled(cron = "${swampy.schedule.phil.dadjoke}", zone = "${swampy.schedule.timezone}")
        void sayDadJoke() {
            final var headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);
            final var dadJoke = restTemplate.exchange("https://icanhazdadjoke.com/", HttpMethod.GET, new HttpEntity<>(headers), DadJokeRecord.class);

            if (dadJoke.getBody() != null) {
                philJda.getTextChannelsByName("general", false).stream().findAny().ifPresent(channel -> {
                    channel.sendMessage(dadJoke.getBody().joke()).queue();
                });
            }
        }

        @Scheduled(cron = "${swampy.schedule.phil.humpday}", zone = "${swampy.schedule.timezone}")
        void philHumpDay() {
            philJda.getTextChannelsByName("general", false).stream().findAny().ifPresent(channel -> {
                channel.sendMessage("https://cdn.discordapp.com/attachments/323666308107599872/834310518478471218/mullethumpygrinch.png").queue();
            });
        }

        @Override
        public void onMessageReceived(final @NotNull MessageReceivedEvent event) {
            if (event.getChannelType() != ChannelType.TEXT && event.getChannelType() != ChannelType.PRIVATE) {
                return;
            }

            final var channelId = event.getChannel().getIdLong();
            final var msgContent = event.getMessage().getContentRaw();
            if (StringUtils.isBlank(msgContent) || event.getAuthor().isBot()) {
                return;
            }

            final var isCommand = StringUtils.startsWith(msgContent, Constants.PREFIX);

            if (!isCommand && event.getChannel().getType() == ChannelType.PRIVATE) {
                return;
            }

            final var swampyGamesConfig = swampyGamesConfigDal.get();

            if (swampyGamesConfig.getBoostPhrase() != null && StringUtils.containsIgnoreCase(msgContent, swampyGamesConfig.getBoostPhrase())) {
                swampyCommand.acceptedBoost(event.getMember());
            }

            if (swampyGamesConfig.getMapPhrase() != null
                    && Pattern.compile(swampyGamesConfig.getMapPhrase(), Pattern.CASE_INSENSITIVE).matcher(msgContent).find()) {
                swampyCommand.acceptedMap(event.getMember());
            }

            final var commandEvent = new CommandEvent(event);

            if (isCommand) {
                for (final var command : commands) {
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
                            final var requiredRole = command.getRequiredRole();

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

            LAST_WORD_MAP.compute(channelId, (key, oldValue) -> {
                if ("bird".equalsIgnoreCase(msgContent) || "word".equalsIgnoreCase(msgContent) || "mattgrinch".equalsIgnoreCase(msgContent)) {
                    if (oldValue == null) {
                        return ObjectShortPair.of(msgContent, (short) 1);
                    }

                    if (oldValue.left().equalsIgnoreCase(msgContent)) {
                        if (oldValue.rightShort() + 1 >= 3 && "bird".equalsIgnoreCase(msgContent)) {
                            discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getAntoniaNickname(), swampyGamesConfig.getAntoniaAvatar(),
                                    "the bird is the word");
                            return null;
                        }
                        if (oldValue.rightShort() + 1 >= 3 && "word".equalsIgnoreCase(msgContent)) {
                            discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getAntoniaNickname(), swampyGamesConfig.getAntoniaAvatar(),
                                    "the word is the bird");
                            return null;
                        }
                        if (oldValue.rightShort() + 1 >= 3 && "mattgrinch".equalsIgnoreCase(msgContent)) {
                            discordWebhookSendService.sendMessage(channelId, swampyGamesConfig.getAntoniaNickname(), swampyGamesConfig.getAntoniaAvatar(),
                                    "https://cdn.discordapp.com/attachments/707453916882665552/914409167610056734/unknown.png");
                            return null;
                        }

                        return ObjectShortPair.of(msgContent, (short) (oldValue.rightShort() + 1));
                    } else {
                        return ObjectShortPair.of(msgContent, (short) 1);
                    }
                }

                return null;
            });

            swampyCommand.execute(commandEvent);
        }

        @Override
        public void onReady(final @NotNull ReadyEvent event) {
            Message.suppressContentIntentWarning(); // This is specifically for all the non-phil bots, they'll print a warning without this.

            log.info("Received ready event for [guild={}] [user={}]", baseConfig.guildId, event.getJDA().getSelfUser());

            final var commitId = Optional.ofNullable(gitProperties).map(GitProperties::getCommitId).orElse(null);
            final var commitMessage = Optional.ofNullable(gitProperties).map(g -> g.get("commit.message.short")).orElse(null);

            final var messageEmbed = Constants.simpleEmbed(
                    "Restarted",
                    "I just restarted\ngit sha: %s\ncommit msg: %s".formatted(commitId, commitMessage),
                    new Color(89, 145, 17));
            event.getJDA().getTextChannelsByName(Constants.TEST_CHANNEL, false).getFirst().sendMessageEmbeds(messageEmbed).queue();

            final var guildById = philJda.getGuildById(baseConfig.guildId);

            var commandListUpdateAction = guildById.updateCommands();

            for (final var slashCommand : slashCommands) {
                if (CollectionUtils.isNotEmpty(slashCommand.getOptions())) {
                    commandListUpdateAction = commandListUpdateAction.addCommands(Commands.slash(slashCommand.getName(), slashCommand.getHelp())
                            .addOptions(slashCommand.getOptions()));
                } else {
                    commandListUpdateAction = commandListUpdateAction.addCommands(Commands.slash(slashCommand.getName(), slashCommand.getHelp()));
                }
            }

            commandListUpdateAction.submit().whenComplete((success, err) -> {
                if (err != null) {
                    log.error("Failed to upsert slash command(s) {}", success.stream().map(net.dv8tion.jda.api.interactions.commands.Command::getName).collect(Collectors.joining(", ")), err);
                    return;
                }

                log.info("Successfully upserted slash command(s) {}", success.stream().map(net.dv8tion.jda.api.interactions.commands.Command::getName).collect(Collectors.joining(", ")));
            });

            applicationContext.getBeansOfType(OnJdaReady.class)
                    .forEach((name, bean) -> Thread.startVirtualThread(bean));
        }

        @Override
        public void onSlashCommandInteraction(final @NotNull SlashCommandInteractionEvent event) {
            for (final var slashCommand : slashCommands) {
                if (event.getName().equals(slashCommand.getName())) {
                    // no guildOnly check because all slash commands are guild only

                    // owner commands
                    if (slashCommand.isOwnerCommand() && !event.getUser().getId().equals(baseConfig.ownerId)) {
                        event.reply("This is an owner command, only Santiago can execute it").queue();
                        return;
                    }

                    // required role check
                    if (Objects.nonNull(slashCommand.getRequiredRole())) {
                        final var requiredRole = slashCommand.getRequiredRole();

                        if (Objects.isNull(event.getMember()) || !slashCommand.hasRole(event.getMember(), requiredRole)) {
                            event.reply(String.format("You must have %s role to execute this command", requiredRole)).queue();
                            return;
                        }
                    }

                    // nsfw check
                    if (slashCommand.isNsfwOnly()) {
                        if (!(event.getChannel() instanceof IAgeRestrictedChannel ageRestrictedChannel) || !ageRestrictedChannel.isNSFW()) {
                            event.reply("This is an nsfw only command, it cannot be executed in this channel").queue();
                            return;
                        }
                    }

                    slashCommand.execute(event);
                    return;
                }
            }
        }

        @Override
        public void onCommandAutoCompleteInteraction(final @NotNull CommandAutoCompleteInteractionEvent event) {
            for (final var slashCommand : slashCommands) {
                if (event.getName().equals(slashCommand.getName())) {
                    slashCommand.onAutoComplete(event);
                    return;
                }
            }
        }

        @Override
        public void onGuildVoiceUpdate(final @NotNull GuildVoiceUpdateEvent event) {
            if (event.getChannelJoined() != null) {
                swampyCommand.voiceJoined(event.getMember());
            } else if (event.getChannelLeft() != null) {
                swampyCommand.voiceLeft(event.getMember());

                if (CollectionUtils.size(event.getChannelLeft().asVoiceChannel().getMembers()) == 1) {
                    event.getGuild().getAudioManager().closeAudioConnection();
                }
            }
        }

        @Override
        public void onMessageReactionAdd(final @NotNull MessageReactionAddEvent event) {
            if (event.getUser() != null && event.getUser().isBot()) {
                return;
            }

            final var emoji = event.getReaction().getEmoji();
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
        public void onGuildMemberRoleAdd(final @NotNull GuildMemberRoleAddEvent event) {
            if (event.getRoles().stream().anyMatch(r -> r.getName().equals(Constants.CHAOS_CHILDREN_ROLE) || r.getName().equals(Constants.EIGHTEEN_PLUS_ROLE))) {
                final var mention = event.getMember().getAsMention();
                event.getGuild().getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).getFirst().sendMessage(mention + " Congratulations on not being a newbie " +
                        "anymore my chaotic friend! Now you're entered into The Swampys, our server wide chaos games. " +
                        "You don't have to actively participate, but if you'd like to join the chaos with us, take a peek at what The Swampys has to offer.\n\n" +
                        "https://discordapp.com/channels/740999022340341791/761398315119280158/761411776561807410").queue();
            }
        }

        @Override
        public void onGuildMemberJoin(final @NotNull GuildMemberJoinEvent event) {
            memberCount.updateCount();

            event.getGuild().getTextChannelsByName("landing-zone", false).stream().findFirst().ifPresent(landingZone -> {
                final var guildName = event.getGuild().getName();
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
        public void onGuildBan(final @NotNull GuildBanEvent event) {
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
        public void onGuildMemberRemove(final @NotNull GuildMemberRemoveEvent event) {
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
        public void onGuildMemberUpdateNickname(final @NotNull GuildMemberUpdateNicknameEvent event) {
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
        public void onUserUpdateName(final @NotNull UserUpdateNameEvent event) {
            event.getJDA().getTextChannelsByName(Constants.MOD_LOGS_CHANNEL, false).stream().findAny().ifPresent(channel -> {
                channel.sendMessageEmbeds(Constants.simpleEmbedThumbnail(
                                event.getOldName() + " Updated Username",
                                String.format("%s\nOld Username = %s\nNew Username = %s", event.getUser().getAsMention(), event.getOldName(), event.getNewName()),
                                event.getUser().getEffectiveAvatarUrl()))
                        .queue();
            });
        }

        @Override
        public void onUserUpdateAvatar(final UserUpdateAvatarEvent event) {
            logAvatarUpdate(event.getUser(), event.getNewAvatarUrl(), event.getOldAvatarUrl());
        }

        @Override
        public void onGuildMemberUpdateAvatar(final @NotNull GuildMemberUpdateAvatarEvent event) {
            logAvatarUpdate(event.getUser(), event.getNewAvatarUrl(), event.getOldAvatarUrl());
        }

        private void logAvatarUpdate(final User user, final String oldAvatar, final String newAvatar) {
            philJda.getTextChannelsByName(Constants.MOD_LOGS_CHANNEL, false).stream().findAny().ifPresent(channel -> {
                channel.sendMessageEmbeds(Constants.simpleEmbedThumbnail(
                                user.getEffectiveName() + " Updated Avatar",
                                user.getAsMention() + "\nOld avatar is thumbnail (unless phil didn't have it cached)\nNew avatar is larger image",
                                newAvatar,
                                oldAvatar))
                        .queue();
            });
        }

        record DadJokeRecord(String joke) {}
    }
}
