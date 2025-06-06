package com.badfic.philbot.listeners;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.ModHelpAware;
import com.badfic.philbot.commands.bang.BaseBangCommand;
import com.badfic.philbot.commands.bang.image.ImageUtils;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.BaseResponsesConfig;
import com.badfic.philbot.data.BaseResponsesConfigRepository;
import com.badfic.philbot.data.GenericBotResponsesConfigJson;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

@Slf4j
public abstract class BasicResponsesBot<T extends BaseResponsesConfig> extends BaseBangCommand implements ModHelpAware {

    private final BaseResponsesConfigRepository<T> configRepository;
    private final Function<SwampyGamesConfig, String> usernameGetter;
    private final Function<SwampyGamesConfig, String> avatarGetter;
    private final String fullCmdPrefix;
    @Getter private final String modHelp;

    public BasicResponsesBot(final BaseResponsesConfigRepository<T> configRepository,
                             final JdbcAggregateTemplate jdbcAggregateTemplate,
                             final String name,
                             final Supplier<T> responsesConfigConstructor,
                             final Function<SwampyGamesConfig, String> usernameGetter,
                             final Function<SwampyGamesConfig, String> avatarGetter) {
        this.configRepository = configRepository;
        this.usernameGetter = usernameGetter;
        this.avatarGetter = avatarGetter;

        this.name = name;
        this.fullCmdPrefix = Constants.PREFIX + name;
        final var rawHelp = "Any message containing `<name>` will make <name> respond with a random message if that channel is configured.";
        this.help = StringUtils.replace(rawHelp, "<name>", name, -1);
        this.modHelp = this.help + '\n' + StringUtils.replace("""
                `!!<name> tts` responds with a random message but spoken via text-to-speech
                `!!<name> nsfw add channel #channel` adds a channel to the list of channels nsfw <name> responds to
                `!!<name> nsfw remove channel #channel` removes a channel from the list of channels nsfw <name> responds to
                `!!<name> sfw add channel #channel` adds a channel to the list of channels normal <name> responds to
                `!!<name> sfw remove channel #channel` removes a channel from the list of channels normal <name> responds to
                `!!<name> nsfw add something something` adds 'something something' to the list of responses <name> has for nsfw channels
                `!!<name> nsfw remove something something` removes 'something something' from the list of responses <name> has for nsfw channels
                `!!<name> sfw add something something` adds 'something something' to the list of responses <name> has for normal channels
                `!!<name> sfw remove something something` removes 'something something' from the list of responses <name> has for normal channels
                `!!<name> nsfw config` responds with a json file of the nsfw config
                `!!<name> sfw config` responds with a json file of the normal config
                """, "<name>", name, -1);

        // seed data if needed
        if (configRepository.findById(Constants.DATA_SINGLETON_ID).isEmpty()) {
            final var kidFriendlyConfig = new GenericBotResponsesConfigJson(Set.of(Constants.SWAMPYS_CHANNEL), Set.of("yes", "no"));

            final var nsfwConfig = new GenericBotResponsesConfigJson(Set.of(Constants.CURSED_SWAMP_CHANNEL), Set.of("heck yes", "heck no"));

            final var responsesConfig = responsesConfigConstructor.get();
            responsesConfig.setId(Constants.DATA_SINGLETON_ID);
            responsesConfig.setSfwConfig(kidFriendlyConfig);
            responsesConfig.setNsfwConfig(nsfwConfig);

            jdbcAggregateTemplate.insert(responsesConfig);
        }
    }

