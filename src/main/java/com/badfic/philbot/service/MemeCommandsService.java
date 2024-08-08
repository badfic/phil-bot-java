package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.MemeCommandEntity;
import com.badfic.philbot.data.MemeCommandRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MemeCommandsService extends BaseService {

    private final MemeCommandRepository memeCommandRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    public List<MemeCommandEntity> findAll() {
        final var memes = memeCommandRepository.findAll();
        memes.forEach(meme -> {
            meme.setUrlIsImage(Constants.urlIsImage(meme.getUrl()));
            meme.setUrlIsList(StringUtils.startsWith(meme.getUrl(), "[") && StringUtils.endsWith(meme.getUrl(), "]"));
        });
        return memes;
    }

    public void saveMeme(final String memeUrl, final String memeName) {
        if (Stream.of(memeName, memeUrl).anyMatch(StringUtils::isBlank)) {
            throw new IllegalArgumentException("Please provide a valid name and url");
        }

        final var lowercaseName = StringUtils.lowerCase(memeName);

        if (!StringUtils.isAlphanumeric(lowercaseName)) {
            throw new IllegalArgumentException("A meme name can only be alphanumeric. No symbols.");
        }

        if (StringUtils.startsWith(memeUrl, "[") && StringUtils.endsWith(memeUrl, "]")) {
            try {
                final var urlList = objectMapper.readValue(memeUrl, String[].class);

                if (ArrayUtils.isEmpty(urlList)) {
                    throw new IllegalArgumentException(String.format("\"%s\" meme command value starts and ends with brackets but is empty", memeName));
                }
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException(String.format("\"%s\" meme command value starts and ends with brackets but is not a list", memeName));
            }
        } else {
            try {
                final var uri = URI.create(memeUrl);
                Objects.requireNonNull(uri.toString());
            } catch (Exception e) {
                throw new IllegalArgumentException("A meme url must be a valid url.");
            }
        }

        if (memeCommandRepository.findById(lowercaseName).isPresent()) {
            throw new IllegalArgumentException("A meme by that name already exists");
        }

        jdbcAggregateTemplate.insert(new MemeCommandEntity(lowercaseName, memeUrl));
    }

    public void deleteMeme(final String memeName) {
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

    public void executeCustomCommand(final String commandName, final GuildMessageChannel textChannel) {
        if (!StringUtils.isAlphanumeric(commandName)) {
            return;
        }

        final var result = memeCommandRepository.findById(StringUtils.lowerCase(commandName));

        if (result.isPresent()) {
            final var memeCommand = result.get();

            var url = memeCommand.getUrl();
            if (StringUtils.startsWith(url, "[") && StringUtils.endsWith(url, "]")) {
                try {
                    final var urlList = objectMapper.readValue(url, String[].class);
                    url = Constants.pickRandom(urlList);
                } catch (JsonProcessingException e) {
                    log.error("{} meme command value starts and ends with brackets but is not a list", commandName, e);
                }
            }

            final var swampyGamesConfig = swampyGamesConfigDal.get();
            discordWebhookSendService.sendMessage(textChannel.getIdLong(), swampyGamesConfig.getBehradNickname(), swampyGamesConfig.getBehradAvatar(), url);
        }
    }

}
