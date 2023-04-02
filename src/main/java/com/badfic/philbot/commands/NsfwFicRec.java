package com.badfic.philbot.commands;

import com.badfic.philbot.service.Ao3MetadataParser;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class NsfwFicRec extends BaseCommand {

    private final Ao3MetadataParser ao3MetadataParser;

    public NsfwFicRec(Ao3MetadataParser ao3MetadataParser) {
        name = "nsfwficrec";
        aliases = new String[] {"swampyficrec"};
        help = "`!!nsfwficrec https://example.com` Sends this fic recommendation to the nsfw-fic-recs channel for you.";
        this.ao3MetadataParser = ao3MetadataParser;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (StringUtils.isBlank(event.getArgs())) {
            event.replyError("Please specify a link. Example `!!nsfwficrec https://example.com`");
            return;
        }

        event.getGuild().getTextChannelsByName("nsfw-fic-recs", false).stream().findAny().ifPresent(textChannel -> {
            String link = event.getArgs().trim();

            textChannel.sendMessage(event.getAuthor().getAsMention() + " recommended this fic: " + link).queue();
            if (StringUtils.containsIgnoreCase(link, "archiveofourown")) {
                ao3MetadataParser.parseLink(link, "nsfw-fic-recs");
            }
        });

        event.getMessage().delete().queue();
    }
}
