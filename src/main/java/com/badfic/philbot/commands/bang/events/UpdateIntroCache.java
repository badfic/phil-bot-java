package com.badfic.philbot.commands.bang.events;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.bang.BaseBangCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.Family;
import com.badfic.philbot.service.DailyTickable;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageHistory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UpdateIntroCache extends BaseBangCommand implements DailyTickable {

    public UpdateIntroCache() {
        name = "updateIntroCache";
        ownerCommand = true;
        help = """
                !!updateIntroCache
                Manually update the user introductions cache\s
                that is used to populate introductions from the #introductions channel onto users' `!!fam`""";
    }

    @Override
    public void execute(final CommandEvent event) {
        executorService.execute(this::runDailyTask);
    }

    @Override
    public void runDailyTask() {
        final var optionalIntroChannel = philJda.getTextChannelsByName("introductions", false).stream().findFirst();

        if (optionalIntroChannel.isEmpty()) {
            log.error("ERROR: UpdateIntroCache could not find the #introductions channel");
            return;
        }

        final var introChannel = optionalIntroChannel.get();

        final var introMessages = new HashMap<String, String>();
        var lastMsgId = 0L;
        while (lastMsgId != -1) {
            final MessageHistory history;
            if (lastMsgId == 0) {
                history = introChannel.getHistoryFromBeginning(100).timeout(30, TimeUnit.SECONDS).complete();
            } else {
                history = introChannel.getHistoryAfter(lastMsgId, 100).timeout(30, TimeUnit.SECONDS).complete();
            }

            lastMsgId = CollectionUtils.isNotEmpty(history.getRetrievedHistory())
                    ? history.getRetrievedHistory().getFirst().getIdLong()
                    : -1;

            for (final var message : history.getRetrievedHistory()) {
                introMessages.put(message.getAuthor().getId(), message.getContentRaw());
            }
        }

        final var users = discordUserRepository.findAll();

        for (final var user : users) {
            final var id = user.getId();

            final var introMsg = introMessages.get(id);

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
