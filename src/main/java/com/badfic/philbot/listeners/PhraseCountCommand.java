package com.badfic.philbot.listeners;

import com.badfic.philbot.config.PhilbotAppConfig;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.Phrase;
import com.badfic.philbot.repository.DiscordUserRepository;
import com.badfic.philbot.repository.PhraseRepository;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class PhraseCountCommand extends Command {

    private final boolean isTestEnvironment;
    private final DiscordUserRepository discordUserRepository;
    private final PhraseRepository phraseRepository;

    @Autowired
    public PhraseCountCommand(DiscordUserRepository discordUserRepository, PhraseRepository phraseRepository, PhilbotAppConfig philbotAppConfig) {
        isTestEnvironment = "test".equalsIgnoreCase(philbotAppConfig.nodeEnvironment);
        name = "phraseCount";
        help = "Counts how many times someone has said a given word.\n" +
                "To see the counts for a given user: `!!phraseCount count @user`\n" +
                "To start counting the word 'peanut' for a user: `!!phraseCount add peanut @user`";
        this.discordUserRepository = discordUserRepository;
        this.phraseRepository = phraseRepository;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        if (isTestEnvironment && !"test-channel".equalsIgnoreCase(event.getChannel().getName())) {
            return;
        }

        String eventAuthorMention = event.getAuthor().getAsMention();

        List<User> mentionedUsers = event.getMessage().getMentionedUsers();

        if (CollectionUtils.isEmpty(mentionedUsers)) {
            event.getChannel().sendMessage(eventAuthorMention + ", you must mention a user. Example: `!!phraseCount count @user`").queue();
            return;
        }

        if (mentionedUsers.size() > 1) {
            event.getChannel().sendMessage(eventAuthorMention + ", I only support one user in a command. Example: `!!phraseCount count @user").queue();
            return;
        }

        String msgContent = event.getMessage().getContentRaw();
        if (msgContent.startsWith("!!phraseCount count")) {
            User user = mentionedUsers.get(0);
            Optional<DiscordUser> optionalUserEntity = discordUserRepository.findById(user.getId());

            if (!optionalUserEntity.isPresent()) {
                event.getChannel().sendMessage(eventAuthorMention
                        + ", that user does not have any phrases set up. Set up a phrase with: `!!phraseCount add [phrase] @user`").queue();
                return;
            }

            DiscordUser discordUser = optionalUserEntity.get();
            List<Phrase> phrases = phraseRepository.findAllByDiscordUser(discordUser);

            event.getChannel().sendMessage(eventAuthorMention + "```\n" + phrases.toString() + "```").queue();
        } else if (msgContent.startsWith("!!phraseCount add")) {
            String[] items = event.getArgs().split("\\s+");

            if (items.length < 3) {
                event.getChannel().sendMessage(eventAuthorMention
                        + ", unrecognized phrase in `add` command. Example: `!!phraseCount add [phrase] @user`").queue();
                return;
            }

            String lastArg = items[items.length - 1];
            if (!lastArg.startsWith("<@")) {
                event.getChannel().sendMessage(eventAuthorMention
                        + ", unrecognized ordering of `add` command, the @user must be at the end of the command. Example: `!!phraseCount add [phrase] @user`")
                        .queue();
                return;
            }

            String phraseToAdd = msgContent.replace("!!phraseCount add ", "").replace(" " + lastArg, "");

            User user = mentionedUsers.get(0);
            Optional<DiscordUser> optionalUserEntity = discordUserRepository.findById(user.getId());

            if (!optionalUserEntity.isPresent()) {
                DiscordUser newUser = new DiscordUser();
                newUser.setId(user.getId());
                optionalUserEntity = Optional.of(discordUserRepository.save(newUser));
            }

            DiscordUser userEntity = optionalUserEntity.get();

            List<Phrase> existingPhrases = phraseRepository.findAllByDiscordUser(userEntity);

            if (existingPhrases.stream().noneMatch(p -> p.getPhrase().equalsIgnoreCase(phraseToAdd))) {
                Phrase phrase = new Phrase();
                phrase.setPhrase(phraseToAdd);
                phrase.setDiscordUser(userEntity);
                phrase.setId(UUID.randomUUID());
                phraseRepository.save(phrase);
            }

            event.getChannel().sendMessage(eventAuthorMention + ", added phrase `" + phraseToAdd + "` for user " + user.getAsMention()).queue();
        } else {
            event.getChannel().sendMessage(eventAuthorMention + ", unrecognized command.").queue();
        }
    }

}
