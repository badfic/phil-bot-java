package com.badfic.philbot.listeners.keanu;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.KeanuMarker;
import com.badfic.philbot.data.BaseResponsesConfig;
import com.badfic.philbot.data.keanu.KeanuResponsesConfig;
import com.badfic.philbot.model.GenericBotResponsesConfigJson;
import com.badfic.philbot.repository.KeanuResponsesConfigRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class KeanuCommand extends Command implements KeanuMarker {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Pattern PUPPY_PATTERN = Pattern.compile("\\b(puppy|puppies|pupper|doggo|doge)\\b", Pattern.CASE_INSENSITIVE);
    private static final String HELLO_GIF = "https://gfycat.com/consciousambitiousantipodesgreenparakeet-squarepants-tumbelweed-spongebob-morning-reeves";
    private static final String PUPPIES_GIF = "https://media.giphy.com/media/8rFNes6jllJQRnHTsF/giphy.gif";
    private static final Set<String> GOOD_MORNING_GIFS = new HashSet<>(Arrays.asList(
            HELLO_GIF,
            PUPPIES_GIF,
            "https://cdn.discordapp.com/attachments/323666308107599872/752638622099832852/donut.gif",
            "https://cdn.discordapp.com/attachments/323666308107599872/752638643562086432/smilesmile.gif",
            "https://cdn.discordapp.com/attachments/323666308107599872/752638684641230908/hecute.gif",
            "https://cdn.discordapp.com/attachments/323666308107599872/752638695999537192/beardrub.gif",
            "https://cdn.discordapp.com/attachments/323666308107599872/752638707026362368/bow.gif",
            "https://cdn.discordapp.com/attachments/323666308107599872/752638709043691580/keanu.gif",
            "https://cdn.discordapp.com/attachments/323666308107599872/752638732288393256/yeah.gif",
            "https://cdn.discordapp.com/attachments/323666308107599872/752638754799353887/eyy.gif",
            "https://cdn.discordapp.com/attachments/323666308107599872/752638757303484526/eyebrows.gif",
            "https://cdn.discordapp.com/attachments/323666308107599872/752638770238586950/kitteneanu.gif",
            "https://cdn.discordapp.com/attachments/323666308107599872/752638772667220049/gay.gif",
            "https://cdn.discordapp.com/attachments/323666308107599872/752638780715958403/imweird_imaweirdo.gif",
            "https://cdn.discordapp.com/attachments/323666308107599872/752638791629668493/keanu_by_firelight.gif",
            "https://cdn.discordapp.com/attachments/323666308107599872/752638807534469141/keanu_smooch.gif",
            "https://cdn.discordapp.com/attachments/323666308107599872/752638810201915392/keanu_stark.gif",
            "https://cdn.discordapp.com/attachments/323666308107599872/752638826379346031/breathtaking.gif",
            "https://cdn.discordapp.com/attachments/323666308107599872/752638835518734446/breath_taking.gif",
            "https://cdn.discordapp.com/attachments/323666308107599872/752638836797997106/winkwonk.gif",
            "https://cdn.discordapp.com/attachments/323666308107599872/752638849771110410/laugh.gif",
            "https://cdn.discordapp.com/attachments/323666308107599872/752638864857890816/keanushark.gif",
            "https://cdn.discordapp.com/attachments/323666308107599872/752638874966163526/keanu_marshmallow.gif",
            "https://cdn.discordapp.com/attachments/323666308107599872/752638879500206171/keanu_confetti.gif",
            "https://cdn.discordapp.com/attachments/741030569307275436/753991114301898762/image0.png"
    ));

    private final boolean isTestEnvironment;
    private final BaseConfig baseConfig;
    private final KeanuResponsesConfigRepository configRepository;
    private final CloseableHttpClient gfycatClient;
    private final ObjectMapper objectMapper;

    @Resource(name = "keanuJda")
    @Lazy
    private JDA keanuJda;

    @Autowired
    public KeanuCommand(ObjectMapper objectMapper, BaseConfig baseConfig, KeanuResponsesConfigRepository keanuResponsesConfigRepository,
                        CloseableHttpClient gfycatClient)
            throws Exception {
        this.baseConfig = baseConfig;
        isTestEnvironment = "test".equalsIgnoreCase(baseConfig.nodeEnvironment);
        name = "keanu";
        help = "Any message containing `keanu` will make keanu respond with a random message if that channel is configured.\n" +
                "\t`!!keanu tts` responds with a random message but spoken via text-to-speech\n" +
                "\t`!!keanu nsfw add channel #channel` adds a channel to the list of channels nsfw keanu responds to\n" +
                "\t`!!keanu nsfw remove channel #channel` removes a channel from the list of channels nsfw keanu responds to\n" +
                "\t`!!keanu sfw add channel #channel` adds a channel to the list of channels normal keanu responds to\n" +
                "\t`!!keanu sfw remove channel #channel` removes a channel from the list of channels normal keanu responds to\n" +
                "\t`!!keanu nsfw add something something` adds 'something something' to the list of responses keanu has for nsfw channels\n" +
                "\t`!!keanu nsfw remove something something` removes 'something something' from the list of responses keanu has for nsfw channels\n" +
                "\t`!!keanu sfw add something something` adds 'something something' to the list of responses keanu has for normal channels\n" +
                "\t`!!keanu sfw remove something something` removes 'something something' from the list of responses keanu has for normal channels\n" +
                "\t`!!keanu nsfw config` responds with a json file of the nsfw config\n" +
                "\t`!!keanu sfw config` responds with a json file of the normal config\n";
        requiredRole = "Queens of the Castle";
        this.configRepository = keanuResponsesConfigRepository;
        this.objectMapper = objectMapper;
        this.gfycatClient = gfycatClient;

        // seed data if needed
        if (!configRepository.findById(BaseResponsesConfig.SINGLETON_ID).isPresent()) {
            GenericBotResponsesConfigJson kidFriendlyConfig = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("keanu-kidFriendlyConfig.json"),
                    GenericBotResponsesConfigJson.class);
            GenericBotResponsesConfigJson nsfwConfig = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("keanu-nsfwConfig.json"),
                    GenericBotResponsesConfigJson.class);
            nsfwConfig.getResponses().addAll(kidFriendlyConfig.getResponses());

            KeanuResponsesConfig responsesConfig = new KeanuResponsesConfig();
            responsesConfig.setId(BaseResponsesConfig.SINGLETON_ID);
            responsesConfig.setSfwConfig(kidFriendlyConfig);
            responsesConfig.setNsfwConfig(nsfwConfig);
            configRepository.save(responsesConfig);
        }
    }

    @Scheduled(cron = "0 0 17 * * ?", zone = "GMT")
    public void goodMorning() {
        TextChannel general = keanuJda.getTextChannelsByName("general", false).get(0);
        general.sendMessage(pickRandom(GOOD_MORNING_GIFS)).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        Optional<KeanuResponsesConfig> optionalConfig = configRepository.findById(BaseResponsesConfig.SINGLETON_ID);
        if (!optionalConfig.isPresent()) {
            event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", failed to read keanu entries from database :(").queue();
            return;
        }

        KeanuResponsesConfig responsesConfig = optionalConfig.get();
        String msgContent = event.getMessage().getContentRaw();

        if (msgContent.startsWith("!!keanu")) {
            if (msgContent.startsWith("!!keanu nsfw add channel")) {
                List<TextChannel> mentionedChannels = event.getMessage().getMentionedChannels();
                if (CollectionUtils.isEmpty(mentionedChannels)) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a channel. `!!keanu nsfw add channel #cursed`").queue();
                    return;
                }

                if (mentionedChannels.size() > 1) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please only specify one channel. `!!keanu nsfw add channel #cursed`")
                            .queue();
                    return;
                }

                responsesConfig.getNsfwConfig().getChannels().add(mentionedChannels.get(0).getName());
                configRepository.save(responsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", saved " + mentionedChannels.get(0).getAsMention() + " to nsfw config")
                        .queue();
            } else if (msgContent.startsWith("!!keanu nsfw remove channel")) {
                List<TextChannel> mentionedChannels = event.getMessage().getMentionedChannels();
                if (CollectionUtils.isEmpty(mentionedChannels)) {
                    String channelName = msgContent.replace("!!keanu nsfw remove channel", "").trim();

                    if (StringUtils.isEmpty(channelName)) {
                        event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a channel. `!!keanu nsfw remove channel #general`")
                                .queue();
                        return;
                    }

                    responsesConfig.getNsfwConfig().getChannels().remove(channelName);
                    configRepository.save(responsesConfig);
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", removed " + channelName + " from nsfw config").queue();
                    return;
                }

                if (mentionedChannels.size() > 1) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please only specify one channel. `!!keanu nsfw remove channel #cursed`")
                            .queue();
                    return;
                }

                responsesConfig.getNsfwConfig().getChannels().remove(mentionedChannels.get(0).getName());
                configRepository.save(responsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", removed " + mentionedChannels.get(0).getAsMention() + " from nsfw config")
                        .queue();
            } else if (msgContent.startsWith("!!keanu sfw add channel")) {
                List<TextChannel> mentionedChannels = event.getMessage().getMentionedChannels();
                if (CollectionUtils.isEmpty(mentionedChannels)) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a channel. `!!keanu sfw add channel #general`").queue();
                    return;
                }

                if (mentionedChannels.size() > 1) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please only specify one channel. `!!keanu sfw add channel #general`")
                            .queue();
                    return;
                }

                responsesConfig.getSfwConfig().getChannels().add(mentionedChannels.get(0).getName());
                configRepository.save(responsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", saved " + mentionedChannels.get(0).getAsMention() + " to sfw config")
                        .queue();
            } else if (msgContent.startsWith("!!keanu sfw remove channel")) {
                List<TextChannel> mentionedChannels = event.getMessage().getMentionedChannels();
                if (CollectionUtils.isEmpty(mentionedChannels)) {
                    String channelName = msgContent.replace("!!keanu sfw remove channel", "").trim();

                    if (StringUtils.isEmpty(channelName)) {
                        event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a channel. `!!keanu sfw remove channel #general`")
                                .queue();
                        return;
                    }

                    responsesConfig.getSfwConfig().getChannels().remove(channelName);
                    configRepository.save(responsesConfig);
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", removed " + channelName + " from sfw config").queue();
                    return;
                }

                if (mentionedChannels.size() > 1) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please only specify one channel. `!!keanu sfw remove channel #general`")
                            .queue();
                    return;
                }

                responsesConfig.getSfwConfig().getChannels().remove(mentionedChannels.get(0).getName());
                configRepository.save(responsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", removed " + mentionedChannels.get(0).getAsMention() + " from sfw config")
                        .queue();
            } else if (msgContent.startsWith("!!keanu nsfw add")) {
                String saying = msgContent.replace("!!keanu nsfw add", "").trim();
                if (saying.isEmpty()) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a saying to add").queue();
                    return;
                }
                responsesConfig.getNsfwConfig().getResponses().add(saying);
                configRepository.save(responsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", saved `" + saying + "` to nsfw config").queue();
            } else if (msgContent.startsWith("!!keanu nsfw remove")) {
                String saying = msgContent.replace("!!keanu nsfw remove", "").trim();
                if (saying.isEmpty()) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a saying to remove").queue();
                    return;
                }
                responsesConfig.getNsfwConfig().getResponses().remove(saying);
                configRepository.save(responsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", removed `" + saying + "` from nsfw config").queue();
            } else if (msgContent.startsWith("!!keanu sfw add")) {
                String saying = msgContent.replace("!!keanu sfw add", "").trim();
                if (saying.isEmpty()) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a saying to add").queue();
                    return;
                }
                responsesConfig.getSfwConfig().getResponses().add(saying);
                responsesConfig.getNsfwConfig().getResponses().add(saying);
                configRepository.save(responsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", saved `" + saying + "` to sfw config").queue();
            } else if (msgContent.startsWith("!!keanu sfw remove")) {
                String saying = msgContent.replace("!!keanu sfw remove", "").trim();
                if (saying.isEmpty()) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", please specify a saying to remove").queue();
                    return;
                }
                responsesConfig.getSfwConfig().getResponses().remove(saying);
                responsesConfig.getNsfwConfig().getResponses().remove(saying);
                configRepository.save(responsesConfig);
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", removed `" + saying + "` from sfw config").queue();
            } else if (msgContent.startsWith("!!keanu nsfw config")) {
                try {
                    event.getChannel().sendFile(objectMapper.writeValueAsBytes(responsesConfig.getNsfwConfig()), "nsfwconfig.json").queue();
                } catch (JsonProcessingException e) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", fatal error could not send nsfw config to you").queue();
                }
            } else if (msgContent.startsWith("!!keanu sfw config")) {
                try {
                    event.getChannel().sendFile(objectMapper.writeValueAsBytes(responsesConfig.getSfwConfig()), "sfwconfig.json").queue();
                } catch (JsonProcessingException e) {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", fatal error could not send sfw config to you").queue();
                }
            } else if (msgContent.startsWith("!!keanu tts")) {
                getResponse(event, responsesConfig).ifPresent(response -> {
                    Message outboundMessage = new MessageBuilder(event.getAuthor().getAsMention() + ", " + response)
                            .setTTS(true)
                            .build();

                    event.getChannel().sendMessage(outboundMessage).queue();
                });
            } else {
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", unrecognized keanu command").queue();
            }
            return;
        }

        getResponse(event, responsesConfig).ifPresent(response -> {
            event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", " + response).queue();
        });
    }

    private Optional<String> getResponse(CommandEvent event, KeanuResponsesConfig responsesConfig) {
        String msgContent = event.getMessage().getContentRaw();
        String channelName = event.getChannel().getName();
        Set<String> responses;
        if (isTestEnvironment) {
            if ("test-channel".equalsIgnoreCase(channelName)) {
                Optional<String> maybeGif = maybeGetGif();
                if (maybeGif.isPresent()) {
                    event.getChannel().sendMessage(maybeGif.get()).queue();
                    return Optional.empty();
                }

                responses = responsesConfig.getNsfwConfig().getResponses();
            } else {
                return Optional.empty();
            }
        } else {
            if (responsesConfig.getSfwConfig().getChannels().contains(channelName)) {
                if (ThreadLocalRandom.current().nextInt(100) < 28) {
                    responses = GOOD_MORNING_GIFS;
                } else {
                    responses = responsesConfig.getSfwConfig().getResponses();
                }
            } else if (responsesConfig.getNsfwConfig().getChannels().contains(channelName)) {
                Optional<String> maybeGif = maybeGetGif();
                if (maybeGif.isPresent()) {
                    event.getChannel().sendMessage(maybeGif.get()).queue();
                    return Optional.empty();
                }

                responses = responsesConfig.getNsfwConfig().getResponses();
            } else {
                return Optional.empty();
            }
        }

        if (StringUtils.containsIgnoreCase(msgContent, "hello")) {
            event.getChannel().sendMessage(HELLO_GIF).queue();
            return Optional.empty();
        }

        if (PUPPY_PATTERN.matcher(msgContent).find()) {
            event.getChannel().sendMessage(PUPPIES_GIF).queue();
            return Optional.empty();
        }

        if (EmojiManager.containsEmoji(msgContent)) {
            Collection<Emoji> allEmoji = EmojiManager.getAll();
            Emoji emoji = pickRandom(allEmoji);
            return Optional.of(emoji.getUnicode());
        }

        return Optional.of(pickRandom(responses));
    }

    private Optional<String> maybeGetGif() {
        if (StringUtils.isNotBlank(baseConfig.gfycatClientId) && ThreadLocalRandom.current().nextInt(100) < 22) {
            try {
                HttpPost credentialsRequest = new HttpPost("https://api.gfycat.com/v1/oauth/token");
                credentialsRequest.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());

                Map<String, String> mapBody = new HashMap<>();
                mapBody.put("grant_type", "client_credentials");
                mapBody.put("client_id", baseConfig.gfycatClientId);
                mapBody.put("client_secret", baseConfig.gfycatClientSecret);

                credentialsRequest.setEntity(new StringEntity(objectMapper.writeValueAsString(mapBody)));

                String accessToken = null;
                try (CloseableHttpResponse response = gfycatClient.execute(credentialsRequest)) {
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            JsonNode jsonNode = objectMapper.readTree(EntityUtils.toString(entity));
                            accessToken = jsonNode.get("access_token").asText();
                        } else {
                            logger.error("Failed to fetch a keanu gif, token response body was null");
                        }
                    } else {
                        logger.error("Failed to fetch a keanu gif, got back non 200 status code from gfycat token endpoint");
                    }
                }

                if (accessToken != null) {
                    HttpGet searchRequest = new HttpGet("https://api.gfycat.com/v1/gfycats/search?search_text=keanu+reeves");
                    searchRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
                    searchRequest.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
                    try (CloseableHttpResponse response = gfycatClient.execute(searchRequest)) {
                        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                            HttpEntity entity = response.getEntity();
                            if (entity != null) {
                                JsonNode jsonNode = objectMapper.readTree(EntityUtils.toString(entity));
                                jsonNode.get("gfycats").get(0).get("max2mbGif").asText();
                                JsonNode gfycatsArray = jsonNode.get("gfycats");
                                if (gfycatsArray.isArray()) {
                                    JsonNode singleGfy = gfycatsArray.get(ThreadLocalRandom.current().nextInt(gfycatsArray.size()));
                                    String gifLink = singleGfy.get("max2mbGif").asText();

                                    if (StringUtils.isNotBlank(gifLink)) {
                                        return Optional.of(gifLink);
                                    } else {
                                        logger.error("Failed to extract single gif from gfycat search results");
                                    }
                                } else {
                                    logger.error("gfycat did not return an array of search results");
                                }
                            } else {
                                logger.error("Failed to fetch a keanu gif, search response body was null");
                            }
                        } else {
                            logger.error("Failed to fetch a keanu gif, got back non 200 status code from gfycat search endpoint");
                        }
                    }
                }
            } catch (Exception e) {
                // continue on with a normal response
                logger.error("Failed to fetch a keanu gif", e);
            }
        }

        return Optional.empty();
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
