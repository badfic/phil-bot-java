package com.badfic.philbot.commands.slash;

import com.badfic.philbot.config.Constants;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdviceSlashCommand extends BaseSlashCommand {
    public AdviceSlashCommand() {
        name = "advice";
        help = "Ask Phil for advice";
        options =  List.of(new OptionData(OptionType.INTEGER, "id", "id", false, false));
    }

    @Override
    public void execute(final SlashCommandInteractionEvent event) {
        final var interactionHook = event.deferReply().submit();
        final var idOption = event.getOption("id");

        final String endpoint;
        if (Objects.nonNull(idOption)) {
            endpoint = "https://api.adviceslip.com/advice/" + idOption.getAsInt();
        } else {
            endpoint = "https://api.adviceslip.com/advice";
        }

        try {
            final var headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            // The AdviceSlip API is always returning content-type HTML, even if you give it an accept header for JSON
            // So we have to get the result as a string and objectMapper it manually
            final var resultEntity = restTemplate.exchange(endpoint, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            final var resultString = resultEntity.getBody();

            if (Objects.isNull(resultString)) {
                replyToInteractionHook(event, interactionHook, "Could not find advice \uD83D\uDE14");
                return;
            }

            final var result = objectMapper.readValue(resultString, AdviceSlip.class);

            replyToInteractionHook(event, interactionHook, Constants.simpleEmbed("Advice " + result.slip.id, result.slip.advice));
        } catch (final Exception e) {
            log.error("Failed to find advice for [id={}]", idOption, e);
            replyToInteractionHook(event, interactionHook, "Could not find advice \uD83D\uDE14");
        }
    }

    private record AdviceSlip(Slip slip) {}
    private record Slip(int id, String advice) {}
}
