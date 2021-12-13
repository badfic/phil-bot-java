package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.MemeCommandEntity;
import com.badfic.philbot.data.phil.MemeCommandRepository;
import com.badfic.philbot.service.BaseService;
import com.badfic.philbot.web.members.MemeForm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class MemeCommandsService extends BaseService {

    private static final TypeReference<Set<String>> STRING_LIST = new TypeReference<>() {};

    @Resource
    private MemeCommandRepository memeCommandRepository;

    @Resource(name = "behradJda")
    @Lazy
    protected JDA behradJda;

    public List<MemeCommandEntity> findAll() {
        List<MemeCommandEntity> memes = memeCommandRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        memes.forEach(meme -> {
            meme.setUrlIsImage(Constants.urlIsImage(meme.getUrl()));
            meme.setUrlIsList(StringUtils.startsWith(meme.getUrl(), "[") && StringUtils.endsWith(meme.getUrl(), "]"));
        });
        return memes;
    }

    public void saveMeme(MemeForm form) {
        String memeUrl = form.getMemeUrl();
        String memeName = form.getMemeName();

        if (Stream.of(memeName, memeUrl).anyMatch(StringUtils::isBlank)) {
            throw new IllegalArgumentException("Please provide a valid name and url");
        }

        String lowercaseName = StringUtils.lowerCase(memeName);

        if (!StringUtils.isAlphanumeric(lowercaseName)) {
            throw new IllegalArgumentException("A meme name can only be alphanumeric. No symbols.");
        }

        if (StringUtils.startsWith(memeUrl, "[") && StringUtils.endsWith(memeUrl, "]")) {
            try {
                Set<String> urlList = objectMapper.readValue(memeUrl, STRING_LIST);

                if (CollectionUtils.isEmpty(urlList)) {
                    throw new IllegalArgumentException(String.format("\"%s\" meme command value starts and ends with brackets but is empty", memeName));
                }
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException(String.format("\"%s\" meme command value starts and ends with brackets but is not a list", memeName));
            }
        } else {
            try {
                URL u = new URL(memeUrl);
                Objects.requireNonNull(u.toURI());
            } catch (Exception e) {
                throw new IllegalArgumentException("A meme url must be a valid url.");
            }
        }

        if (memeCommandRepository.findById(lowercaseName).isPresent()) {
            throw new IllegalArgumentException("A meme by that name already exists");
        }

        MemeCommandEntity meme = new MemeCommandEntity();
        meme.setName(lowercaseName);
        meme.setUrl(memeUrl);

        memeCommandRepository.save(meme);
    }

    public void deleteMeme(String memeName) {
        if (StringUtils.isBlank(memeName)) {
            throw new IllegalArgumentException("memeName provided was blank");
        }

        if (!StringUtils.isAlphanumeric(memeName)) {
            throw new IllegalArgumentException("memeName provided was not alphanumeric");
        }

        if (memeCommandRepository.findById(memeName).isPresent()) {
            memeCommandRepository.deleteById(memeName);
        } else {
            throw new IllegalArgumentException("memeName provided does not exist");
        }
    }

    public void executeCustomCommand(String commandName, TextChannel textChannel) {
        if (!StringUtils.isAlphanumeric(commandName)) {
            return;
        }

        Optional<MemeCommandEntity> result = memeCommandRepository.findById(StringUtils.lowerCase(commandName));

        if (result.isPresent()) {
            MemeCommandEntity memeCommand = result.get();
            String url = memeCommand.getUrl();

            if (StringUtils.startsWith(url, "[") && StringUtils.endsWith(url, "]")) {
                try {
                    Set<String> urlList = objectMapper.readValue(url, STRING_LIST);
                    url = Constants.pickRandom(urlList);
                } catch (JsonProcessingException e) {
                    honeybadgerReporter.reportError(e, null,
                            String.format("\"%s\" meme command value starts and ends with brackets but is not a list", commandName));
                }
            }

            behradJda.getTextChannelById(textChannel.getIdLong())
                    .sendMessage(url)
                    .queue();
        }
    }

}
