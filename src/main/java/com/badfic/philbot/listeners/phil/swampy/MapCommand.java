package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.jagrosh.jdautilities.command.CommandEvent;
import jakarta.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.LocalFileHeader;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MapCommand extends BaseSwampy {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String MAP_ZIP_FILENAME = "map-trivia-flags.zip";
    private List<MapTriviaObject> countries;

    public MapCommand() {
        name = "map";
        help = "!!map\nTo trigger a Map trivia question that will last 15 minutes";
        requiredRole = Constants.ADMIN_ROLE;
    }

    @PostConstruct
    public void init() throws Exception {
        countries = ImmutableList.copyOf(objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("map-trivia.json"),
                new TypeReference<List<MapTriviaObject>>() {}));

        if (countries.stream()
                .anyMatch(country -> StringUtils.isBlank(country.code()) || StringUtils.isBlank(country.capital()) || StringUtils.isBlank(country.regex()))) {
            throw new IllegalArgumentException("Map Trivia JSON is malformed");
        }

        Set<String> flagCodes = new HashSet<>();

        try (ZipInputStream zipFile = new ZipInputStream(getClass().getClassLoader().getResourceAsStream(MAP_ZIP_FILENAME))) {
            LocalFileHeader fileHeader;
            while ((fileHeader = zipFile.getNextEntry()) != null) {
                String countryCode = fileHeader.getFileName().substring(0, fileHeader.getFileName().length() - 4);
                flagCodes.add(countryCode);
            }
        }

        Collection<String> disjunction = CollectionUtils.disjunction(flagCodes, countries.stream().map(MapTriviaObject::code).collect(Collectors.toList()));

        if (CollectionUtils.isNotEmpty(disjunction)) {
            throw new IllegalArgumentException("Map Trivia JSON does not match Map Trivia Flags, Disjunction: " + disjunction);
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        doMap();
    }

    @Scheduled(cron = "${swampy.schedule.events.map}", zone = "${swampy.schedule.timezone}")
    public void doMap() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                .get(0);

        if (swampyGamesConfig.getMapPhrase() != null) {
            swampysChannel.sendMessage("There is currently a map trivia running, you can't trigger another.").queue();
            return;
        }

        MapTriviaObject mapTriviaObject = Constants.pickRandom(countries);
        TriviaType triviaType = Constants.pickRandom(TriviaType.values());

        swampyGamesConfig.setMapPhrase(mapTriviaObject.regex());
        swampyGamesConfig.setMapTriviaExpiration(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(15));
        swampyGamesConfigRepository.save(swampyGamesConfig);

        String type = mapTriviaObject.code().startsWith("us-") ? "US State?" : "Country";
        String description = switch (triviaType) {
            case CAPITAL -> mapTriviaObject.capital() + " is the capital of what " + type + "?";
            case FLAG -> "This flag represents what " + type + "?";
        };
        description += "\n\nType your answer here in this channel within the next 15 minutes for " + swampyGamesConfig.getMapEventPoints() + " points.";

        if (triviaType == TriviaType.CAPITAL) {
            swampysChannel.sendMessageEmbeds(Constants.simpleEmbed("Map Trivia", description)).queue();
            return;
        }

        try (ZipInputStream zipFile = new ZipInputStream(getClass().getClassLoader().getResourceAsStream(MAP_ZIP_FILENAME))) {
            LocalFileHeader fileHeader;
            while ((fileHeader = zipFile.getNextEntry()) != null) {
                if (fileHeader.getFileName().equalsIgnoreCase(mapTriviaObject.code() + ".png")) {
                    break;
                }
            }

            if (fileHeader == null) {
                swampysChannel.sendMessage("Failed to load image for map trivia. The answer is " + mapTriviaObject.regex()).queue();
                return;
            }

            swampysChannel.sendMessageEmbeds(Constants.simpleEmbed("Map Trivia", description))
                    .addFiles(FileUpload.fromData(zipFile.readAllBytes(), "map-trivia.png"))
                    .queue();
        } catch (Exception e) {
            logger.error("Failed to load map image", e);
            honeybadgerReporter.reportError(e, null, "Failed to load image for map trivia");
            swampysChannel.sendMessage("Failed to load image for map trivia. The answer is " + mapTriviaObject.regex()).queue();
        }
    }

    private record MapTriviaObject(String code, String regex, String capital) {}
    private enum TriviaType {
        FLAG, CAPITAL
    }
}
