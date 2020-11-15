package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.PhilMarker;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class SfwFicRec extends BaseSwampy implements PhilMarker {
    public SfwFicRec() {
        name = "sfwficrec";
        aliases = new String[] {"safeficrec"};
        help = "!!sfwficrec\nExample `!!sfwficrec https://example.com`";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (StringUtils.isBlank(event.getArgs())) {
            event.replyError("Please specify a link. Example `!!sfwficrec https://example.com`");
            return;
        }

        event.getGuild().getTextChannelsByName("fic-recs", false).stream().findAny().ifPresent(textChannel -> {
            textChannel.sendMessage(event.getAuthor().getAsMention() + " recommended this fic: " + event.getArgs()).queue();
        });

        event.getMessage().delete().queue();
    }
}
