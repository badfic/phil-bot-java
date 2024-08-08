package com.badfic.philbot.commands.bang;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.service.Ao3MetadataParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
class NsfwFicRec extends BaseBangCommand {

    private final Ao3MetadataParser ao3MetadataParser;

    NsfwFicRec(final Ao3MetadataParser ao3MetadataParser) {
        name = "nsfwficrec";
        aliases = new String[] {"swampyficrec"};
        help = "`!!nsfwficrec https://example.com` Sends this fic recommendation to the nsfw-fic-recs channel for you.";
        this.ao3MetadataParser = ao3MetadataParser;
    }

    @Override
    public void execute(final CommandEvent event) {
        if (StringUtils.isBlank(event.getArgs())) {
            event.replyError("Please specify a link. Example `!!nsfwficrec https://example.com`");
            return;
        }

        event.getGuild().getTextChannelsByName("nsfw-fic-recs", false).stream().findAny().ifPresent(textChannel -> {
            final var link = event.getArgs().trim();

            textChannel.sendMessage(event.getAuthor().getAsMention() + " recommended this fic: " + link).queue();
            if (StringUtils.containsIgnoreCase(link, "archiveofourown")) {
                ao3MetadataParser.parseLink(link, "nsfw-fic-recs");
            }
        });

        event.getMessage().delete().queue();
    }
}
