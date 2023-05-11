package com.badfic.philbot.commands;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.Constants;
import java.util.List;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
public class PfpCommand extends BaseNormalCommand {

    public PfpCommand() {
        name = "pfp";
        help = "`!!pfp @user`\n" +
                "show that user's profile picture, large";
    }

    @Override
    public void execute(CommandEvent event) {
        Member member = event.getMember();
        List<Member> mentionedMembers = event.getMessage().getMentions().getMembers();
        if (CollectionUtils.isNotEmpty(mentionedMembers)) {
            member = mentionedMembers.get(0);
        }

        event.reply(Constants.simpleEmbed(member.getEffectiveName(), null, member.getEffectiveAvatarUrl()));
    }

}
