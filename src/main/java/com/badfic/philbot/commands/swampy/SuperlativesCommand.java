package com.badfic.philbot.commands.swampy;

import com.badfic.philbot.commands.BaseNormalCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SuperlativesCommand extends BaseNormalCommand {

    private final JDA behradJda;

    public SuperlativesCommand(@Qualifier("behradJda") JDA behradJda) {
        this.behradJda = behradJda;
        this.name = "superlatives";
        this.requiredRole = Constants.ADMIN_ROLE;
        this.help = "Announce the superlatives (highest person on each category of the leaderboard)";
    }

    @Override
    protected void execute(CommandEvent event) {
        long channelId = event.getChannel().getIdLong();
        MessageChannel channel = behradJda.getChannelById(MessageChannel.class, channelId);

        if (Objects.isNull(channel)) {
            event.replyError("Channel does not exist");
            return;
        }

        List<DiscordUser> allEligibleUsers = discordUserRepository.findByXpGreaterThan(0);
        Guild guild = event.getGuild();

        List<DiscordUser> bastards = new ArrayList<>();
        List<DiscordUser> chaosChildren = new ArrayList<>();
        for (DiscordUser user : allEligibleUsers) {
            Member member = guild.getMemberById(user.getId());

            if (Objects.nonNull(member)) {
                if (hasRole(member, Constants.EIGHTEEN_PLUS_ROLE)) {
                    bastards.add(user);
                }
                if (hasRole(member, Constants.CHAOS_CHILDREN_ROLE)) {
                    chaosChildren.add(user);
                }
            }
        }

        StringBuilder description = new StringBuilder();

        bastards.stream().min((a, b) -> (int) (a.getTaxesPoints() - b.getTaxesPoints())).ifPresent(user -> {
            description.append("Worst Taxes: ").append("<@!").append(user.getId()).append(">\n");
        });
        bastards.stream().max((a, b) -> (int) (a.getRobinhoodPoints() - b.getRobinhoodPoints())).ifPresent(user -> {
            description.append("Refunds: ").append("<@!").append(user.getId()).append(">\n");
        });
        bastards.stream().max((a, b) -> (int) (a.getStonksPoints() - b.getStonksPoints())).ifPresent(user -> {
            description.append("Stonks: ").append("<@!").append(user.getId()).append(">\n");
        });

        buildSuperlativeDescription(description, bastards);
        MessageEmbed bastardsSuperlatives = Constants.simpleEmbed("Swampy Bastards Superlatives", description.toString());

        description.setLength(0);

        chaosChildren.stream().max((a, b) -> (int) (a.getShrekoningPoints() - b.getShrekoningPoints())).ifPresent(user -> {
            description.append("Shrekoning: ").append("<@!").append(user.getId()).append(">\n");
        });

        buildSuperlativeDescription(description, chaosChildren);
        MessageEmbed chaosChildrenSuperlatives = Constants.simpleEmbed("Chaos Children Superlatives", description.toString());

        channel.sendMessageEmbeds(bastardsSuperlatives, chaosChildrenSuperlatives).queue();
    }

    private void buildSuperlativeDescription(StringBuilder description, List<DiscordUser> list) {
        String trickOrTreatName = getSwampyGamesConfig().getTrickOrTreatName();

        list.stream().max((a, b) -> (int) (a.getTrickOrTreatPoints() - b.getTrickOrTreatPoints())).ifPresent(user -> {
            description.append(trickOrTreatName).append(": <@!").append(user.getId()).append(">\n");
        });
        list.stream().max((a, b) -> (int) (a.getSweepstakesPoints() - b.getSweepstakesPoints())).ifPresent(user -> {
            description.append("Sweepstakes: ").append("<@!").append(user.getId()).append(">\n");
        });
        list.stream().max((a, b) -> (int) (a.getBoostPoints() - b.getBoostPoints())).ifPresent(user -> {
            description.append("Boosts: ").append("<@!").append(user.getId()).append(">\n");
        });
        list.stream().max((a, b) -> (int) (a.getUpvotedPoints() - b.getUpvotedPoints())).ifPresent(user -> {
            description.append("Upvoted: ").append("<@!").append(user.getId()).append(">\n");
        });
        list.stream().max((a, b) -> (int) (a.getUpvoterPoints() - b.getUpvoterPoints())).ifPresent(user -> {
            description.append("Upvoter: ").append("<@!").append(user.getId()).append(">\n");
        });
        list.stream().max((a, b) -> (int) (a.getMapsPoints() - b.getMapsPoints())).ifPresent(user -> {
            description.append("Map: ").append("<@!").append(user.getId()).append(">\n");
        });
        list.stream().max((a, b) -> (int) (a.getTriviaPoints() - b.getTriviaPoints())).ifPresent(user -> {
            description.append("Trivia: ").append("<@!").append(user.getId()).append(">\n");
        });
        list.stream().max((a, b) -> (int) (a.getQuoteTriviaPoints() - b.getQuoteTriviaPoints())).ifPresent(user -> {
            description.append("Quote Trivia: ").append("<@!").append(user.getId()).append(">\n");
        });
        list.stream().max((a, b) -> (int) (a.getFightPoints() - b.getFightPoints())).ifPresent(user -> {
            description.append("Fight: ").append("<@!").append(user.getId()).append(">\n");
        });
        list.stream().min((a, b) -> (int) (a.getNoNoPoints() - b.getNoNoPoints())).ifPresent(user -> {
            description.append("No No: ").append("<@!").append(user.getId()).append(">\n");
        });
        list.stream().max((a, b) -> (int) (a.getMessagePoints() - b.getMessagePoints())).ifPresent(user -> {
            description.append("Talkative: ").append("<@!").append(user.getId()).append(">\n");
        });
        list.stream().max((a, b) -> (int) (a.getPictureMessagePoints() - b.getPictureMessagePoints())).ifPresent(user -> {
            description.append("Images: ").append("<@!").append(user.getId()).append(">\n");
        });
        list.stream().max((a, b) -> (int) (a.getVoiceChatPoints() - b.getVoiceChatPoints())).ifPresent(user -> {
            description.append("Voice Chat: ").append("<@!").append(user.getId()).append(">\n");
        });
        list.stream().max((a, b) -> (int) (a.getReactPoints() - b.getReactPoints())).ifPresent(user -> {
            description.append("Give Reactions: ").append("<@!").append(user.getId()).append(">\n");
        });
        list.stream().max((a, b) -> (int) (a.getReactedPoints() - b.getReactedPoints())).ifPresent(user -> {
            description.append("Receive Reactions: ").append("<@!").append(user.getId()).append(">\n");
        });
        list.stream().max((a, b) -> (int) (a.getSlotsLosses() - b.getSlotsLosses())).ifPresent(user -> {
            description.append("Slottiest: ").append("<@!").append(user.getId()).append(">\n");
        });
        list.stream().max((a, b) -> (int) (a.getSlotsWinnerWinnerPoints() - b.getSlotsWinnerWinnerPoints())).ifPresent(user -> {
            description.append("Slots Wins: ").append("<@!").append(user.getId()).append(">\n");
        });
        list.stream().max((a, b) -> (int) (a.getSwiperParticipations() - b.getSwiperParticipations())).ifPresent(user -> {
            description.append("Swiper Saves: ").append("<@!").append(user.getId()).append(">\n");
        });
    }
}
