package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.DiscordUserRepository;
import com.badfic.philbot.data.Family;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UpdateIntroCache extends Command implements PhilMarker {

    @Resource(name = "philJda")
    @Lazy
    private JDA philJda;

    private final DiscordUserRepository discordUserRepository;

    @Autowired
    public UpdateIntroCache(DiscordUserRepository discordUserRepository) {
        name = "updateIntroCache";
        ownerCommand = true;
        help = "!!updateIntroCache\n" +
                "Manually update the user introductions cache \n" +
                "that is used to populate introductions from the #introductions channel onto users' `!!fam`";
        this.discordUserRepository = discordUserRepository;
    }

    @Override
    protected void execute(CommandEvent event) {
        doUpdateIntroCache();
    }

    @Scheduled(cron = "0 0 1,12 * * ?", zone = "GMT")
    public void doUpdateIntroCache() {
        Optional<TextChannel> optionalIntroChannel = philJda.getGuilds().get(0).getTextChannelsByName("introductions", false).stream().findFirst();

        if (!optionalIntroChannel.isPresent()) {
            debug("ERROR: UpdateIntroCache could not find the #introductions channel");
            return;
        }

        TextChannel introChannel = optionalIntroChannel.get();

        Map<String, String> introMessages = new HashMap<>();
        long lastMsgId = 0;
        while (lastMsgId != -1) {
            MessageHistory history;
            if (lastMsgId == 0) {
                history = introChannel.getHistoryFromBeginning(25).complete();
            } else {
                history = introChannel.getHistoryAfter(lastMsgId, 25).complete();
            }

            lastMsgId = CollectionUtils.isNotEmpty(history.getRetrievedHistory())
                    ? history.getRetrievedHistory().get(0).getIdLong()
                    : -1;

            for (Message message : history.getRetrievedHistory()) {
                introMessages.put(message.getAuthor().getId(), message.getContentRaw());
            }
        }

        List<DiscordUser> users = discordUserRepository.findAll();

        for (DiscordUser user : users) {
            String id = user.getId();

            String introMsg = introMessages.get(id);

            if (introMsg != null) {
                if (user.getFamily() == null) {
                    user.setFamily(new Family());
                }

                user.getFamily().setIntro(introMsg);
                discordUserRepository.save(user);
            }
        }

        debug("Successfully updated member introductions cache");
    }

    private void debug(String msg) {
        philJda.getTextChannelsByName(Constants.TEST_CHANNEL, false).stream().findFirst().ifPresent(channel -> {
            channel.sendMessage(msg).queue();
        });
    }

}
