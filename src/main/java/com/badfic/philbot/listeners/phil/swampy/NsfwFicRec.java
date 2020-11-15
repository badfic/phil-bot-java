package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.PhilMarker;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class NsfwFicRec extends BaseSwampy implements PhilMarker {
    public NsfwFicRec() {
        name = "nsfwficrec";
        aliases = new String[] {"swampyficrec"};
        help = "!!nsfwficrec\nExample `!!nsfwficrec https://example.com`";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (StringUtils.isBlank(event.getArgs())) {
            event.replyError("Please specify a link. Example `!!nsfwficrec https://example.com`");
            return;
        }

        event.getGuild().getTextChannelsByName("nsfw-fic-recs", false).stream().findAny().ifPresent(textChannel -> {
            textChannel.sendMessage(event.getAuthor().getAsMention() + " recommended this fic: " + event.getArgs()).queue();
        });

        event.getMessage().delete().queue();
    }
}
