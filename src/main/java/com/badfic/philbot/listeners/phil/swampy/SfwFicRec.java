package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.service.Ao3MetadataParser;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class SfwFicRec extends BaseSwampy {

    private final Ao3MetadataParser ao3MetadataParser;

    public SfwFicRec(Ao3MetadataParser ao3MetadataParser) {
        name = "sfwficrec";
        aliases = new String[] {"safeficrec"};
        help = "`!!sfwficrec https://example.com` Sends this fic recommendation to the fic-recs channel for you.";
        this.ao3MetadataParser = ao3MetadataParser;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (StringUtils.isBlank(event.getArgs())) {
            event.replyError("Please specify a link. Example `!!sfwficrec https://example.com`");
            return;
        }

        event.getGuild().getTextChannelsByName("fic-recs", false).stream().findAny().ifPresent(textChannel -> {
            String link = event.getArgs().trim();

            textChannel.sendMessage(event.getAuthor().getAsMention() + " recommended this fic: " + link).queue();
            if (StringUtils.containsIgnoreCase(link, "archiveofourown")) {
                ao3MetadataParser.parseLink(link, "fic-recs");
            }
        });

        event.getMessage().delete().queue();
    }
}
