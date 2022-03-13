package com.badfic.philbot.listeners;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.BaseResponsesConfig;
import com.badfic.philbot.data.BaseResponsesConfigRepository;
import com.badfic.philbot.data.GenericBotResponsesConfigJson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import io.honeybadger.reporter.HoneybadgerReporter;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public abstract class BasicResponsesBot<T extends BaseResponsesConfig> extends Command {

    private static final BufferedImage HUG;

    private final BaseResponsesConfigRepository<T> configRepository;
    private final ObjectMapper objectMapper;
    private final HoneybadgerReporter honeybadgerReporter;
    private final String fullCmdPrefix;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String modHelp;

    static {
        try {
            HUG = ImageIO.read(Objects.requireNonNull(BasicResponsesBot.class.getClassLoader().getResourceAsStream("flags/hug.png")));
        } catch (IOException e) {
            throw new IllegalStateException("Could not load hug.png");
        }
    }

    public BasicResponsesBot(BaseResponsesConfigRepository<T> configRepository,
                             ObjectMapper objectMapper, HoneybadgerReporter honeybadgerReporter, String name, String sfwBootstrapJson, String nsfwBootstrapJson,
                             Supplier<T> responsesConfigConstructor) throws Exception {
        this.configRepository = configRepository;
        this.objectMapper = objectMapper;
        this.honeybadgerReporter = honeybadgerReporter;

        this.name = name;
        this.fullCmdPrefix = Constants.PREFIX + name;
        String rawHelp = "Any message containing `<name>` will make <name> respond with a random message if that channel is configured.";
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
        if (configRepository.findById(BaseResponsesConfig.SINGLETON_ID).isEmpty()) {
            GenericBotResponsesConfigJson kidFriendlyConfig = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream(sfwBootstrapJson),
                    GenericBotResponsesConfigJson.class);
            GenericBotResponsesConfigJson nsfwConfig = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream(nsfwBootstrapJson),
                    GenericBotResponsesConfigJson.class);
            nsfwConfig.getResponses().addAll(kidFriendlyConfig.getResponses());

            T responsesConfig = responsesConfigConstructor.get();
            responsesConfig.setId(BaseResponsesConfig.SINGLETON_ID);
            responsesConfig.setSfwConfig(kidFriendlyConfig);
            responsesConfig.setNsfwConfig(nsfwConfig);
            configRepository.save(responsesConfig);
        }
    }

    @Override
    public void execute(CommandEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        final JDA selfJda = event.getJDA();
        final Guild guild = selfJda.getGuilds().get(0);

        Optional<T> optionalConfig = configRepository.findById(BaseResponsesConfig.SINGLETON_ID);
        if (optionalConfig.isEmpty()) {
            guild.getTextChannelById(event.getChannel().getId())
                    .sendMessageFormat("%s, failed to read %s entries from database :(", event.getAuthor().getAsMention(), name)
                    .queue();
            return;
        }

        T responsesConfig = optionalConfig.get();
        String msgContent = event.getMessage().getContentRaw();

        if (msgContent.startsWith(fullCmdPrefix)) {
            if (event.getMember()
                    .getRoles()
                    .stream()
                    .noneMatch(r -> r.getName().equalsIgnoreCase(Constants.ADMIN_ROLE))) {
                event.replyError("You do not have the correct role to use the " + name + " command");
                return;
            }

            if (msgContent.startsWith(fullCmdPrefix + " help")) {
                event.replyInDm(Constants.simpleEmbed(name + " Help", help));
                return;
            } else if (msgContent.startsWith(fullCmdPrefix + " nsfw add channel")) {
                List<TextChannel> mentionedChannels = event.getMessage().getMentionedChannels();
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

                responsesConfig.getNsfwConfig().getChannels().add(mentionedChannels.get(0).getName());
                configRepository.save(responsesConfig);
                event.getChannel().sendMessageFormat("%s, saved %s to nsfw config", event.getAuthor().getAsMention(), mentionedChannels.get(0).getAsMention())
                        .queue();
            } else if (msgContent.startsWith(fullCmdPrefix + " nsfw remove channel")) {
                List<TextChannel> mentionedChannels = event.getMessage().getMentionedChannels();
                if (CollectionUtils.isEmpty(mentionedChannels)) {
                    String channelName = msgContent.replace(fullCmdPrefix + " nsfw remove channel", "").trim();

                    if (StringUtils.isEmpty(channelName)) {
                        event.getChannel().sendMessageFormat("%s, please specify a channel. `%s nsfw remove channel #general`",
                                event.getAuthor().getAsMention(), fullCmdPrefix).queue();
                        return;
                    }

                    responsesConfig.getNsfwConfig().getChannels().remove(channelName);
                    configRepository.save(responsesConfig);
                    event.getChannel().sendMessageFormat("%s, removed %s from nsfw config", event.getAuthor().getAsMention(), channelName).queue();
                    return;
                }

                if (mentionedChannels.size() > 1) {
                    event.getChannel().sendMessageFormat("%s, please only specify one channel. `%s nsfw remove channel #cursed`",
                            event.getAuthor().getAsMention(), fullCmdPrefix).queue();
                    return;
                }

                responsesConfig.getNsfwConfig().getChannels().remove(mentionedChannels.get(0).getName());
                configRepository.save(responsesConfig);
                event.getChannel().sendMessageFormat("%s, removed %s from nsfw config",
                        event.getAuthor().getAsMention(), mentionedChannels.get(0).getAsMention()).queue();
            } else if (msgContent.startsWith(fullCmdPrefix + " sfw add channel")) {
                List<TextChannel> mentionedChannels = event.getMessage().getMentionedChannels();
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

                responsesConfig.getSfwConfig().getChannels().add(mentionedChannels.get(0).getName());
                configRepository.save(responsesConfig);
                event.getChannel().sendMessageFormat("%s, saved %s to sfw config", event.getAuthor().getAsMention(), mentionedChannels.get(0).getAsMention())
                        .queue();
            } else if (msgContent.startsWith(fullCmdPrefix + " sfw remove channel")) {
                List<TextChannel> mentionedChannels = event.getMessage().getMentionedChannels();
                if (CollectionUtils.isEmpty(mentionedChannels)) {
                    String channelName = msgContent.replace(fullCmdPrefix + " sfw remove channel", "").trim();

                    if (StringUtils.isEmpty(channelName)) {
                        event.getChannel().sendMessageFormat("%s, please specify a channel. `%s sfw remove channel #general`",
                                event.getAuthor().getAsMention(), fullCmdPrefix).queue();
                        return;
                    }

                    responsesConfig.getSfwConfig().getChannels().remove(channelName);
                    configRepository.save(responsesConfig);
                    event.getChannel().sendMessageFormat("%s, removed %s from sfw config", event.getAuthor().getAsMention(), channelName).queue();
                    return;
                }

                if (mentionedChannels.size() > 1) {
                    event.getChannel().sendMessageFormat("%s, please only specify one channel. `%s sfw remove channel #general`",
                            event.getAuthor().getAsMention(), fullCmdPrefix).queue();
                    return;
                }

                responsesConfig.getSfwConfig().getChannels().remove(mentionedChannels.get(0).getName());
                configRepository.save(responsesConfig);
                event.getChannel().sendMessageFormat("%s, removed %s from sfw config",
                        event.getAuthor().getAsMention(), mentionedChannels.get(0).getAsMention()).queue();
            } else if (msgContent.startsWith(fullCmdPrefix + " nsfw add")) {
                String saying = msgContent.replace(fullCmdPrefix + " nsfw add", "").trim();
                if (saying.isEmpty()) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a saying to add").queue();
                    return;
                }
                responsesConfig.getNsfwConfig().getResponses().add(saying);
                configRepository.save(responsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", saved `" + saying + "` to nsfw config").queue();
            } else if (msgContent.startsWith(fullCmdPrefix + " nsfw remove")) {
                String saying = msgContent.replace(fullCmdPrefix + " nsfw remove", "").trim();
                if (saying.isEmpty()) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a saying to remove").queue();
                    return;
                }

                if (saying.startsWith("`") && saying.endsWith("`")) {
                    saying = StringUtils.substringAfter(saying, "`");
                    saying = StringUtils.substringBefore(saying, "`");
                }

                responsesConfig.getNsfwConfig().getResponses().remove(saying);
                configRepository.save(responsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", removed `" + saying + "` from nsfw config").queue();
            } else if (msgContent.startsWith(fullCmdPrefix + " sfw add")) {
                String saying = msgContent.replace(fullCmdPrefix + " sfw add", "").trim();
                if (saying.isEmpty()) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a saying to add").queue();
                    return;
                }
                responsesConfig.getSfwConfig().getResponses().add(saying);
                responsesConfig.getNsfwConfig().getResponses().add(saying);
                configRepository.save(responsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", saved `" + saying + "` to sfw config").queue();
            } else if (msgContent.startsWith(fullCmdPrefix + " sfw remove")) {
                String saying = msgContent.replace(fullCmdPrefix + " sfw remove", "").trim();
                if (saying.isEmpty()) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a saying to remove").queue();
                    return;
                }

                if (saying.startsWith("`") && saying.endsWith("`")) {
                    saying = StringUtils.substringAfter(saying, "`");
                    saying = StringUtils.substringBefore(saying, "`");
                }

                responsesConfig.getSfwConfig().getResponses().remove(saying);
                responsesConfig.getNsfwConfig().getResponses().remove(saying);
                configRepository.save(responsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", removed `" + saying + "` from sfw config").queue();
            } else if (msgContent.startsWith(fullCmdPrefix + " nsfw config")) {
                try {
                    event.getChannel().sendFile(objectMapper.writeValueAsBytes(responsesConfig.getNsfwConfig()), name + "-nsfwconfig.json").queue();
                } catch (JsonProcessingException e) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", fatal error could not send nsfw config to you").queue();
                }
            } else if (msgContent.startsWith(fullCmdPrefix + " sfw config")) {
                try {
                    event.getChannel().sendFile(objectMapper.writeValueAsBytes(responsesConfig.getSfwConfig()), name + "-sfwconfig.json").queue();
                } catch (JsonProcessingException e) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", fatal error could not send sfw config to you").queue();
                }
            } else if (msgContent.startsWith(fullCmdPrefix + " tts")) {
                getResponse(event, responsesConfig).ifPresent(response -> {
                    Message outboundMessage = new MessageBuilder(event.getAuthor().getAsMention() + ", " + response)
                            .setTTS(true)
                            .build();

                    event.getChannel().sendMessage(outboundMessage).queue();
                });
            } else {
                event.getChannel().sendMessageFormat("%s, unrecognized %s command", event.getAuthor().getAsMention(), name).queue();
            }
            return;
        }

        if (StringUtils.containsIgnoreCase(msgContent, "hug")) {
            // Width and Height for scaled profile images: 160px
            // Left image coordinates: (27, 63)
            // Right image coordinate: (187, 24)

            try {
                String selfAvatarUrl = selfJda.getSelfUser().getEffectiveAvatarUrl();

                long authorId = event.getAuthor().getIdLong();
                Member authorMember = guild.getMemberById(authorId);
                String authorAvatarUrl = authorMember != null ? authorMember.getEffectiveAvatarUrl() : event.getAuthor().getEffectiveAvatarUrl();

                BufferedImage scaledProfileImage = scaleProfileTo160(selfAvatarUrl);
                BufferedImage scaledAuthorImage = scaleProfileTo160(authorAvatarUrl);

                BufferedImage outputImg = new BufferedImage(HUG.getWidth(), HUG.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = outputImg.createGraphics();

                // clear
                graphics.setComposite(AlphaComposite.Clear);
                graphics.fillRect(0, 0, outputImg.getWidth(), outputImg.getHeight());

                // draw profile image
                graphics.setComposite(AlphaComposite.SrcOver);
                graphics.drawImage(scaledProfileImage, 27, 63, null);

                // draw author image
                graphics.setComposite(AlphaComposite.SrcOver);
                graphics.drawImage(scaledAuthorImage, 187, 24, null);

                // draw hug over top
                graphics.setComposite(AlphaComposite.SrcOver);
                graphics.drawImage(HUG, 0, 0, null);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(outputImg, "png", outputStream);
                graphics.dispose();

                guild.getTextChannelById(event.getChannel().getId())
                        .sendMessage(" ")
                        .addFile(outputStream.toByteArray(), "hug.png")
                        .queue();

                return;
            } catch (Exception e) {
                guild.getTextChannelById(event.getChannel().getId()).sendMessage("\uD83E\uDD17").queue();

                honeybadgerReporter.reportError(e, null, getClass().getSimpleName() + " could not hug user " + event.getAuthor().getAsMention());
            }
        }

        getResponse(event, responsesConfig).ifPresent(response -> {
            guild.getTextChannelById(event.getChannel().getId())
                    .sendMessage(StringUtils.startsWithIgnoreCase(response, "http") ? response : (event.getAuthor().getAsMention() + ", " + response)).queue();
        });
    }

    protected abstract Optional<String> getResponse(CommandEvent event, T responsesConfig);

    private BufferedImage scaleProfileTo160(String effectiveAvatarUrl) throws Exception {
        BufferedImage profilePic = ImageIO.read(new URL(effectiveAvatarUrl));

        // scale it to 160x160
        Image scaledProfileTmp = profilePic.getScaledInstance(160, 160, Image.SCALE_SMOOTH);
        BufferedImage scaledProfile = new BufferedImage(160, 160, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphicsScaledProfile = scaledProfile.createGraphics();
        graphicsScaledProfile.drawImage(scaledProfileTmp, 0, 0, null);
        graphicsScaledProfile.dispose();

        return scaledProfile;
    }

}
