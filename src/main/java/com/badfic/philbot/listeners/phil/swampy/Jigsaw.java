package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.awt.Color;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Jigsaw extends BaseSwampy implements PhilMarker {

    private static final long MAX_POINTS = 51_777;
    private static final String JIGSAW = "https://cdn.discordapp.com/attachments/323666308107599872/767999453969252362/jigsaw.png";
    private static final String MICKEY = "https://cdn.discordapp.com/attachments/323666308107599872/768003851177033758/mickeymouse.png";
    private static final String GAME_OVER = "https://cdn.discordapp.com/attachments/323666308107599872/768005786785546260/game_over.png";
    private static final String BICYCLE = "\uD83D\uDEB2";
    private static final Color JIGSAW_COLOR = new Color(222, 4, 12);
    private static final Color MICKEY_COLOR = new Color(255, 209, 0);

    public Jigsaw() {
        name = "jigsaw";
        requiredRole = Constants.ADMIN_ROLE;
    }

    @Override
    protected void execute(CommandEvent event) {
        doJigsaw();
    }

    @Scheduled(cron = "0 20,35 0,2,4,6,8,10,12,14,16,18,20,22 * * ?", zone = "GMT")
    public void doJigsaw() {
        Optional<SwampyGamesConfig> optionalConfig = swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID);
        if (!optionalConfig.isPresent()) {
            return;
        }
        SwampyGamesConfig swampyGamesConfig = optionalConfig.get();

        if (swampyGamesConfig.getPastVictims() == null) {
            swampyGamesConfig.setPastVictims(new HashSet<>());
        }

        if (swampyGamesConfig.getJigsawAwaiting() != null) {
            final String messageId = swampyGamesConfig.getJigsawMessageId();
            final String victimId = swampyGamesConfig.getJigsawAwaiting();
            swampyGamesConfig.setJigsawAwaiting(null);
            swampyGamesConfig.setJigsawMessageId(null);
            swampyGamesConfigRepository.save(swampyGamesConfig);

            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .retrieveMessageById(messageId)
                    .queue(msg -> {
                        msg.retrieveReactionUsers(BICYCLE).queue(users -> {
                            Set<String> victimSet = new HashSet<>();
                            victimSet.add(victimId);
                            victimSet.addAll(users.stream().map(ISnowflake::getId).collect(Collectors.toList()));
                            victimSet.remove(philJda.getSelfUser().getId());

                            final long pointsPerPerson = MAX_POINTS / victimSet.size();

                            StringBuilder description = new StringBuilder();
                            for (String v : victimSet) {
                                try {
                                    Member memberById = philJda.getGuilds().get(0).getMemberById(v);

                                    if (memberById == null) {
                                        description.append("Oops. Failed to take ")
                                                .append(NumberFormat.getIntegerInstance().format(pointsPerPerson))
                                                .append(" points from <@!")
                                                .append(v)
                                                .append(">\n");
                                    } else {
                                        takePointsFromMember(pointsPerPerson, memberById);
                                        description.append("Took ")
                                                .append(NumberFormat.getIntegerInstance().format(pointsPerPerson))
                                                .append(" points from <@!")
                                                .append(v)
                                                .append(">\n");
                                    }
                                } catch (Exception e) {
                                    description.append("Oops. Failed to take ")
                                            .append(NumberFormat.getIntegerInstance().format(pointsPerPerson))
                                            .append(" points from <@!")
                                            .append(v)
                                            .append(">\n");
                                }
                            }

                            MessageEmbed message = new EmbedBuilder()
                                    .setTitle("Game Over")
                                    .setDescription(description.toString())
                                    .setColor(Constants.HALOWEEN_ORANGE)
                                    .setImage(GAME_OVER)
                                    .build();

                            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                                    .get(0)
                                    .sendMessage(message)
                                    .queue();
                        }, err -> {
                            if (err != null) {
                                MessageEmbed message = new EmbedBuilder()
                                        .setTitle("Game Over")
                                        .setDescription("The game broke, I'll get you next time Swamplings")
                                        .setColor(Constants.HALOWEEN_ORANGE)
                                        .setImage(GAME_OVER)
                                        .build();

                                philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                                        .get(0)
                                        .sendMessage(message)
                                        .queue();
                            }
                        });
                    }, err -> {
                        if (err != null) {
                            MessageEmbed message = new EmbedBuilder()
                                    .setTitle("Game Over")
                                    .setDescription("The game broke, I'll get you next time Swamplings")
                                    .setColor(Constants.HALOWEEN_ORANGE)
                                    .setImage(GAME_OVER)
                                    .build();

                            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                                    .get(0)
                                    .sendMessage(message)
                                    .queue();
                        }
                    });

            return;
        }

        List<DiscordUser> allUsers = discordUserRepository.findAll();
        Collections.shuffle(allUsers);

        Member member = null;
        for (DiscordUser winningUser : allUsers) {
            try {
                Member memberById = philJda.getGuilds().get(0).getMemberById(winningUser.getId());
                if (memberById != null
                        && !memberById.getUser().isBot()
                        && winningUser.getXp() > MAX_POINTS
                        && !swampyGamesConfig.getPastVictims().contains(winningUser.getId())) {
                    member = memberById;
                }
            } catch (Exception ignored) {}
        }

        if (member != null) {
            swampyGamesConfig.setJigsawAwaiting(member.getId());
            swampyGamesConfig.getPastVictims().add(member.getId());
            MessageEmbed message = new EmbedBuilder()
                    .setTitle("Let's Play A Game")
                    .setDescription("In 15 minutes, I'll be taking " + NumberFormat.getIntegerInstance().format(MAX_POINTS) +
                            " points from The Swamp.\nYou decide who I take the points from.\n\n" +
                            "If no one reacts, I'll take " + NumberFormat.getIntegerInstance().format(MAX_POINTS) +
                            " from " + member.getAsMention() + ", but if you react " + BICYCLE +
                            " selflessly to save your Swampy Brethren, " +
                            "the points with be split with everyone that reacts to this message.\n\n" +
                            "The more people react, the less you have to lose.\n\nTick tock, Swamplings.")
                    .setColor(hasRole(member, Constants.CHAOS_CHILDREN_ROLE) || "486427102854381568".equalsIgnoreCase(member.getId()) ? MICKEY_COLOR : JIGSAW_COLOR)
                    .setImage(hasRole(member, Constants.CHAOS_CHILDREN_ROLE) || "486427102854381568".equalsIgnoreCase(member.getId()) ? MICKEY : JIGSAW)
                    .build();

            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(message)
                    .queue(msg -> {
                        msg.addReaction(BICYCLE).queue();
                        swampyGamesConfig.setJigsawMessageId(msg.getId());
                        swampyGamesConfigRepository.save(swampyGamesConfig);
                    });
        } else {
            MessageEmbed message = new EmbedBuilder()
                    .setTitle("Let's Play A Game")
                    .setDescription("Jigsaw was spotted nearby, keep an eye out")
                    .setColor(JIGSAW_COLOR)
                    .setImage(JIGSAW)
                    .build();

            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(message)
                    .queue();
        }
    }

}
