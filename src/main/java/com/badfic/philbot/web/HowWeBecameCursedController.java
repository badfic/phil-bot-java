package com.badfic.philbot.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HowWeBecameCursedController extends BaseController {

    @CrossOrigin
    @GetMapping(value = "/how-we-became-cursed", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> get() {
        Optional<TextChannel> optionalChannel = philJda.getGuilds().get(0).getTextChannelsByName("how-we-became-cursed", false).stream().findFirst();

        if (!optionalChannel.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        TextChannel channel = optionalChannel.get();

        List<String> messages = new ArrayList<>();
        long lastMsgId = 0;
        while (lastMsgId != -1) {
            MessageHistory history;
            if (lastMsgId == 0) {
                history = channel.getHistoryFromBeginning(25).complete();
            } else {
                history = channel.getHistoryAfter(lastMsgId, 25).complete();
            }

            lastMsgId = CollectionUtils.isNotEmpty(history.getRetrievedHistory())
                    ? history.getRetrievedHistory().get(0).getIdLong()
                    : -1;

            for (Message message : history.getRetrievedHistory()) {
                String content = message.getContentDisplay();
                if (CollectionUtils.isNotEmpty(message.getAttachments())) {
                    for (Message.Attachment attachment : message.getAttachments()) {
                        if (attachment.getUrl() != null) {
                            content += '\n' + attachment.getUrl();
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(message.getEmbeds())) {
                    for (MessageEmbed embed : message.getEmbeds()) {
                        if (embed.getUrl() != null) {
                            content += '\n' + embed.getUrl();
                        }
                    }
                }
                messages.add(content);
            }
        }

        return ResponseEntity.ok(messages);
    }

}
