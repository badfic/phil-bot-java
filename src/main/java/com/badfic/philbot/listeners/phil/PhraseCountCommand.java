package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.Phrase;
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
public class PhraseCountCommand extends Command implements PhilMarker {

    private final boolean isTestEnvironment;
    private final DiscordUserRepository discordUserRepository;
    private final PhraseRepository phraseRepository;

    @Autowired
    public PhraseCountCommand(DiscordUserRepository discordUserRepository, PhraseRepository phraseRepository, BaseConfig baseConfig) {
        isTestEnvironment = "test".equalsIgnoreCase(baseConfig.nodeEnvironment);
        name = "phraseCount";
        help = "Counts how many times someone has said a given word.\n" +
                "\tTo see the counts for a given user: `!!phraseCount count @user`\n" +
                "\tTo start counting the word 'peanut' for a user: `!!phraseCount add peanut @user`\n" +
                "\tTo stop counting the word 'peanut' for a user: `!!phraseCount remove peanut @user`\n";
        requiredRole = "Queens of the Castle";
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
            countPhrase(event);
        } else if (msgContent.startsWith("!!phraseCount add")) {
            addPhrase(event);
        } else if (msgContent.startsWith("!!phraseCount remove")) {
            removePhrase(event);
        } else {
            event.getChannel().sendMessage(eventAuthorMention + ", unrecognized command.").queue();
        }
    }

    private void countPhrase(CommandEvent event) {
        String eventAuthorMention = event.getAuthor().getAsMention();
        User user = event.getMessage().getMentionedUsers().get(0);
        Optional<DiscordUser> optionalUserEntity = discordUserRepository.findById(user.getId());

        if (!optionalUserEntity.isPresent()) {
            event.getChannel().sendMessage(eventAuthorMention
                    + ", that user does not have any phrases set up. Set up a phrase with: `!!phraseCount add [phrase] @user`").queue();
            return;
        }

        DiscordUser discordUser = optionalUserEntity.get();
        List<Phrase> phrases = phraseRepository.findAllByDiscordUser(discordUser);

        if (CollectionUtils.isEmpty(phrases)) {
            event.getChannel().sendMessage(eventAuthorMention
                    + ", that user does not have any phrases set up. Set up a phrase with: `!!phraseCount add [phrase] @user`").queue();
            return;
        }

        event.getChannel().sendMessageFormat("%s\n```\n%s\n```", eventAuthorMention, phrases.toString()).queue();
    }

    private void addPhrase(CommandEvent event) {
        String eventAuthorMention = event.getAuthor().getAsMention();
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

        String phraseToAdd = event.getMessage().getContentRaw().replace("!!phraseCount add ", "").replace(" " + lastArg, "");

        User user = event.getMessage().getMentionedUsers().get(0);
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

        event.getChannel().sendMessageFormat("%s, added phrase `%s` for user %s", eventAuthorMention, phraseToAdd, user.getAsMention()).queue();
    }

    private void removePhrase(CommandEvent event) {
        String eventAuthorMention = event.getAuthor().getAsMention();
        String[] items = event.getArgs().split("\\s+");

        if (items.length < 3) {
            event.getChannel().sendMessage(eventAuthorMention
                    + ", unrecognized phrase in `remove` command. Example: `!!phraseCount remove [phrase] @user`").queue();
            return;
        }

        String lastArg = items[items.length - 1];
        if (!lastArg.startsWith("<@")) {
            event.getChannel().sendMessage(eventAuthorMention
                    + ", unrecognized ordering of `remove` command, the @user must be at the end of the command. Example: " +
                    "`!!phraseCount remove [phrase] @user`")
                    .queue();
            return;
        }

        String phraseToRemove = event.getMessage().getContentRaw().replace("!!phraseCount remove ", "").replace(" " + lastArg, "");
        User user = event.getMessage().getMentionedUsers().get(0);
        List<Phrase> existingPhrases = phraseRepository.findAllByDiscordUser_id(user.getId());

        Optional<Phrase> foundPhraseEntity = existingPhrases.stream().filter(p -> p.getPhrase().equalsIgnoreCase(phraseToRemove)).findFirst();

        if (!foundPhraseEntity.isPresent()) {
            event.getChannel().sendMessageFormat("%s, unrecognized phrase in `remove` command. That user does not have [phrase=%s] registered",
                    eventAuthorMention, phraseToRemove).queue();
            return;
        }

        phraseRepository.delete(foundPhraseEntity.get());
        event.getChannel().sendMessageFormat("%s, removed [phrase=%s] for user %s", eventAuthorMention, phraseToRemove, user.getAsMention()).queue();
    }

}
