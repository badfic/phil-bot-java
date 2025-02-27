package com.badfic.philbot.commands.bang;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.CourtCase;
import com.badfic.philbot.data.CourtCaseDal;
import com.badfic.philbot.service.OnJdaReady;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class JudgeCommand extends BaseBangCommand implements OnJdaReady {
    @Getter
    @RequiredArgsConstructor
    enum Sentence {
        ACQUIT("❌"),
        ONE_HOUR("⏲️"),
        FIVE_HOUR("\uD83D\uDD54"),
        ONE_DAY("\uD83D\uDCC6");

        private final String emoji;
    }

    private final CourtCaseDal courtCaseDal;

    JudgeCommand(final CourtCaseDal courtCaseDal) {
        name = "judge";
        help = """
                `!!judge @user for such and such crime` To judge a user for various crimes. People then vote on their sentence in mega-hell
                `!!judge mistrial @user` if you accidentally judged them you can cancel the trial, but only if they have not been convicted yet.
                `!!judge show @user` to see how much longer the person has for their trial or their sentence.""";
        this.courtCaseDal = courtCaseDal;
    }

    @Override
    public void run() {
        for (final var courtCase : courtCaseDal.findAll()) {
            if (courtCase.getTrialDate() != null) {
                scheduleTask(() -> trialComplete(courtCase.getDefendantId()), courtCase.getTrialDate());
            } else if (courtCase.getReleaseDate() != null) {
                scheduleTask(() -> releaseComplete(courtCase.getDefendantId()), courtCase.getReleaseDate());
            }
        }
    }

    @Override
    public void execute(final CommandEvent event) {
        if (CollectionUtils.size(event.getMessage().getMentions().getMembers()) != 1) {
            event.replyError("Please mention a user to accuse. Example `!!judge @user for such and such crime`");
            return;
        }

        final var swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).getFirst();
        final var accuser = event.getMember();
        final var defendant = event.getMessage().getMentions().getMembers().getFirst();

        final var optionalExistingCase = courtCaseDal.findById(defendant.getIdLong());

        if (event.getArgs().startsWith("mistrial")) {
            if (optionalExistingCase.isPresent()) {
                final var courtCase = optionalExistingCase.get();
                if (defendant.getIdLong() == courtCase.getDefendantId()
                        && accuser.getIdLong() == courtCase.getAccuserId()
                        && courtCase.getReleaseDate() == null) {
                    final var trialMessageId = courtCase.getTrialMessageId();
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
            final var courtCase = optionalExistingCase.get();

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

        var crime = event.getArgs().replace("<@" + defendant.getIdLong() + ">", "").trim();
        if (crime.startsWith("for")) {
            crime = crime.substring(3).trim();
        }
        if (StringUtils.isBlank(crime)) {
            crime = "unspecified crimes";
        }
        final var finalCrime = crime;

        final var description = accuser.getAsMention() + " is accusing " + defendant.getAsMention() +
                " of " + crime + "\n\nReact below with a\n" + Sentence.ACQUIT.getEmoji() + " to acquit\n" +
                Sentence.ONE_HOUR.getEmoji() + " for a 1 hour sentence\n" +
                Sentence.FIVE_HOUR.getEmoji() + " for a 5 hour sentence\n" +
                Sentence.ONE_DAY.getEmoji() + " for a 1 day sentence";

        swampysChannel.sendMessageEmbeds(Constants.simpleEmbedThumbnail("Jury Summons", description, defendant.getEffectiveAvatarUrl())).queue(msg -> {
            final var courtCase = new CourtCase(defendant.getIdLong(), accuser.getIdLong(), msg.getIdLong(), finalCrime,
                    LocalDateTime.now().plusMinutes(15));
            courtCaseDal.insert(courtCase);

            msg.addReaction(Emoji.fromUnicode(Sentence.ACQUIT.getEmoji())).queue();
            msg.addReaction(Emoji.fromUnicode(Sentence.ONE_HOUR.getEmoji())).queue();
            msg.addReaction(Emoji.fromUnicode(Sentence.FIVE_HOUR.getEmoji())).queue();
            msg.addReaction(Emoji.fromUnicode(Sentence.ONE_DAY.getEmoji())).queue();

            scheduleTask(() -> trialComplete(courtCase.getDefendantId()), courtCase.getTrialDate());
        });
    }

    private void trialComplete(final long defendantId) {
        final var guild = philJda.getGuildById(baseConfig.guildId);
        final var megaHellRole = guild.getRolesByName(Constants.MEGA_HELL_ROLE, false).getFirst();
        final var swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).getFirst();
        final var megaHellChannel = philJda.getTextChannelsByName(Constants.MEGA_HELL_CHANNEL, false).getFirst();

        final var optCourtCase = courtCaseDal.findById(defendantId);

        if (optCourtCase.isEmpty()) {
            swampysChannel.sendMessage("Megahell trial for <@" + defendantId + "> failed.").queue();
            return;
        }

        final var courtCase = optCourtCase.get();

        try {
            final var trialMessage = swampysChannel.retrieveMessageById(courtCase.getTrialMessageId()).timeout(30, TimeUnit.SECONDS).complete();

            final var sentenceMap = Map.of(
                    JudgeCommand.Sentence.ACQUIT, new MutableInt(-1),
                    JudgeCommand.Sentence.ONE_HOUR, new MutableInt(-1),
                    JudgeCommand.Sentence.FIVE_HOUR, new MutableInt(-1),
                    JudgeCommand.Sentence.ONE_DAY, new MutableInt(-1));
            final var reactions = trialMessage.getReactions();
            for (final var reaction : reactions) {
                if (reaction.getEmoji().getType() == Emoji.Type.UNICODE) {
                    final var emoji = reaction.getEmoji().asUnicode().getName();

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
                swampysChannel.sendMessage("<@" + defendantId + "> has been acquitted").queue();
                courtCaseDal.deleteById(defendantId);
                return;
            }

            final var winningSentence = sentenceMap.entrySet().stream()
                    .max(Comparator.comparingInt(a -> a.getValue().getValue()))
                    .orElseThrow(IllegalStateException::new)
                    .getKey();

            switch (winningSentence) {
                case ACQUIT -> {
                    swampysChannel.sendMessage("<@" + defendantId + "> has been acquitted").queue();
                    courtCaseDal.deleteById(defendantId);
                }
                case ONE_HOUR -> {
                    guild.addRoleToMember(UserSnowflake.fromId(defendantId), megaHellRole).queue();
                    courtCase.setTrialDate(null);
                    final var releaseDate = LocalDateTime.now().plusHours(1);
                    courtCase.setReleaseDate(releaseDate);
                    courtCaseDal.update(courtCase);

                    scheduleTask(() -> releaseComplete(defendantId), releaseDate);

                    swampysChannel.sendMessage("<@" + defendantId + "> has been sentenced to 1 hour in mega hell for "
                            + courtCase.getCrime()).queue();
                    megaHellChannel.sendMessage("<@" + defendantId + "> has been sentenced to 1 hour in mega hell for "
                            + courtCase.getCrime()).queue();
                }
                case FIVE_HOUR -> {
                    guild.addRoleToMember(UserSnowflake.fromId(defendantId), megaHellRole).queue();
                    courtCase.setTrialDate(null);
                    final var releaseDate = LocalDateTime.now().plusHours(5);
                    courtCase.setReleaseDate(releaseDate);
                    courtCaseDal.update(courtCase);

                    scheduleTask(() -> releaseComplete(defendantId), releaseDate);

                    swampysChannel.sendMessage("<@" + defendantId + "> has been sentenced to 5 hours in mega hell for "
                            + courtCase.getCrime()).queue();
                    megaHellChannel.sendMessage("<@" + defendantId + "> has been sentenced to 5 hours in mega hell for "
                            + courtCase.getCrime()).queue();
                }
                case ONE_DAY -> {
                    guild.addRoleToMember(UserSnowflake.fromId(defendantId), megaHellRole).queue();
                    courtCase.setTrialDate(null);
                    final var releaseDate = LocalDateTime.now().plusDays(1);
                    courtCase.setReleaseDate(releaseDate);
                    courtCaseDal.update(courtCase);

                    scheduleTask(() -> releaseComplete(defendantId), releaseDate);

                    swampysChannel.sendMessage("<@" + defendantId + "> has been sentenced to 1 day in mega hell for "
                            + courtCase.getCrime()).queue();
                    megaHellChannel.sendMessage("<@" + defendantId + "> has been sentenced to 1 day in mega hell for "
                            + courtCase.getCrime()).queue();
                }
            }
        } catch (final Exception e) {
            log.error("Error with trial sentencing for [userId={}]", defendantId, e);
            courtCaseDal.deleteById(defendantId);
            swampysChannel.sendMessage("Megahell trial sentencing for <@" + defendantId + "> failed.").queue();
        }
    }

    private void releaseComplete(final long defendantId) {
        final var guild = philJda.getGuildById(baseConfig.guildId);
        final var megaHellRole = guild.getRolesByName(Constants.MEGA_HELL_ROLE, false).getFirst();
        final var megaHellChannel = philJda.getTextChannelsByName(Constants.MEGA_HELL_CHANNEL, false).getFirst();

        courtCaseDal.findById(defendantId)
                .ifPresent(courtCase -> courtCaseDal.deleteById(defendantId));

        try {
            guild.removeRoleFromMember(UserSnowflake.fromId(defendantId), megaHellRole).queue();
            megaHellChannel.sendMessage("<@" + defendantId + "> has been released from mega-hell").queue();
        } catch (final Exception e) {
            log.error("Error with release date for [userId={}]", defendantId, e);
            megaHellChannel.sendMessage("Megahell sentence release for <@" + defendantId + "> failed.").queue();
        }
    }

}
