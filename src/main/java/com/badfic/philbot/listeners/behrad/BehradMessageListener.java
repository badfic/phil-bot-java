package com.badfic.philbot.listeners.behrad;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.BehradMarker;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class BehradMessageListener extends ListenerAdapter implements BehradMarker {

    private static final Pattern WEED_PATTERN = Pattern.compile("\\b(marijuana|weed|420|stoned|high|stoner|kush)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern BEHRAD_PATTERN = Pattern.compile("\\b(behrad)\\b", Pattern.CASE_INSENSITIVE);
    private static final String[] SHAYAN_IMGS = {
            "https://cdn.discordapp.com/attachments/323666308107599872/750575009650573332/unknown-15.png",
            "https://cdn.discordapp.com/attachments/323666308107599872/750575009885454356/unknown-21.png",
            "https://cdn.discordapp.com/attachments/323666308107599872/750575010221129889/unknown-17.png",
            "https://cdn.discordapp.com/attachments/323666308107599872/750575275783487598/MV5BMGEyZDE2YmYtNjRhNi00MzQwLThjNjItM2E5YjVjOTI3MDMwXkEyXkFqcGdeQXVyMTAzMjM0MjE0.png",
            "https://cdn.discordapp.com/attachments/323666308107599872/750575276026626129/MV5BYTRjOGE2OWUtMjk2MS00MGFkLTg2YjEtYmNjZDRjODAzNWI4XkEyXkFqcGdeQXVyMTAzMjM0MjE0.png"
    };

    private final boolean isTestEnvironment;
    private final BehradCommand behradCommand;
    private final CommandClient behradCommandClient;

    @Autowired
    public BehradMessageListener(BaseConfig baseConfig,
                                 BehradCommand behradCommand,
                                 @Qualifier("behradCommandClient") CommandClient behradCommandClient) {
        isTestEnvironment = "test".equalsIgnoreCase(baseConfig.nodeEnvironment);
        this.behradCommand = behradCommand;
        this.behradCommandClient = behradCommandClient;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String msgContent = event.getMessage().getContentRaw();

        if (msgContent.startsWith("!!") || event.getAuthor().isBot()) {
            return;
        }

        if (BEHRAD_PATTERN.matcher(msgContent).find()) {
            behradCommand.execute(new CommandEvent(event, null, behradCommandClient));
            return;
        }

        if (isTestEnvironment && !"test-channel".equalsIgnoreCase(event.getChannel().getName())) {
            return;
        }

        if (WEED_PATTERN.matcher(msgContent).find()) {
            MessageEmbed messageEmbed = new EmbedBuilder()
                    .setImage("https://cdn.discordapp.com/attachments/323666308107599872/750575541266022410/cfff6b4479a51d245d26cd82e16d4f3f.png")
                    .build();
            Message message = new MessageBuilder(messageEmbed)
                    .setContent("420 whatcha smokin?")
                    .build();
            event.getChannel().sendMessage(message).queue();
            return;
        }

        if (StringUtils.containsIgnoreCase(msgContent, "i'm gay")) {
            event.getChannel().sendMessage("same").queue();
            return;
        }

        if (StringUtils.containsIgnoreCase(msgContent, "shayan") || StringUtils.containsIgnoreCase(msgContent, "sobhian")) {
            event.getChannel().sendMessage(SHAYAN_IMGS[ThreadLocalRandom.current().nextInt(SHAYAN_IMGS.length)]).queue();
            return;
        }
    }

}
