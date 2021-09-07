package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.data.phil.MemeCommandEntity;
import com.badfic.philbot.data.phil.MemeCommandRepository;
import com.badfic.philbot.service.BaseService;
import java.util.Optional;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class MemeCommands extends BaseService {

    @Resource
    private MemeCommandRepository memeCommandRepository;

    @Resource(name = "behradJda")
    @Lazy
    protected JDA behradJda;

    public void executeCustomCommand(String commandName, TextChannel textChannel) {
        if (!StringUtils.isAlphanumeric(commandName)) {
            return;
        }

        Optional<MemeCommandEntity> result = memeCommandRepository.findById(StringUtils.lowerCase(commandName));

        if (result.isPresent()) {
            behradJda.getTextChannelById(textChannel.getIdLong()).sendMessage(result.get().getUrl()).queue();
        }
    }

}
