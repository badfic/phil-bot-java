package com.badfic.philbot.listeners;

import com.badfic.philbot.config.PhilbotAppConfig;
import com.badfic.philbot.data.PhilResponsesConfig;
import com.badfic.philbot.model.PhilConfigJson;
import com.badfic.philbot.repository.PhilResponsesConfigRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class PhilCommand extends Command {

    private final boolean isTestEnvironment;
    private final PhilResponsesConfigRepository philResponsesConfigRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public PhilCommand(ObjectMapper objectMapper, PhilbotAppConfig philbotAppConfig, PhilResponsesConfigRepository philResponsesConfigRepository)
            throws Exception {
        isTestEnvironment = "test".equalsIgnoreCase(philbotAppConfig.nodeEnvironment);
        name = "phil";
        help = "Any message containing `phil|klemmer|phellen` will make Phil respond with a random message if that channel is configured.\n" +
                "\t`!!phil tts` responds with a random message but spoken via text-to-speech\n" +
                "\t`!!phil nsfw add channel #channel` adds a channel to the list of channels nsfw phil responds to\n" +
                "\t`!!phil nsfw remove channel #channel` removes a channel from the list of channels nsfw phil responds to\n" +
                "\t`!!phil sfw add channel #channel` adds a channel to the list of channels normal phil responds to\n" +
                "\t`!!phil sfw remove channel #channel` removes a channel from the list of channels normal phil responds to\n" +
                "\t`!!phil nsfw add something something` adds 'something something' to the list of responses phil has for nsfw channels\n" +
                "\t`!!phil nsfw remove something something` removes 'something something' from the list of responses phil has for nsfw channels\n" +
                "\t`!!phil sfw add something something` adds 'something something' to the list of responses phil has for normal channels\n" +
                "\t`!!phil sfw remove something something` removes 'something something' from the list of responses phil has for normal channels\n" +
                "\t`!!phil nsfw config` responds with a json file of the nsfw config\n" +
                "\t`!!phil sfw config` responds with a json file of the normal config\n";
        requiredRole = "Queens of the Castle";
        this.philResponsesConfigRepository = philResponsesConfigRepository;
        this.objectMapper = objectMapper;

        // seed data if needed
        if (!philResponsesConfigRepository.findById(PhilResponsesConfig.SINGLETON_ID).isPresent()) {
            PhilConfigJson kidFriendlyConfig = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("kidFriendlyConfig.json"),
                    PhilConfigJson.class);
            PhilConfigJson nsfwConfig = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("nsfwConfig.json"), PhilConfigJson.class);
            nsfwConfig.getResponses().addAll(kidFriendlyConfig.getResponses());

            PhilResponsesConfig philResponsesConfig = new PhilResponsesConfig();
            philResponsesConfig.setId(PhilResponsesConfig.SINGLETON_ID);
            philResponsesConfig.setSfwConfig(kidFriendlyConfig);
            philResponsesConfig.setNsfwConfig(nsfwConfig);
            philResponsesConfigRepository.save(philResponsesConfig);
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        Optional<PhilResponsesConfig> optionalConfig = philResponsesConfigRepository.findById(PhilResponsesConfig.SINGLETON_ID);
        if (!optionalConfig.isPresent()) {
            event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", failed to read phil entries from database :(").queue();
            return;
        }

        PhilResponsesConfig philResponsesConfig = optionalConfig.get();
        String msgContent = event.getMessage().getContentRaw();

        if (msgContent.startsWith("!!phil")) {
            if (msgContent.startsWith("!!phil nsfw add channel")) {
                List<TextChannel> mentionedChannels = event.getMessage().getMentionedChannels();
                if (CollectionUtils.isEmpty(mentionedChannels)) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a channel. `!!phil nsfw add channel #cursed`").queue();
                    return;
                }

                if (mentionedChannels.size() > 1) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please only specify one channel. `!!phil nsfw add channel #cursed`")
                            .queue();
                    return;
                }

                philResponsesConfig.getNsfwConfig().getChannels().add(mentionedChannels.get(0).getName());
                philResponsesConfigRepository.save(philResponsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", saved " + mentionedChannels.get(0).getAsMention() + " to nsfw config")
                        .queue();
            } else if (msgContent.startsWith("!!phil nsfw remove channel")) {
                List<TextChannel> mentionedChannels = event.getMessage().getMentionedChannels();
                if (CollectionUtils.isEmpty(mentionedChannels)) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a channel. `!!phil nsfw remove channel #cursed`")
                            .queue();
                    return;
                }

                if (mentionedChannels.size() > 1) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please only specify one channel. `!!phil nsfw remove channel #cursed`")
                            .queue();
                    return;
                }

                philResponsesConfig.getNsfwConfig().getChannels().remove(mentionedChannels.get(0).getName());
                philResponsesConfigRepository.save(philResponsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", removed " + mentionedChannels.get(0).getAsMention() + " from nsfw config")
                        .queue();
            } else if (msgContent.startsWith("!!phil sfw add channel")) {
                List<TextChannel> mentionedChannels = event.getMessage().getMentionedChannels();
                if (CollectionUtils.isEmpty(mentionedChannels)) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a channel. `!!phil sfw add channel #general`").queue();
                    return;
                }

                if (mentionedChannels.size() > 1) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please only specify one channel. `!!phil sfw add channel #general`")
                            .queue();
                    return;
                }

                philResponsesConfig.getSfwConfig().getChannels().add(mentionedChannels.get(0).getName());
                philResponsesConfigRepository.save(philResponsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", saved " + mentionedChannels.get(0).getAsMention() + " to sfw config")
                        .queue();
            } else if (msgContent.startsWith("!!phil sfw remove channel")) {
                List<TextChannel> mentionedChannels = event.getMessage().getMentionedChannels();
                if (CollectionUtils.isEmpty(mentionedChannels)) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a channel. `!!phil sfw remove channel #general`")
                            .queue();
                    return;
                }

                if (mentionedChannels.size() > 1) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please only specify one channel. `!!phil sfw remove channel #general`")
                            .queue();
                    return;
                }

                philResponsesConfig.getSfwConfig().getChannels().remove(mentionedChannels.get(0).getName());
                philResponsesConfigRepository.save(philResponsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", removed " + mentionedChannels.get(0).getAsMention() + " from sfw config")
                        .queue();
            } else if (msgContent.startsWith("!!phil nsfw add")) {
                String saying = msgContent.replace("!!phil nsfw add", "").trim();
                if (saying.isEmpty()) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a saying to add").queue();
                    return;
                }
                philResponsesConfig.getNsfwConfig().getResponses().add(saying);
                philResponsesConfigRepository.save(philResponsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", saved `" + saying + "` to nsfw config").queue();
            } else if (msgContent.startsWith("!!phil nsfw remove")) {
                String saying = msgContent.replace("!!phil nsfw remove", "").trim();
                if (saying.isEmpty()) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a saying to remove").queue();
                    return;
                }
                philResponsesConfig.getNsfwConfig().getResponses().remove(saying);
                philResponsesConfigRepository.save(philResponsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", removed `" + saying + "` from nsfw config").queue();
            } else if (msgContent.startsWith("!!phil sfw add")) {
                String saying = msgContent.replace("!!phil sfw add", "").trim();
                if (saying.isEmpty()) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a saying to add").queue();
                    return;
                }
                philResponsesConfig.getSfwConfig().getResponses().add(saying);
                philResponsesConfig.getNsfwConfig().getResponses().add(saying);
                philResponsesConfigRepository.save(philResponsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", saved `" + saying + "` to sfw config").queue();
            } else if (msgContent.startsWith("!!phil sfw remove")) {
                String saying = msgContent.replace("!!phil sfw remove", "").trim();
                if (saying.isEmpty()) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a saying to remove").queue();
                    return;
                }
                philResponsesConfig.getSfwConfig().getResponses().remove(saying);
                philResponsesConfig.getNsfwConfig().getResponses().remove(saying);
                philResponsesConfigRepository.save(philResponsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", removed `" + saying + "` from sfw config").queue();
            } else if (msgContent.startsWith("!!phil nsfw config")) {
                try {
                    event.getChannel().sendFile(objectMapper.writeValueAsBytes(philResponsesConfig.getNsfwConfig()), "nsfwconfig.json").queue();
                } catch (JsonProcessingException e) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", fatal error could not send nsfw config to you").queue();
                }
            } else if (msgContent.startsWith("!!phil sfw config")) {
                try {
                    event.getChannel().sendFile(objectMapper.writeValueAsBytes(philResponsesConfig.getSfwConfig()), "sfwconfig.json").queue();
                } catch (JsonProcessingException e) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", fatal error could not send sfw config to you").queue();
                }
            } else if (msgContent.startsWith("!!phil tts")) {
                getPhilResponse(event, philResponsesConfig).ifPresent(response -> {
                    Message outboundMessage = new MessageBuilder(event.getAuthor().getAsMention() + ", " + response)
                            .setTTS(true)
                            .build();

                    event.getChannel().sendMessage(outboundMessage).queue();
                });
            } else {
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", unrecognized phil command").queue();
            }
            return;
        }

        getPhilResponse(event, philResponsesConfig).ifPresent(response -> {
            event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", " + response).queue();
        });
    }

    private Optional<String> getPhilResponse(CommandEvent event, PhilResponsesConfig philResponsesConfig) {
        String msgContent = event.getMessage().getContentRaw();
        String channelName = event.getChannel().getName();
        Set<String> responses;
        if (isTestEnvironment) {
            if ("test-channel".equalsIgnoreCase(channelName)) {
                responses = philResponsesConfig.getNsfwConfig().getResponses();
            } else {
                return Optional.empty();
            }
        } else {
            if (philResponsesConfig.getSfwConfig().getChannels().contains(channelName)) {
                responses = philResponsesConfig.getSfwConfig().getResponses();
            } else if (philResponsesConfig.getNsfwConfig().getChannels().contains(channelName)) {
                responses = philResponsesConfig.getNsfwConfig().getResponses();

                if (StringUtils.containsIgnoreCase(msgContent, "you suck")) {
                    return Optional.of("you swallow");
                }
            } else {
                return Optional.empty();
            }
        }

        if (EmojiManager.containsEmoji(msgContent)) {
            Collection<Emoji> allEmoji = EmojiManager.getAll();
            Emoji emoji = pickRandom(allEmoji);
            return Optional.of(emoji.getUnicode());
        }

        return Optional.of(pickRandom(responses));
    }

    private static <T> T pickRandom(Collection<T> collection) {
        int index = ThreadLocalRandom.current().nextInt(collection.size());
        Iterator<T> iterator = collection.iterator();
        for (int i = 0; i < index; i++) {
            iterator.next();
        }
        return iterator.next();
    }

}
