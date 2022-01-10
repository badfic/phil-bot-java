package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.CourtCase;
import com.badfic.philbot.data.phil.CourtCaseRepository;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import javax.annotation.Resource;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class JudgeCommand extends BaseSwampy {
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

    @Resource
    private CourtCaseRepository courtCaseRepository;

    public JudgeCommand() {
        name = "judge";
        help = """
                `!!judge @user for such and such crime` To judge a user for various crimes. People then vote on their sentence in mega-hell
                `!!judge mistrial @user` if you accidentally judged them you can cancel the trial, but only if they have not been convicted yet.
                `!!judge show @user` to see how much longer the person has for their trial or their sentence.""";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (CollectionUtils.size(event.getMessage().getMentionedMembers()) != 1) {
            event.replyError("Please mention a user to accuse. Example `!!judge @user for such and such crime`");
            return;
        }

        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);
        Member accuser = event.getMember();
        Member defendant = event.getMessage().getMentionedMembers().get(0);

        Optional<CourtCase> optionalExistingCase = courtCaseRepository.findById(defendant.getIdLong());

        if (event.getArgs().startsWith("mistrial")) {
            if (optionalExistingCase.isPresent()) {
                CourtCase courtCase = optionalExistingCase.get();
                if (defendant.getIdLong() == courtCase.getDefendantId()
                        && accuser.getIdLong() == courtCase.getAccuserId()
                        && courtCase.getReleaseDate() == null) {
                    long trialMessageId = courtCase.getTrialMessageId();
                    courtCaseRepository.deleteById(courtCase.getDefendantId());
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
            courtCaseRepository.save(courtCase);

            msg.addReaction(Sentence.ACQUIT.getEmoji()).queue();
            msg.addReaction(Sentence.ONE_HOUR.getEmoji()).queue();
            msg.addReaction(Sentence.FIVE_HOUR.getEmoji()).queue();
            msg.addReaction(Sentence.ONE_DAY.getEmoji()).queue();
        });
    }
}
