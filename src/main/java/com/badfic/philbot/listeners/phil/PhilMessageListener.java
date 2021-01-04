package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.phil.Quote;
import com.badfic.philbot.data.phil.QuoteRepository;
import com.badfic.philbot.listeners.antonia.AntoniaMessageListener;
import com.badfic.philbot.listeners.behrad.BehradMessageListener;
import com.badfic.philbot.listeners.john.JohnMessageListener;
import com.badfic.philbot.listeners.keanu.KeanuMessageListener;
import com.badfic.philbot.listeners.phil.swampy.MemberCount;
import com.badfic.philbot.listeners.phil.swampy.SwampyCommand;
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
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class PhilMessageListener extends ListenerAdapter implements PhilMarker {

    private static final Cache<String, Function<GuildMessageReactionAddEvent, Boolean>> OUTSTANDING_REACTION_TASKS = CacheBuilder.newBuilder()
            .maximumSize(200)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();
    private static final Pattern PHIL_PATTERN = Constants.compileWords("phil|klemmer|phellen|cw|willip|schlemmer|pharole|klaskin|phreddie|klercury|philliam");
    private static final Pattern AO3_PATTERN = Pattern.compile("^(?:http(s)?://)?(archiveofourown\\.org/works/)([0-9]+).*$", Pattern.CASE_INSENSITIVE);
    private static final Multimap<String, Pair<Pattern, String>> USER_TRIGGER_WORDS = ImmutableMultimap.<String, Pair<Pattern, String>>builder()
            .put("594740276568784906", ImmutablePair.of(Constants.compileWords("hubby"), "Hi boo"))
            .put("323520695550083074", ImmutablePair.of(Constants.compileWords("child"), "Yes father?"))
            .build();

    @Resource
    @Lazy
    private BehradMessageListener behradMessageListener;

    @Resource
    @Lazy
    private KeanuMessageListener keanuMessageListener;

    @Resource
    @Lazy
    private AntoniaMessageListener antoniaMessageListener;

    @Resource
    @Lazy
    private JohnMessageListener johnMessageListener;

    @Resource(name = "behradJda")
    @Lazy
    private JDA behradJda;

    @Resource(name = "keanuJda")
    @Lazy
    private JDA keanuJda;

    @Resource(name = "antoniaJda")
    @Lazy
    private JDA antoniaJda;

    @Resource(name = "johnJda")
    @Lazy
    private JDA johnJda;

    @Resource
    private QuoteRepository quoteRepository;

    @Resource
    private PhilCommand philCommand;

    @Resource
    private SwampyCommand swampyCommand;

    @Resource
    @Lazy
    private Ao3MetadataParser ao3MetadataParser;

    @Resource
    @Lazy
    private MemberCount memberCount;

    @Resource(name = "philCommandClient")
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

        if (msgContent.startsWith("!!") || event.getAuthor().isBot()) {
            return;
        }

        Constants.checkUserTriggerWords(event, USER_TRIGGER_WORDS);

        if (AO3_PATTERN.matcher(msgContent).find()) {
            ao3MetadataParser.parseLink(msgContent, event.getChannel().getName());
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

                    String image = null;
                    if (CollectionUtils.isNotEmpty(msg.getEmbeds())) {
                        image = msg.getEmbeds().get(0).getUrl();
                    }
                    if (CollectionUtils.isNotEmpty(msg.getAttachments())) {
                        image = msg.getAttachments().get(0).getUrl();
                    }

                    Quote savedQuote = quoteRepository.save(new Quote(msgId, channelId, msg.getContentRaw(), image,
                            msg.getAuthor().getIdLong(), msg.getTimeCreated().toLocalDateTime()));

                    msg.addReaction("\uD83D\uDCAC").queue();

                    String msgLink = " [(jump)](https://discordapp.com/channels/" + event.getGuild().getIdLong() + '/' + channelId + '/' + msgId + ')';

                    MessageEmbed messageEmbed = Constants.simpleEmbed("Quote #" + savedQuote.getId() + " Added",
                            "<@!" + event.getUserId() + "> Added quote #" + savedQuote.getId() + msgLink);

                    johnJda.getTextChannelById(event.getChannel().getIdLong()).sendMessage(messageEmbed).queue();
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
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        memberCount.updateCount();
    }

    @Override
    public void onGuildBan(@NotNull GuildBanEvent event) {
        begone(event.getUser(), event);
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        begone(event.getUser(), event);
    }

    private void begone(User user, GenericGuildEvent event) {
        swampyCommand.removeFromGames(user.getId());
        Optional<TextChannel> announcementsChannel = event.getGuild().getTextChannelsByName("event-reminders", false).stream().findFirst();
        announcementsChannel.ifPresent(channel -> channel.sendMessage("Begone Bot " + user.getAsMention()).queue());
        memberCount.updateCount();
    }

}
