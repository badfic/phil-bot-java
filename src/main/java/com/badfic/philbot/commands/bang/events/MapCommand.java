package com.badfic.philbot.commands.bang.events;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.bang.BaseBangCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.service.OnJdaReady;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.FileUpload;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MapCommand extends BaseBangCommand implements OnJdaReady {
    // VisibleForTesting
    static final String MAP_ZIP_FILENAME = "map-trivia-flags.zip";

    public MapCommand() {
        name = "map";
        help = "!!map\nTo trigger a Map trivia question that will last 15 minutes";
        requiredRole = Constants.ADMIN_ROLE;
    }

    @Override
    public void run() {
        final var swampyGamesConfig = getSwampyGamesConfig();

        if (Objects.nonNull(swampyGamesConfig.getMapPhrase()) && Objects.nonNull(swampyGamesConfig.getMapTriviaExpiration())) {
            scheduleTask(this::mapComplete, swampyGamesConfig.getMapTriviaExpiration());
        }
    }

    @Override
    public void execute(final CommandEvent event) {
        doMap();
    }

    @Scheduled(cron = "${swampy.schedule.events.map}", zone = "${swampy.schedule.timezone}")
    private void onSchedule() {
        Thread.startVirtualThread(this::doMap);
    }

    private void doMap() {
        var swampyGamesConfig = getSwampyGamesConfig();

        final var swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).getFirst();

        if (swampyGamesConfig.getMapPhrase() != null) {
            swampysChannel.sendMessage("There is currently a map trivia running, you can't trigger another.").queue();
            return;
        }

        final var mapTriviaObject = Constants.pickRandom(getCountries());
        final var triviaType = StringUtils.isBlank(mapTriviaObject.capital()) ? TriviaType.FLAG : Constants.pickRandom(TriviaType.values());

        swampyGamesConfig.setMapPhrase(mapTriviaObject.regex());
        final var mapTriviaExpiration = LocalDateTime.now().plusMinutes(15);
        swampyGamesConfig.setMapTriviaExpiration(mapTriviaExpiration);
        swampyGamesConfig = saveSwampyGamesConfig(swampyGamesConfig);

        scheduleTask(this::mapComplete, mapTriviaExpiration);

        final var type = mapTriviaObject.code().startsWith("us-") ? "US State" : "Country";
        var description = switch (triviaType) {
            case CAPITAL -> mapTriviaObject.capital() + " is the capital of what " + type + "?";
            case FLAG -> "This flag represents what " + type + "?";
        };
        description += "\n\nType your answer here in this channel within the next 15 minutes for " + swampyGamesConfig.getMapEventPoints() + " points.";

        if (triviaType == TriviaType.CAPITAL) {
            swampysChannel.sendMessageEmbeds(Constants.simpleEmbed("Map Trivia", description)).queue();
            return;
        }

        try (final var mapFileStream = getClass().getClassLoader().getResourceAsStream(MAP_ZIP_FILENAME);
             final var zipFile = new ZipInputStream(mapFileStream)) {
            var fileHeader = zipFile.getNextEntry();
            while (fileHeader != null) {
                if (fileHeader.getFileName().equalsIgnoreCase(mapTriviaObject.code() + ".png")) {
                    break;
                }
                fileHeader = zipFile.getNextEntry();
            }

            if (fileHeader == null) {
                swampysChannel.sendMessage("Failed to load image for map trivia. The answer is " + mapTriviaObject.regex()).queue();
                return;
            }

            swampysChannel.sendMessageEmbeds(Constants.simpleEmbed("Map Trivia", description))
                    .addFiles(FileUpload.fromData(zipFile.readAllBytes(), "map-trivia.png"))
                    .queue();
        } catch (final Exception e) {
            log.error("Failed to load map image for map trivia", e);
            swampysChannel.sendMessage("Failed to load image for map trivia. The answer is " + mapTriviaObject.regex()).queue();
        }
    }

    private void mapComplete() {
        final var swampyGamesConfig = getSwampyGamesConfig();

        final var swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).getFirst();

        if (Objects.isNull(swampyGamesConfig.getMapPhrase())) {
            swampysChannel.sendMessage("Map Trivia failed.").queue();
            return;
        }

        final var mapExpiration = swampyGamesConfig.getMapTriviaExpiration();

        swampyGamesConfig.setMapPhrase(null);
        swampyGamesConfig.setMapTriviaExpiration(null);
        saveSwampyGamesConfig(swampyGamesConfig);

        final var guild = philJda.getGuildById(baseConfig.guildId);

        final var futures = new ArrayList<CompletableFuture<?>>();
        final var startTime = mapExpiration.minusMinutes(15);
        final var description = new StringBuilder();
        discordUserRepository.findAll()
                .stream()
                .filter(u -> Objects.nonNull(u.getAcceptedMapTrivia()) && (u.getAcceptedMapTrivia().isEqual(startTime) || u.getAcceptedMapTrivia().isAfter(startTime)))
                .forEach(u -> {
                    try {
                        final var memberLookedUp = guild.getMemberById(u.getId());
                        if (memberLookedUp == null) {
                            throw new RuntimeException("member not found");
                        }

                        futures.add(givePointsToMember(swampyGamesConfig.getMapEventPoints(), memberLookedUp, PointsStat.MAP));
                        description.append("Gave ")
                                .append(swampyGamesConfig.getMapEventPoints())
                                .append(" points to <@")
                                .append(u.getId())
                                .append(">\n");
                    } catch (final Exception e) {
                        log.error("Failed to give map trivia points to user [id={}]", u.getId(), e);
                        description.append("OOPS: Unable to give points to <@")
                                .append(u.getId())
                                .append(">\n");
                    }
                });

        final var messageEmbed = Constants.simpleEmbed("Map Trivia Complete", description.toString());

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenRun(() -> swampysChannel.sendMessageEmbeds(messageEmbed).queue());
    }

    // VisibleForTesting
    @SneakyThrows
    MapTriviaObject[] getCountries() {
        try (final var stream = getClass().getClassLoader().getResourceAsStream("map-trivia.json")) {
            return objectMapper.readValue(stream, MapTriviaObject[].class);
        }
    }

    // VisibleForTesting
    record MapTriviaObject(String code, String regex, String capital) {}

    private enum TriviaType {
        FLAG, CAPITAL
    }
}