    @Override
    public void execute(final CommandEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        final var guild = philJda.getGuildById(baseConfig.guildId);

        final var optionalConfig = configRepository.findById(Constants.DATA_SINGLETON_ID);
        final var channelId = event.getChannel().getIdLong();
        if (optionalConfig.isEmpty()) {
            philJda.getTextChannelById(channelId)
                    .sendMessageFormat("%s, failed to read %s entries from database :(", event.getAuthor().getAsMention(), name)
                    .queue();
            return;
        }

        final var responsesConfig = optionalConfig.get();
        final var msgContent = event.getMessage().getContentRaw();

        if (msgContent.startsWith(fullCmdPrefix)) {
            if (event.getMember()
                    .getUnsortedRoles()
                    .stream()
                    .noneMatch(r -> r.getName().equalsIgnoreCase(Constants.ADMIN_ROLE))) {
                event.replyError("You do not have the correct role to use the " + name + " command");
                return;
            }

            if (msgContent.startsWith(fullCmdPrefix + " help")) {
                event.replyInDm(Constants.simpleEmbed(name + " Help", help));
                return;
            } else if (msgContent.startsWith(fullCmdPrefix + " nsfw add channel")) {
                final var mentionedChannels = event.getMessage().getMentions().getChannels(TextChannel.class);
                if (CollectionUtils.isEmpty(mentionedChannels)) {
                    event.getChannel().sendMessageFormat("%s, please specify a channel. `%s nsfw add channel #cursed`",
                            event.getAuthor().getAsMention(), fullCmdPrefix).queue();
                    return;
                }

                if (mentionedChannels.size() > 1) {
                    event.getChannel().sendMessageFormat("%s, please only specify one channel. `%s nsfw add channel #cursed`",
                            event.getAuthor().getAsMention(), fullCmdPrefix).queue();
                    return;
                }

                responsesConfig.getNsfwConfig().channels().add(mentionedChannels.getFirst().getName());
                configRepository.save(responsesConfig);
                event.getChannel().sendMessageFormat("%s, saved %s to nsfw config", event.getAuthor().getAsMention(), mentionedChannels.getFirst().getAsMention())
                        .queue();
            } else if (msgContent.startsWith(fullCmdPrefix + " nsfw remove channel")) {
                final var mentionedChannels = event.getMessage().getMentions().getChannels(TextChannel.class);
                if (CollectionUtils.isEmpty(mentionedChannels)) {
                    final var channelName = msgContent.replace(fullCmdPrefix + " nsfw remove channel", "").trim();

                    if (StringUtils.isEmpty(channelName)) {
                        event.getChannel().sendMessageFormat("%s, please specify a channel. `%s nsfw remove channel #general`",
                                event.getAuthor().getAsMention(), fullCmdPrefix).queue();
                        return;
                    }

                    responsesConfig.getNsfwConfig().channels().remove(channelName);
                    configRepository.save(responsesConfig);
                    event.getChannel().sendMessageFormat("%s, removed %s from nsfw config", event.getAuthor().getAsMention(), channelName).queue();
                    return;
                }

                if (mentionedChannels.size() > 1) {
                    event.getChannel().sendMessageFormat("%s, please only specify one channel. `%s nsfw remove channel #cursed`",
                            event.getAuthor().getAsMention(), fullCmdPrefix).queue();
                    return;
                }

                responsesConfig.getNsfwConfig().channels().remove(mentionedChannels.getFirst().getName());
                configRepository.save(responsesConfig);
                event.getChannel().sendMessageFormat("%s, removed %s from nsfw config",
                        event.getAuthor().getAsMention(), mentionedChannels.getFirst().getAsMention()).queue();
            } else if (msgContent.startsWith(fullCmdPrefix + " sfw add channel")) {
                final var mentionedChannels = event.getMessage().getMentions().getChannels(TextChannel.class);
                if (CollectionUtils.isEmpty(mentionedChannels)) {
                    event.getChannel().sendMessageFormat("%s, please specify a channel. `%s sfw add channel #general`",
                            event.getAuthor().getAsMention(), fullCmdPrefix).queue();
                    return;
                }

                if (mentionedChannels.size() > 1) {
                    event.getChannel().sendMessageFormat("%s, please only specify one channel. `%s sfw add channel #general`",
                            event.getAuthor().getAsMention(), fullCmdPrefix).queue();
                    return;
                }

                responsesConfig.getSfwConfig().channels().add(mentionedChannels.getFirst().getName());
                configRepository.save(responsesConfig);
                event.getChannel().sendMessageFormat("%s, saved %s to sfw config", event.getAuthor().getAsMention(), mentionedChannels.getFirst().getAsMention())
                        .queue();
            } else if (msgContent.startsWith(fullCmdPrefix + " sfw remove channel")) {
                final var mentionedChannels = event.getMessage().getMentions().getChannels(TextChannel.class);
                if (CollectionUtils.isEmpty(mentionedChannels)) {
                    final var channelName = msgContent.replace(fullCmdPrefix + " sfw remove channel", "").trim();

                    if (StringUtils.isEmpty(channelName)) {
                        event.getChannel().sendMessageFormat("%s, please specify a channel. `%s sfw remove channel #general`",
                                event.getAuthor().getAsMention(), fullCmdPrefix).queue();
                        return;
                    }

                    responsesConfig.getSfwConfig().channels().remove(channelName);
                    configRepository.save(responsesConfig);
                    event.getChannel().sendMessageFormat("%s, removed %s from sfw config", event.getAuthor().getAsMention(), channelName).queue();
                    return;
                }

                if (mentionedChannels.size() > 1) {
                    event.getChannel().sendMessageFormat("%s, please only specify one channel. `%s sfw remove channel #general`",
                            event.getAuthor().getAsMention(), fullCmdPrefix).queue();
                    return;
                }

                responsesConfig.getSfwConfig().channels().remove(mentionedChannels.getFirst().getName());
                configRepository.save(responsesConfig);
                event.getChannel().sendMessageFormat("%s, removed %s from sfw config",
                        event.getAuthor().getAsMention(), mentionedChannels.getFirst().getAsMention()).queue();
            } else if (msgContent.startsWith(fullCmdPrefix + " nsfw add")) {
                final var saying = msgContent.replace(fullCmdPrefix + " nsfw add", "").trim();
                if (saying.isEmpty()) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a saying to add").queue();
                    return;
                }
                responsesConfig.getNsfwConfig().responses().add(saying);
                configRepository.save(responsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", saved `" + saying + "` to nsfw config").queue();
            } else if (msgContent.startsWith(fullCmdPrefix + " nsfw remove")) {
                var saying = msgContent.replace(fullCmdPrefix + " nsfw remove", "").trim();
                if (saying.isEmpty()) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a saying to remove").queue();
                    return;
                }

                if (saying.startsWith("`") && saying.endsWith("`")) {
                    saying = StringUtils.substringAfter(saying, "`");
                    saying = StringUtils.substringBefore(saying, "`");
                }

                responsesConfig.getNsfwConfig().responses().remove(saying);
                configRepository.save(responsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", removed `" + saying + "` from nsfw config").queue();
            } else if (msgContent.startsWith(fullCmdPrefix + " sfw add")) {
                final var saying = msgContent.replace(fullCmdPrefix + " sfw add", "").trim();
                if (saying.isEmpty()) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a saying to add").queue();
                    return;
                }
                responsesConfig.getSfwConfig().responses().add(saying);
                responsesConfig.getNsfwConfig().responses().add(saying);
                configRepository.save(responsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", saved `" + saying + "` to sfw config").queue();
            } else if (msgContent.startsWith(fullCmdPrefix + " sfw remove")) {
                var saying = msgContent.replace(fullCmdPrefix + " sfw remove", "").trim();
                if (saying.isEmpty()) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a saying to remove").queue();
                    return;
                }

                if (saying.startsWith("`") && saying.endsWith("`")) {
                    saying = StringUtils.substringAfter(saying, "`");
                    saying = StringUtils.substringBefore(saying, "`");
                }

                responsesConfig.getSfwConfig().responses().remove(saying);
                responsesConfig.getNsfwConfig().responses().remove(saying);
                configRepository.save(responsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", removed `" + saying + "` from sfw config").queue();
            } else if (msgContent.startsWith(fullCmdPrefix + " nsfw config")) {
                try {
                    event.getChannel().sendFiles(FileUpload.fromData(objectMapper.writeValueAsBytes(responsesConfig.getNsfwConfig()), name + "-nsfwconfig.json")).queue();
                } catch (final JsonProcessingException e) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", fatal error could not send nsfw config to you").queue();
                }
            } else if (msgContent.startsWith(fullCmdPrefix + " sfw config")) {
                try {
                    event.getChannel().sendFiles(FileUpload.fromData(objectMapper.writeValueAsBytes(responsesConfig.getSfwConfig()), name + "-sfwconfig.json")).queue();
                } catch (final JsonProcessingException e) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", fatal error could not send sfw config to you").queue();
                }
            } else if (msgContent.startsWith(fullCmdPrefix + " tts")) {
                getResponse(event, responsesConfig).ifPresent(response -> {

                    final var outboundMessage = new MessageCreateBuilder()
                            .addContent(event.getAuthor().getAsMention() + ", " + response)
                            .setTTS(true)
                            .build();

                    event.getChannel().sendMessage(outboundMessage).queue();
                });
            } else {
                event.getChannel().sendMessageFormat("%s, unrecognized %s command", event.getAuthor().getAsMention(), name).queue();
            }
            return;
        }

        final var swampyGamesConfig = getSwampyGamesConfig();

        if (StringUtils.containsIgnoreCase(msgContent, "hug")) {
            // Width and Height for scaled profile images: 160px
            // Left image coordinates: (27, 63)
            // Right image coordinate: (187, 24)

            try {
                final var authorId = event.getAuthor().getIdLong();
                final var authorMember = guild.getMemberById(authorId);
                final var authorAvatarUrl = authorMember != null ? authorMember.getEffectiveAvatarUrl() : event.getAuthor().getEffectiveAvatarUrl();

                final BufferedImage hugImage;
                try (final var stream = BasicResponsesBot.class.getClassLoader().getResourceAsStream("flags/hug.png")) {
                    hugImage = ImageIO.read(Objects.requireNonNull(stream));
                }

                final String botAvatarUrl;
                if (usernameGetter == null) {
                    botAvatarUrl = philJda.getSelfUser().getEffectiveAvatarUrl();
                } else {
                    botAvatarUrl = avatarGetter.apply(swampyGamesConfig);
                }

                philJda.getTextChannelById(channelId)
                        .sendMessageEmbeds(new EmbedBuilder().setImage(botAvatarUrl).build())
                        .queue(msg -> {
                            // This nightmare is because Discord disallowed hot-linking.
                            // We have to "re-upload" the image, pull the newly generated proxyUrl, then delete the "re-uploaded" message.
                            final var fetchedUrl = msg.getEmbeds().getFirst().getImage().getProxyUrl();

                            final byte[] bytes;
                            try {
                                bytes = ImageUtils.makeTwoUserMemeImageBytes(fetchedUrl, 160, 27, 63, authorAvatarUrl, 160, 187, 24, hugImage);
                            } catch (final Exception e) {
                                msg.delete().queue();
                                philJda.getTextChannelById(channelId)
                                        .sendMessage("HUG!")
                                        .queue();
                                return;
                            }

                            msg.delete().queue();
                            philJda.getTextChannelById(channelId)
                                    .sendMessage(" ")
                                    .addFiles(FileUpload.fromData(bytes, "hug.png"))
                                    .queue();
                        });
                return;
            } catch (final Exception e) {
                log.error("{} could not hug [user={}]", getClass().getSimpleName(), event.getAuthor().getAsMention(), e);
                philJda.getTextChannelById(channelId).sendMessage("\uD83E\uDD17").queue();
            }
        }

        getResponse(event, responsesConfig).ifPresent(response -> {
            final var message = StringUtils.startsWithIgnoreCase(response, "http") ? response : (event.getAuthor().getAsMention() + ", " + response);
            if (usernameGetter == null) {
                philJda.getTextChannelById(channelId)
                        .sendMessage(message)
                        .queue();
                return;
            }

            discordWebhookSendService.sendMessage(channelId, usernameGetter.apply(swampyGamesConfig), avatarGetter.apply(swampyGamesConfig), message);
        });
    }

    protected Optional<String> getResponse(final CommandEvent event, final T responsesConfig) {
        String channelName = event.getChannel().getName();
        final Set<String> responses;
        if (responsesConfig.getSfwConfig().channels().contains(channelName)) {
            responses = responsesConfig.getSfwConfig().responses();
        } else if (responsesConfig.getNsfwConfig().channels().contains(channelName)) {
            responses = responsesConfig.getNsfwConfig().responses();
        } else {
            return Optional.empty();
        }

        final var responseValue = Constants.pickRandom(responses);

        if (StringUtils.startsWithIgnoreCase(responseValue, "http")) {
            return Optional.of(responseValue);
        }

        final var msgContent = event.getMessage().getContentRaw();
        var isAllUppercase = true;
        for (final var s : msgContent.split("\\s+")) {
            isAllUppercase &= StringUtils.isAllUpperCase(s);
        }

        if (isAllUppercase) {
            return Optional.of(responseValue.toUpperCase(Locale.ENGLISH));
        }

        return Optional.of(responseValue);
    }

}
