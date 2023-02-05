package com.badfic.philbot.listeners.antonia;

import com.badfic.philbot.config.Constants;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.awt.Color;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AntoniaMessageListener {

    public static final List<String> VALENTINES_WORDS = ImmutableList.of(
            "love", "romance", "valentine", "valentines", "affection", "adore", "admire", "caress");

    private static final Pattern ANTONIA_PATTERN = Constants.compileWords("antonia|tony|stark|tash|iron man|tin can");
    private static final Multimap<String, Pair<Pattern, String>> USER_TRIGGER_WORDS = ImmutableMultimap.<String, Pair<Pattern, String>>builder()
            .put("307611036134146080", ImmutablePair.of(Constants.compileWords("I love you"), "I know"))
            .put("323520695550083074", ImmutablePair.of(Constants.compileWords("togna"), "bologna"))
            .put("323520695550083074", ImmutablePair.of(Constants.compileWords("child"), "Yes father?"))
            .build();
    private static final ConcurrentMap<Long, Pair<String, Long>> LAST_WORD_MAP = new ConcurrentHashMap<>();

    private final AntoniaCommand antoniaCommand;
    private final GrinchCommand grinchCommand;

    @Autowired
    public AntoniaMessageListener(AntoniaCommand antoniaCommand, GrinchCommand grinchCommand) {
        this.antoniaCommand = antoniaCommand;
        this.grinchCommand = grinchCommand;
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
                if (oldValue.getRight() + 1 >= 3 && "bird".equalsIgnoreCase(msgContent.trim())) {
                    antoniaCommand.getAntoniaJda().getTextChannelById(channelId).sendMessage("the bird is the word").queue();
                    return new ImmutablePair<>(msgContent, 0L);
                }
                if (oldValue.getRight() + 1 >= 3 && "word".equalsIgnoreCase(msgContent.trim())) {
                    antoniaCommand.getAntoniaJda().getTextChannelById(channelId).sendMessage("the word is the bird").queue();
                    return new ImmutablePair<>(msgContent, 0L);
                }
                if (oldValue.getRight() + 1 >= 3 && "mattgrinch".equalsIgnoreCase(msgContent.trim())) {
                    antoniaCommand.getAntoniaJda()
                            .getTextChannelById(channelId)
                            .sendMessage("https://cdn.discordapp.com/attachments/707453916882665552/914409167610056734/unknown.png")
                            .queue();
                    return new ImmutablePair<>(msgContent, 0L);
                }

                return new ImmutablePair<>(msgContent, oldValue.getRight() + 1);
            } else {
                return new ImmutablePair<>(msgContent, 1L);
            }
        });

        if (StringUtils.containsIgnoreCase(msgContent, "kite man")) {
            antoniaCommand.getAntoniaJda().getTextChannelById(channelId)
                    .sendMessage("Hell yea").queue();
            return;
        }
        if (StringUtils.containsIgnoreCase(msgContent, "owen wilson")) {
            antoniaCommand.getAntoniaJda().getTextChannelById(channelId)
                    .sendMessage("wow").queue();
            return;
        }

        Constants.checkUserTriggerWords(event, USER_TRIGGER_WORDS);

        for (String word : VALENTINES_WORDS) {
            if (StringUtils.containsIgnoreCase(msgContent, word)) {
                long pointsTaken = grinchCommand.takePoints(event.getMember());

                String description = pointsTaken > 0 ? ("BING BONG I stole your points. -" + pointsTaken) : ("BING BONG I stole your points. Or did I?");

                MessageEmbed messageEmbed = Constants.simpleEmbed("Love Grinch!", description,
                        "https://cdn.discordapp.com/attachments/794506942906761226/1071339276945588264/love_grinch.png",
                        null, Color.RED, event.getAuthor().getEffectiveAvatarUrl());

                antoniaCommand.getAntoniaJda()
                        .getTextChannelById(channelId)
                        .sendMessage(messageEmbed)
                        .queue();
            }
        }

        if (ANTONIA_PATTERN.matcher(msgContent).find()) {
            antoniaCommand.execute(new CommandEvent(event, Constants.PREFIX, null, null));
            return;
        }
    }

}
