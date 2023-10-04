package com.badfic.philbot.commands.events;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.BaseNormalCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.Family;
import com.badfic.philbot.service.DailyTickable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UpdateIntroCache extends BaseNormalCommand implements DailyTickable {

    public UpdateIntroCache() {
        name = "updateIntroCache";
        ownerCommand = true;
        help = """
                !!updateIntroCache
                Manually update the user introductions cache\s
                that is used to populate introductions from the #introductions channel onto users' `!!fam`""";
    }

    @Override
    public void execute(CommandEvent event) {
        executorService.execute(this::runDailyTask);
    }

    @Override
    public void runDailyTask() {
        Optional<TextChannel> optionalIntroChannel = philJda.getTextChannelsByName("introductions", false).stream().findFirst();

        if (optionalIntroChannel.isEmpty()) {
            log.error("ERROR: UpdateIntroCache could not find the #introductions channel");
            return;
        }

        TextChannel introChannel = optionalIntroChannel.get();

        Map<String, String> introMessages = new HashMap<>();
        long lastMsgId = 0;
        while (lastMsgId != -1) {
            MessageHistory history;
            if (lastMsgId == 0) {
                history = introChannel.getHistoryFromBeginning(100).timeout(30, TimeUnit.SECONDS).complete();
            } else {
                history = introChannel.getHistoryAfter(lastMsgId, 100).timeout(30, TimeUnit.SECONDS).complete();
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

        Constants.debugToTestChannel(philJda, "Successfully updated member introductions cache");
    }

}
