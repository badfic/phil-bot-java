package com.badfic.philbot.commands;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.CourtCase;
import com.badfic.philbot.data.CourtCaseDao;
import com.badfic.philbot.service.MinuteTickable;
import com.google.common.collect.ImmutableMap;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.time.Duration;
import java.time.LocalDateTime;
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
public class JudgeCommand extends BaseNormalCommand implements MinuteTickable {
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

    private final CourtCaseDao courtCaseDao;

    public JudgeCommand(CourtCaseDao courtCaseDao) {
        name = "judge";
        help = """
                `!!judge @user for such and such crime` To judge a user for various crimes. People then vote on their sentence in mega-hell
                `!!judge mistrial @user` if you accidentally judged them you can cancel the trial, but only if they have not been convicted yet.
                `!!judge show @user` to see how much longer the person has for their trial or their sentence.""";
        this.courtCaseDao = courtCaseDao;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (CollectionUtils.size(event.getMessage().getMentions().getMembers()) != 1) {
            event.replyError("Please mention a user to accuse. Example `!!judge @user for such and such crime`");
            return;
        }

        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);
        Member accuser = event.getMember();
        Member defendant = event.getMessage().getMentions().getMembers().get(0);

        Optional<CourtCase> optionalExistingCase = courtCaseDao.findById(defendant.getIdLong());

        if (event.getArgs().startsWith("mistrial")) {
            if (optionalExistingCase.isPresent()) {
                CourtCase courtCase = optionalExistingCase.get();
                if (defendant.getIdLong() == courtCase.getDefendantId()
                        && accuser.getIdLong() == courtCase.getAccuserId()
                        && courtCase.getReleaseDate() == null) {
                    long trialMessageId = courtCase.getTrialMessageId();
                    courtCaseDao.deleteById(courtCase.getDefendantId());
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

        String crime = event.getArgs().replace("<@!" + defendant.getIdLong() + ">", "").trim();
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
            courtCaseDao.save(courtCase);

            msg.addReaction(Emoji.fromUnicode(Sentence.ACQUIT.getEmoji())).queue();
            msg.addReaction(Emoji.fromUnicode(Sentence.ONE_HOUR.getEmoji())).queue();
            msg.addReaction(Emoji.fromUnicode(Sentence.FIVE_HOUR.getEmoji())).queue();
            msg.addReaction(Emoji.fromUnicode(Sentence.ONE_DAY.getEmoji())).queue();
        });
    }

    @Override
    public void runMinutelyTask() {
        Guild guild = philJda.getGuildById(baseConfig.guildId);
        Role megaHellRole = guild.getRolesByName(Constants.MEGA_HELL_ROLE, false).get(0);
        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);
        TextChannel megaHellChannel = philJda.getTextChannelsByName(Constants.MEGA_HELL_CHANNEL, false).get(0);
        LocalDateTime now = LocalDateTime.now();

        for (CourtCase courtCase : courtCaseDao.findAll()) {
            try {
                if (courtCase.getTrialDate() != null && courtCase.getTrialDate().isBefore(now)) {
                    try {
                        Message trialMessage = swampysChannel.retrieveMessageById(courtCase.getTrialMessageId()).timeout(30, TimeUnit.SECONDS).complete();

                        Map<Sentence, MutableInt> sentenceMap = ImmutableMap.of(
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
                            swampysChannel.sendMessage("<@!" + courtCase.getDefendantId() + "> has been acquitted").queue();
                            courtCaseDao.deleteById(courtCase.getDefendantId());
                            continue;
                        }

                        JudgeCommand.Sentence winningSentence = sentenceMap.entrySet().stream()
                                .max(Comparator.comparingInt(a -> a.getValue().getValue()))
                                .orElseThrow(IllegalStateException::new)
                                .getKey();

                        switch (winningSentence) {
                            case ACQUIT -> {
                                swampysChannel.sendMessage("<@!" + courtCase.getDefendantId() + "> has been acquitted").queue();
                                courtCaseDao.deleteById(courtCase.getDefendantId());
                            }
                            case ONE_HOUR -> {
                                guild.addRoleToMember(UserSnowflake.fromId(courtCase.getDefendantId()), megaHellRole).queue();
                                courtCase.setTrialDate(null);
                                courtCase.setReleaseDate(LocalDateTime.now().plusHours(1));
                                courtCaseDao.save(courtCase);
                                swampysChannel.sendMessage("<@!" + courtCase.getDefendantId() + "> has been sentenced to 1 hour in mega hell for "
                                        + courtCase.getCrime()).queue();
                                megaHellChannel.sendMessage("<@!" + courtCase.getDefendantId() + "> has been sentenced to 1 hour in mega hell for "
                                        + courtCase.getCrime()).queue();
                            }
                            case FIVE_HOUR -> {
                                guild.addRoleToMember(UserSnowflake.fromId(courtCase.getDefendantId()), megaHellRole).queue();
                                courtCase.setTrialDate(null);
                                courtCase.setReleaseDate(LocalDateTime.now().plusHours(5));
                                courtCaseDao.save(courtCase);
                                swampysChannel.sendMessage("<@!" + courtCase.getDefendantId() + "> has been sentenced to 5 hours in mega hell for "
                                        + courtCase.getCrime()).queue();
                                megaHellChannel.sendMessage("<@!" + courtCase.getDefendantId() + "> has been sentenced to 5 hours in mega hell for "
                                        + courtCase.getCrime()).queue();
                            }
                            case ONE_DAY -> {
                                guild.addRoleToMember(UserSnowflake.fromId(courtCase.getDefendantId()), megaHellRole).queue();
                                courtCase.setTrialDate(null);
                                courtCase.setReleaseDate(LocalDateTime.now().plusDays(1));
                                courtCaseDao.save(courtCase);
                                swampysChannel.sendMessage("<@!" + courtCase.getDefendantId() + "> has been sentenced to 1 day in mega hell for "
                                        + courtCase.getCrime()).queue();
                                megaHellChannel.sendMessage("<@!" + courtCase.getDefendantId() + "> has been sentenced to 1 day in mega hell for "
                                        + courtCase.getCrime()).queue();
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error with trial for [userId={}]", courtCase.getDefendantId(), e);
                        honeybadgerReporter.reportError(e, null, "Error with trial for user " + courtCase.getDefendantId());
                        courtCaseDao.deleteById(courtCase.getDefendantId());
                        swampysChannel.sendMessage("Trial for <@!" + courtCase.getDefendantId() + "> aborted.").queue();
                    }
                } else if (courtCase.getReleaseDate() != null && courtCase.getReleaseDate().isBefore(now)) {
                    courtCaseDao.deleteById(courtCase.getDefendantId());

                    try {
                        guild.removeRoleFromMember(UserSnowflake.fromId(courtCase.getDefendantId()), megaHellRole).queue();
                        megaHellChannel.sendMessage("<@!" + courtCase.getDefendantId() + "> has been released from mega-hell").queue();
                    } catch (Exception e) {
                        log.error("Error with release date for [userId={}]", courtCase.getDefendantId(), e);
                        honeybadgerReporter.reportError(e, null, "Error with release date for user " + courtCase.getDefendantId());
                        megaHellChannel.sendMessage("Sentence for <@!" + courtCase.getDefendantId() + "> aborted.").queue();
                    }
                }
            } catch (Exception e) {
                log.error("Error with court case for [userId={}]", courtCase.getDefendantId(), e);
                honeybadgerReporter.reportError(e, null, "Error with court case for user " + courtCase.getDefendantId());
            }
        }
    }
}
