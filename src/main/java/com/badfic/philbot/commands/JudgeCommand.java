package com.badfic.philbot.commands;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.CourtCase;
import com.badfic.philbot.data.CourtCaseDal;
import com.badfic.philbot.service.OnJdaReady;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JudgeCommand extends BaseNormalCommand implements OnJdaReady {
    public enum Sentence {
        ACQUIT("❌"),
        ONE_HOUR("⏲️"),
        FIVE_HOUR("\uD83D\uDD54"),
        ONE_DAY("\uD83D\uDCC6");

        private final String emoji;

        Sentence(String emoji) {
            this.emoji = emoji;
        }

        public String getEmoji() {
            return emoji;
        }
    }

    private final CourtCaseDal courtCaseDal;

    public JudgeCommand(CourtCaseDal courtCaseDal) {
        name = "judge";
        help = """
                `!!judge @user for such and such crime` To judge a user for various crimes. People then vote on their sentence in mega-hell
                `!!judge mistrial @user` if you accidentally judged them you can cancel the trial, but only if they have not been convicted yet.
                `!!judge show @user` to see how much longer the person has for their trial or their sentence.""";
        this.courtCaseDal = courtCaseDal;
    }

    @Override
    public void run() {
        for (CourtCase courtCase : courtCaseDal.findAll()) {
            if (courtCase.getTrialDate() != null) {
                taskScheduler.schedule(() -> trialComplete(courtCase.getDefendantId()), courtCase.getTrialDate().toInstant(ZoneOffset.UTC));
            } else if (courtCase.getReleaseDate() != null) {
                taskScheduler.schedule(() -> releaseComplete(courtCase.getDefendantId()), courtCase.getReleaseDate().toInstant(ZoneOffset.UTC));
            }
        }
    }

    @Override
    public void execute(CommandEvent event) {
        if (CollectionUtils.size(event.getMessage().getMentions().getMembers()) != 1) {
            event.replyError("Please mention a user to accuse. Example `!!judge @user for such and such crime`");
            return;
        }

        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);
        Member accuser = event.getMember();
        Member defendant = event.getMessage().getMentions().getMembers().get(0);

        Optional<CourtCase> optionalExistingCase = courtCaseDal.findById(defendant.getIdLong());

        if (event.getArgs().startsWith("mistrial")) {
            if (optionalExistingCase.isPresent()) {
                CourtCase courtCase = optionalExistingCase.get();
                if (defendant.getIdLong() == courtCase.getDefendantId()
                        && accuser.getIdLong() == courtCase.getAccuserId()
                        && courtCase.getReleaseDate() == null) {
                    long trialMessageId = courtCase.getTrialMessageId();
                    courtCaseDal.deleteById(courtCase.getDefendantId());
                    swampysChannel.retrieveMessageById(trialMessageId)
                            .queue(msg -> msg.delete().queue());
                    event.reply("The accuser has declared a mistrial. " + defendant.getEffectiveName() + " is free to go");
                    return;
                }
            }
            event.replyError("That person is not on trial, or you aren't the original accuser");
            return;
        }

        if (optionalExistingCase.isPresent()) {
            CourtCase courtCase = optionalExistingCase.get();

            if (courtCase.getReleaseDate() != null) {
                event.reply(defendant.getEffectiveName() + " is currently serving a sentence of "
                        + Constants.prettyPrintDuration(Duration.between(LocalDateTime.now(), courtCase.getReleaseDate())));
            } else {
                event.reply(defendant.getEffectiveName() + " is currently on trial with "
                        + Constants.prettyPrintDuration(Duration.between(LocalDateTime.now(), courtCase.getTrialDate())) + " left");
            }

            return;
        }

        if (event.getArgs().startsWith("show")) {
            event.reply(defendant.getEffectiveName() + " is neither serving nor on trial.");
            return;
        }

        String crime = event.getArgs().replace("<@" + defendant.getIdLong() + ">", "").trim();
        if (crime.startsWith("for")) {
            crime = crime.substring(3).trim();
        }
        if (StringUtils.isBlank(crime)) {
            crime = "unspecified crimes";
        }
        String finalCrime = crime;

        String description = accuser.getAsMention() + " is accusing " + defendant.getAsMention() +
                " of " + crime + "\n\nReact below with a\n" + Sentence.ACQUIT.getEmoji() + " to acquit\n" +
                Sentence.ONE_HOUR.getEmoji() + " for a 1 hour sentence\n" +
                Sentence.FIVE_HOUR.getEmoji() + " for a 5 hour sentence\n" +
                Sentence.ONE_DAY.getEmoji() + " for a 1 day sentence";

        swampysChannel.sendMessageEmbeds(Constants.simpleEmbedThumbnail("Jury Summons", description, defendant.getEffectiveAvatarUrl())).queue(msg -> {
            CourtCase courtCase = new CourtCase(defendant.getIdLong(), accuser.getIdLong(), msg.getIdLong(), finalCrime,
                    LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(15));
            courtCaseDal.insert(courtCase);

            msg.addReaction(Emoji.fromUnicode(Sentence.ACQUIT.getEmoji())).queue();
            msg.addReaction(Emoji.fromUnicode(Sentence.ONE_HOUR.getEmoji())).queue();
            msg.addReaction(Emoji.fromUnicode(Sentence.FIVE_HOUR.getEmoji())).queue();
            msg.addReaction(Emoji.fromUnicode(Sentence.ONE_DAY.getEmoji())).queue();
        });
    }

    private void trialComplete(long defendentId) {
        Guild guild = philJda.getGuildById(baseConfig.guildId);
        Role megaHellRole = guild.getRolesByName(Constants.MEGA_HELL_ROLE, false).get(0);
        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);
        TextChannel megaHellChannel = philJda.getTextChannelsByName(Constants.MEGA_HELL_CHANNEL, false).get(0);

        Optional<CourtCase> optCourtCase = courtCaseDal.findById(defendentId);

        if (optCourtCase.isEmpty()) {
            swampysChannel.sendMessage("Megahell trial for <@" + defendentId + "> failed.").queue();
            return;
        }

        CourtCase courtCase = optCourtCase.get();

        try {
            Message trialMessage = swampysChannel.retrieveMessageById(courtCase.getTrialMessageId()).timeout(30, TimeUnit.SECONDS).complete();

            Map<Sentence, MutableInt> sentenceMap = Map.of(
                    JudgeCommand.Sentence.ACQUIT, new MutableInt(-1),
                    JudgeCommand.Sentence.ONE_HOUR, new MutableInt(-1),
                    JudgeCommand.Sentence.FIVE_HOUR, new MutableInt(-1),
                    JudgeCommand.Sentence.ONE_DAY, new MutableInt(-1));
            List<MessageReaction> reactions = trialMessage.getReactions();
            for (MessageReaction reaction : reactions) {
                if (reaction.getEmoji().getType() == Emoji.Type.UNICODE) {
                    String emoji = reaction.getEmoji().asUnicode().getName();

                    if (JudgeCommand.Sentence.ACQUIT.getEmoji().equals(emoji)) {
                        sentenceMap.get(JudgeCommand.Sentence.ACQUIT).add(reaction.getCount());
                    } else if (JudgeCommand.Sentence.ONE_HOUR.getEmoji().equals(emoji)) {
                        sentenceMap.get(JudgeCommand.Sentence.ONE_HOUR).add(reaction.getCount());
                    } else if (JudgeCommand.Sentence.FIVE_HOUR.getEmoji().equals(emoji)) {
                        sentenceMap.get(JudgeCommand.Sentence.FIVE_HOUR).add(reaction.getCount());
                    } else if (JudgeCommand.Sentence.ONE_DAY.getEmoji().equals(emoji)) {
                        sentenceMap.get(JudgeCommand.Sentence.ONE_DAY).add(reaction.getCount());
                    }
                }
            }
            trialMessage.clearReactions().queue();

            if (sentenceMap.entrySet().stream().allMatch(e -> e.getValue().getValue() == 0)) {
                swampysChannel.sendMessage("<@" + defendentId + "> has been acquitted").queue();
                courtCaseDal.deleteById(defendentId);
                return;
            }

            JudgeCommand.Sentence winningSentence = sentenceMap.entrySet().stream()
                    .max(Comparator.comparingInt(a -> a.getValue().getValue()))
                    .orElseThrow(IllegalStateException::new)
                    .getKey();

            switch (winningSentence) {
                case ACQUIT -> {
                    swampysChannel.sendMessage("<@" + defendentId + "> has been acquitted").queue();
                    courtCaseDal.deleteById(defendentId);
                }
                case ONE_HOUR -> {
                    guild.addRoleToMember(UserSnowflake.fromId(defendentId), megaHellRole).queue();
                    courtCase.setTrialDate(null);
                    LocalDateTime releaseDate = LocalDateTime.now().plusHours(1);
                    courtCase.setReleaseDate(releaseDate);
                    courtCaseDal.update(courtCase);

                    taskScheduler.schedule(() -> releaseComplete(defendentId), releaseDate.toInstant(ZoneOffset.UTC));

                    swampysChannel.sendMessage("<@" + defendentId + "> has been sentenced to 1 hour in mega hell for "
                            + courtCase.getCrime()).queue();
                    megaHellChannel.sendMessage("<@" + defendentId + "> has been sentenced to 1 hour in mega hell for "
                            + courtCase.getCrime()).queue();
                }
                case FIVE_HOUR -> {
                    guild.addRoleToMember(UserSnowflake.fromId(defendentId), megaHellRole).queue();
                    courtCase.setTrialDate(null);
                    LocalDateTime releaseDate = LocalDateTime.now().plusHours(5);
                    courtCase.setReleaseDate(releaseDate);
                    courtCaseDal.update(courtCase);

                    taskScheduler.schedule(() -> releaseComplete(defendentId), releaseDate.toInstant(ZoneOffset.UTC));

                    swampysChannel.sendMessage("<@" + defendentId + "> has been sentenced to 5 hours in mega hell for "
                            + courtCase.getCrime()).queue();
                    megaHellChannel.sendMessage("<@" + defendentId + "> has been sentenced to 5 hours in mega hell for "
                            + courtCase.getCrime()).queue();
                }
                case ONE_DAY -> {
                    guild.addRoleToMember(UserSnowflake.fromId(defendentId), megaHellRole).queue();
                    courtCase.setTrialDate(null);
                    LocalDateTime releaseDate = LocalDateTime.now().plusDays(1);
                    courtCase.setReleaseDate(releaseDate);
                    courtCaseDal.update(courtCase);

                    taskScheduler.schedule(() -> releaseComplete(defendentId), releaseDate.toInstant(ZoneOffset.UTC));

                    swampysChannel.sendMessage("<@" + defendentId + "> has been sentenced to 1 day in mega hell for "
                            + courtCase.getCrime()).queue();
                    megaHellChannel.sendMessage("<@" + defendentId + "> has been sentenced to 1 day in mega hell for "
                            + courtCase.getCrime()).queue();
                }
            }
        } catch (Exception e) {
            log.error("Error with trial sentencing for [userId={}]", defendentId, e);
            courtCaseDal.deleteById(defendentId);
            swampysChannel.sendMessage("Megahell trial sentencing for <@" + defendentId + "> failed.").queue();
        }
    }

    private void releaseComplete(long defendentId) {
        Guild guild = philJda.getGuildById(baseConfig.guildId);
        Role megaHellRole = guild.getRolesByName(Constants.MEGA_HELL_ROLE, false).get(0);
        TextChannel megaHellChannel = philJda.getTextChannelsByName(Constants.MEGA_HELL_CHANNEL, false).get(0);

        courtCaseDal.findById(defendentId)
                .ifPresent(courtCase -> courtCaseDal.deleteById(defendentId));

        try {
            guild.removeRoleFromMember(UserSnowflake.fromId(defendentId), megaHellRole).queue();
            megaHellChannel.sendMessage("<@" + defendentId + "> has been released from mega-hell").queue();
        } catch (Exception e) {
            log.error("Error with release date for [userId={}]", defendentId, e);
            megaHellChannel.sendMessage("Megahell sentence release for <@" + defendentId + "> failed.").queue();
        }
    }

}
