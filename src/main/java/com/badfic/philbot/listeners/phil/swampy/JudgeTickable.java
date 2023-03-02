package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.CourtCase;
import com.badfic.philbot.data.phil.CourtCaseRepository;
import com.badfic.philbot.service.BaseService;
import com.badfic.philbot.service.MinuteTickable;
import com.google.common.collect.ImmutableMap;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JudgeTickable extends BaseService implements MinuteTickable {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private CourtCaseRepository courtCaseRepository;

    @Override
    public void runMinutelyTask() {
        Guild guild = philJda.getGuilds().get(0);
        Role megaHellRole = guild.getRolesByName(Constants.MEGA_HELL_ROLE, false).get(0);
        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);
        TextChannel megaHellChannel = philJda.getTextChannelsByName(Constants.MEGA_HELL_CHANNEL, false).get(0);
        LocalDateTime now = LocalDateTime.now();

        for (CourtCase courtCase : courtCaseRepository.findAll()) {
            try {
                if (courtCase.getTrialDate() != null && courtCase.getTrialDate().isBefore(now)) {
                    try {
                        Message trialMessage = swampysChannel.retrieveMessageById(courtCase.getTrialMessageId()).timeout(30, TimeUnit.SECONDS).complete();

                        Map<JudgeCommand.Sentence, MutableInt> sentenceMap = ImmutableMap.of(
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
                            courtCaseRepository.deleteById(courtCase.getDefendantId());
                            continue;
                        }

                        JudgeCommand.Sentence winningSentence = sentenceMap.entrySet().stream()
                                .max(Comparator.comparingInt(a -> a.getValue().getValue()))
                                .orElseThrow(IllegalStateException::new)
                                .getKey();

                        switch (winningSentence) {
                            case ACQUIT -> {
                                swampysChannel.sendMessage("<@!" + courtCase.getDefendantId() + "> has been acquitted").queue();
                                courtCaseRepository.deleteById(courtCase.getDefendantId());
                            }
                            case ONE_HOUR -> {
                                guild.addRoleToMember(UserSnowflake.fromId(courtCase.getDefendantId()), megaHellRole).queue();
                                courtCase.setTrialDate(null);
                                courtCase.setReleaseDate(LocalDateTime.now().plusHours(1));
                                courtCaseRepository.save(courtCase);
                                swampysChannel.sendMessage("<@!" + courtCase.getDefendantId() + "> has been sentenced to 1 hour in mega hell for "
                                        + courtCase.getCrime()).queue();
                                megaHellChannel.sendMessage("<@!" + courtCase.getDefendantId() + "> has been sentenced to 1 hour in mega hell for "
                                        + courtCase.getCrime()).queue();
                            }
                            case FIVE_HOUR -> {
                                guild.addRoleToMember(UserSnowflake.fromId(courtCase.getDefendantId()), megaHellRole).queue();
                                courtCase.setTrialDate(null);
                                courtCase.setReleaseDate(LocalDateTime.now().plusHours(5));
                                courtCaseRepository.save(courtCase);
                                swampysChannel.sendMessage("<@!" + courtCase.getDefendantId() + "> has been sentenced to 5 hours in mega hell for "
                                        + courtCase.getCrime()).queue();
                                megaHellChannel.sendMessage("<@!" + courtCase.getDefendantId() + "> has been sentenced to 5 hours in mega hell for "
                                        + courtCase.getCrime()).queue();
                            }
                            case ONE_DAY -> {
                                guild.addRoleToMember(UserSnowflake.fromId(courtCase.getDefendantId()), megaHellRole).queue();
                                courtCase.setTrialDate(null);
                                courtCase.setReleaseDate(LocalDateTime.now().plusDays(1));
                                courtCaseRepository.save(courtCase);
                                swampysChannel.sendMessage("<@!" + courtCase.getDefendantId() + "> has been sentenced to 1 day in mega hell for "
                                        + courtCase.getCrime()).queue();
                                megaHellChannel.sendMessage("<@!" + courtCase.getDefendantId() + "> has been sentenced to 1 day in mega hell for "
                                        + courtCase.getCrime()).queue();
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error with trial for [userId={}]", courtCase.getDefendantId(), e);
                        honeybadgerReporter.reportError(e, null, "Error with trial for user " + courtCase.getDefendantId());
                        courtCaseRepository.deleteById(courtCase.getDefendantId());
                        swampysChannel.sendMessage("Trial for <@!" + courtCase.getDefendantId() + "> aborted.").queue();
                    }
                } else if (courtCase.getReleaseDate() != null && courtCase.getReleaseDate().isBefore(now)) {
                    courtCaseRepository.deleteById(courtCase.getDefendantId());

                    try {
                        guild.removeRoleFromMember(UserSnowflake.fromId(courtCase.getDefendantId()), megaHellRole).queue();
                        megaHellChannel.sendMessage("<@!" + courtCase.getDefendantId() + "> has been released from mega-hell").queue();
                    } catch (Exception e) {
                        logger.error("Error with release date for [userId={}]", courtCase.getDefendantId(), e);
                        honeybadgerReporter.reportError(e, null, "Error with release date for user " + courtCase.getDefendantId());
                        megaHellChannel.sendMessage("Sentence for <@!" + courtCase.getDefendantId() + "> aborted.").queue();
                    }
                }
            } catch (Exception e) {
                logger.error("Error with court case for [userId={}]", courtCase.getDefendantId(), e);
                honeybadgerReporter.reportError(e, null, "Error with court case for user " + courtCase.getDefendantId());
            }
        }
    }
}
