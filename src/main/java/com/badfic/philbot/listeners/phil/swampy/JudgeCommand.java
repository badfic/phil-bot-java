package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.phil.CourtCase;
import com.badfic.philbot.data.phil.CourtCaseRepository;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class JudgeCommand extends BaseSwampy implements PhilMarker {
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
        help = "`!!judge @user for such and such crime`\n" +
                "To judge a user for simpy crimes. People then vote on their sentence in mega-hell";
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
        String crime = event.getArgs().replace("<@!" + defendant.getIdLong() + ">", "").trim();
        if (crime.startsWith("for")) {
            crime = crime.substring(3).trim();
        }
        if (StringUtils.isBlank(crime)) {
            crime = "unspecified crimes";
        }
        String finalCrime = crime;

        if (courtCaseRepository.existsById(defendant.getIdLong())) {
            event.replyError("You cannot judge that user, they are currently serving a sentence or awaiting trial");
            return;
        }

        String description = accuser.getAsMention() + " is accusing " + defendant.getAsMention() +
                " of " + crime + "\n\nReact below with a\n" + Sentence.ACQUIT.getEmoji() + " to acquit\n" +
                Sentence.ONE_HOUR.getEmoji() + " for a 1 hour sentence\n" +
                Sentence.FIVE_HOUR.getEmoji() + " for a 5 hour sentence\n" +
                Sentence.ONE_DAY.getEmoji() + " for a 1 day sentence";

        swampysChannel.sendMessage(simpleEmbed("Jury Summons", description)).queue(msg -> {
            CourtCase courtCase = new CourtCase(defendant.getIdLong(), accuser.getIdLong(), msg.getIdLong(), finalCrime, LocalDateTime.now().plusMinutes(15));
            courtCaseRepository.save(courtCase);

            msg.addReaction(Sentence.ACQUIT.getEmoji()).queue();
            msg.addReaction(Sentence.ONE_HOUR.getEmoji()).queue();
            msg.addReaction(Sentence.FIVE_HOUR.getEmoji()).queue();
            msg.addReaction(Sentence.ONE_DAY.getEmoji()).queue();
        });
    }
}
