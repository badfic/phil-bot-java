package com.badfic.philbot.commands.bang;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.Constants;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
class PfpCommand extends BaseBangCommand {
    PfpCommand() {
        name = "pfp";
        help = "`!!pfp @user`\n" +
                "show that user's profile picture, large";
    }

    @Override
    public void execute(final CommandEvent event) {
        var member = event.getMember();
        final var mentionedMembers = event.getMessage().getMentions().getMembers();
        if (CollectionUtils.isNotEmpty(mentionedMembers)) {
            member = mentionedMembers.getFirst();
        }

        event.reply(Constants.simpleEmbed(member.getEffectiveName(), null, member.getEffectiveAvatarUrl()));
    }
}
