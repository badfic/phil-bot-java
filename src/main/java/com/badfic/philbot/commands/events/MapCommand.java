package com.badfic.philbot.commands.events;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.BaseNormalCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.service.OnJdaReady;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.LocalFileHeader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MapCommand extends BaseNormalCommand implements OnJdaReady {
    // VisibleForTesting
    static final String MAP_ZIP_FILENAME = "map-trivia-flags.zip";

    public MapCommand() {
        name = "map";
        help = "!!map\nTo trigger a Map trivia question that will last 15 minutes";
        requiredRole = Constants.ADMIN_ROLE;
    }

    @Override
    public void run() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        if (Objects.nonNull(swampyGamesConfig.getMapPhrase()) && Objects.nonNull(swampyGamesConfig.getMapTriviaExpiration())) {
            scheduleTask(this::mapComplete, swampyGamesConfig.getMapTriviaExpiration());
        }
    }

    @Override
    public void execute(CommandEvent event) {
        doMap();
    }

    @Scheduled(cron = "${swampy.schedule.events.map}", zone = "${swampy.schedule.timezone}")
    public void doMap() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);

        if (swampyGamesConfig.getMapPhrase() != null) {
            swampysChannel.sendMessage("There is currently a map trivia running, you can't trigger another.").queue();
            return;
        }

        MapTriviaObject mapTriviaObject = Constants.pickRandom(getCountries());
        TriviaType triviaType = Constants.pickRandom(TriviaType.values());

        swampyGamesConfig.setMapPhrase(mapTriviaObject.regex());
        LocalDateTime mapTriviaExpiration = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(15);
        swampyGamesConfig.setMapTriviaExpiration(mapTriviaExpiration);
        swampyGamesConfig = saveSwampyGamesConfig(swampyGamesConfig);

        scheduleTask(this::mapComplete, mapTriviaExpiration);

        String type = mapTriviaObject.code().startsWith("us-") ? "US State" : "Country";
        String description = switch (triviaType) {
            case CAPITAL -> mapTriviaObject.capital() + " is the capital of what " + type + "?";
            case FLAG -> "This flag represents what " + type + "?";
        };
        description += "\n\nType your answer here in this channel within the next 15 minutes for " + swampyGamesConfig.getMapEventPoints() + " points.";

        if (triviaType == TriviaType.CAPITAL) {
            swampysChannel.sendMessageEmbeds(Constants.simpleEmbed("Map Trivia", description)).queue();
            return;
        }

        try (InputStream mapFileStream = getClass().getClassLoader().getResourceAsStream(MAP_ZIP_FILENAME);
             ZipInputStream zipFile = new ZipInputStream(mapFileStream)) {
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
            log.error("Failed to load map image for map trivia", e);
            swampysChannel.sendMessage("Failed to load image for map trivia. The answer is " + mapTriviaObject.regex()).queue();
        }
    }

    private void mapComplete() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);

        if (Objects.isNull(swampyGamesConfig.getMapPhrase())) {
            swampysChannel.sendMessage("Map Trivia failed.").queue();
            return;
        }

        LocalDateTime mapExpiration = swampyGamesConfig.getMapTriviaExpiration();

        swampyGamesConfig.setMapPhrase(null);
        swampyGamesConfig.setMapTriviaExpiration(null);
        saveSwampyGamesConfig(swampyGamesConfig);

        Guild guild = philJda.getGuildById(baseConfig.guildId);

        List<CompletableFuture<?>> futures = new ArrayList<>();
        LocalDateTime startTime = mapExpiration.minusMinutes(15);
        StringBuilder description = new StringBuilder();
        discordUserRepository.findAll()
                .stream()
                .filter(u -> Objects.nonNull(u.getAcceptedMapTrivia()) && (u.getAcceptedMapTrivia().isEqual(startTime) || u.getAcceptedMapTrivia().isAfter(startTime)))
                .forEach(u -> {
                    try {
                        Member memberLookedUp = guild.getMemberById(u.getId());
                        if (memberLookedUp == null) {
                            throw new RuntimeException("member not found");
                        }

                        futures.add(givePointsToMember(swampyGamesConfig.getMapEventPoints(), memberLookedUp, PointsStat.MAP));
                        description.append("Gave ")
                                .append(swampyGamesConfig.getMapEventPoints())
                                .append(" points to <@")
                                .append(u.getId())
                                .append(">\n");
                    } catch (Exception e) {
                        log.error("Failed to give map trivia points to user [id={}]", u.getId(), e);
                        description.append("OOPS: Unable to give points to <@")
                                .append(u.getId())
                                .append(">\n");
                    }
                });

        MessageEmbed messageEmbed = Constants.simpleEmbed("Map Trivia Complete", description.toString());

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenRun(() -> swampysChannel.sendMessageEmbeds(messageEmbed).queue());
    }

    // VisibleForTesting
    @SneakyThrows
    MapTriviaObject[] getCountries() {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("map-trivia.json")) {
            return objectMapper.readValue(stream, MapTriviaObject[].class);
        }
    }

    // VisibleForTesting
    record MapTriviaObject(String code, String regex, String capital) {}

    private enum TriviaType {
        FLAG, CAPITAL
    }
}
