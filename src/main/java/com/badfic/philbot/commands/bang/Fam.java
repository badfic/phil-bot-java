package com.badfic.philbot.commands.bang;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.ModHelpAware;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.Family;
import java.awt.Color;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

@Component
class Fam extends BaseBangCommand implements ModHelpAware {

    @Getter
    private final String modHelp;

    Fam() {
        name = "fam";
        help = """
                !!fam
                Note for all the propose/divorce/adopt/disown commands,
                You can @ somebody or you can just type in any name if they are not on this server

                `!!fam`: Show your fam
                `!!fam show @Santiago`: Show somebody else's fam
                `!!fam image image-url-here`: Change the featured image for your "fam show" command
                `!!fam tag there's a boost`: Tags all your fam, telling them "there's a boost"
                `!!fam propose Somebody`: Add a spouse
                `!!fam divorce Somebody`: Remove a spouse
                `!!fam add ex Somebody`: Add an ex
                `!!fam remove ex Somebody`: Remove an ex
                `!!fam adopt child Somebody`: Add a child
                `!!fam disown child Somebody`: Remove a child
                `!!fam adopt grandchild Somebody`: Add a grandchild
                `!!fam disown grandchild Somebody`: Remove a grandchild
                `!!fam adopt parent Somebody`: Add a parent
                `!!fam disown parent Somebody`: Remove a parent
                `!!fam adopt grandparent Somebody`: Add a grandparent
                `!!fam disown grandparent Somebody`: Remove a grandparent
                `!!fam adopt cousin Somebody`: Add a cousin
                `!!fam disown cousin Somebody`: Remove a cousin
                `!!fam adopt sibling Somebody`: Add a sibling
                `!!fam disown sibling Somebody`: Remove a sibling
                `!!fam adopt nibling Somebody`: Add a nibling
                `!!fam disown nibling Somebody`: Remove a nibling
                `!!fam adopt pibling Somebody`: Add a pibling
                `!!fam disown pibling Somebody`: Remove a pibling""";
        modHelp = help +
                "\n**MOD ONLY COMMANDS**\n" +
                "`!!fam nuke @Santiago`: Delete someone's family tree if they are misbehaving or not getting consent";
    }

    @Override
    public void execute(final CommandEvent event) {
        if (isNotEligible(event.getMember(), event)) {
            return;
        }

        final var args = event.getArgs();

        if (args.startsWith("help")) {
            if (hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
                event.replyInDm(Constants.simpleEmbed("Help", modHelp));
            } else {
                event.replyInDm(Constants.simpleEmbed("Help", help));
            }
        } else if (args.startsWith("show")) {
            show(event);
        } else if (args.startsWith("image")){
            image(event);
        } else if (args.startsWith("tag")) {
            tag(event);
        } else if (args.startsWith("nuke")) {
            nuke(event);
        } else if (args.startsWith("propose")) {
            propose(event);
        } else if (args.startsWith("divorce")) {
            divorce(event);
        } else if (args.startsWith("add ex")) {
            addEx(event);
        } else if (args.startsWith("remove ex")) {
            removeEx(event);
        } else if (args.startsWith("adopt cousin")) {
            addCousin(event);
        } else if (args.startsWith("disown cousin")) {
            removeCousin(event);
        } else if (args.startsWith("adopt child")) {
            adoptChild(event);
        } else if (args.startsWith("disown child")) {
            disownChild(event);
        } else if (args.startsWith("adopt grandchild")) {
            adoptGrandchild(event);
        } else if (args.startsWith("disown grandchild")) {
            disownGrandchild(event);
        } else if (args.startsWith("adopt parent")) {
            adoptParent(event);
        } else if (args.startsWith("disown parent")) {
            disownParent(event);
        } else if (args.startsWith("adopt grandparent")) {
            adoptGrandparent(event);
        } else if (args.startsWith("disown grandparent")) {
            disownGrandparent(event);
        } else if (args.startsWith("adopt sibling")) {
            adoptSibling(event);
        } else if (args.startsWith("disown sibling")) {
            disownSibling(event);
        } else if (args.startsWith("adopt nibling")) {
            adoptNibling(event);
        } else if (args.startsWith("disown nibling")) {
            disownNibling(event);
        } else if (args.startsWith("adopt pibling")) {
            adoptPibling(event);
        } else if (args.startsWith("disown pibling")) {
            disownPibling(event);
        } else if (args.isEmpty() || args.startsWith("<@")) {
            show(event);
        } else {
            event.replyError("unrecognized fam command");
        }
    }

    private void tag(final CommandEvent event) {
        final var msg = new StringBuilder();
        final var endOfMessage = event.getArgs().replace("tag", "");

        final var discordUser = getUserAndFamily(event.getMember());

        final var family = discordUser.getFamily();

        final var familyMentions = Stream.of(collectionToMentions(family.getSpouses()), collectionToMentions(family.getExes()),
                collectionToMentions(family.getChildren()), collectionToMentions(family.getGrandchildren()), collectionToMentions(family.getParents()),
                collectionToMentions(family.getGrandparents()), collectionToMentions(family.getSiblings()))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(" "));

        msg.append(familyMentions)
                .append(endOfMessage);

        event.reply(msg.toString());
    }

    private String collectionToMentions(final Collection<String> collection) {
        return collection.stream().filter(NumberUtils::isParsable).map(s -> "<@" + s + ">").collect(Collectors.joining(" "));
    }

    private void nuke(final CommandEvent event) {
        if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
            event.replyError(Constants.ADMIN_ROLE + " is required to use that command");
            return;
        }

        if (CollectionUtils.size(event.getMessage().getMentions().getMembers()) != 1) {
            event.replyError("Please mention a user to nuke. Example `!!fam nuke @Santiago`");
            return;
        }

        final var memberToNuke = event.getMessage().getMentions().getMembers().getFirst();
        final var discordUserToNuke = getUserAndFamily(memberToNuke);
        discordUserToNuke.setFamily(new Family());
        discordUserRepository.save(discordUserToNuke);

        event.replySuccess("Putting the 'nuke' in nuclear family: " + memberToNuke.getEffectiveName() + "'s family has been nuked.");
    }

    private void adoptGrandparent(final CommandEvent event) {
        final var discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "adopt grandparent", discordUser, d -> d.getFamily().getGrandparents(), true);
    }

    private void disownGrandparent(final CommandEvent event) {
        final var discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "disown grandparent", discordUser, d -> d.getFamily().getGrandparents(), false);
    }

    private void addEx(final CommandEvent event) {
        final var discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "add ex", discordUser, d -> d.getFamily().getExes(), true);
    }

    private void removeEx(final CommandEvent event) {
        final var discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "remove ex", discordUser, d -> d.getFamily().getExes(), false);
    }

    private void addCousin(final CommandEvent event) {
        final var discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "add cousin", discordUser, d -> d.getFamily().getCousins(), true);
    }

    private void removeCousin(final CommandEvent event) {
        final var discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "remove cousin", discordUser, d -> d.getFamily().getCousins(), false);
    }

    private void adoptParent(final CommandEvent event) {
        final var discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "adopt parent", discordUser, d -> d.getFamily().getParents(), true);
    }

    private void disownParent(final CommandEvent event) {
        final var discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "disown parent", discordUser, d -> d.getFamily().getParents(), false);
    }

    private void adoptSibling(final CommandEvent event) {
        final var discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "adopt sibling", discordUser, d -> d.getFamily().getSiblings(), true);
    }

    private void disownSibling(final CommandEvent event) {
        final var discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "disown sibling", discordUser, d -> d.getFamily().getSiblings(), false);
    }

    private void adoptNibling(final CommandEvent event) {
        final var discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "adopt nibling", discordUser, d -> d.getFamily().getNiblings(), true);
    }

    private void disownNibling(final CommandEvent event) {
        final var discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "disown nibling", discordUser, d -> d.getFamily().getNiblings(), false);
    }

    private void adoptPibling(final CommandEvent event) {
        final var discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "adopt pibling", discordUser, d -> d.getFamily().getPiblings(), true);
    }

    private void disownPibling(final CommandEvent event) {
        final var discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "disown pibling", discordUser, d -> d.getFamily().getPiblings(), false);
    }

    private void adoptGrandchild(final CommandEvent event) {
        final var discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "adopt grandchild", discordUser, d -> d.getFamily().getGrandchildren(), true);
    }

    private void disownGrandchild(final CommandEvent event) {
        final var discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "disown grandchild", discordUser, d -> d.getFamily().getGrandchildren(), false);
    }

    private void adoptChild(final CommandEvent event) {
        final var discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "adopt child", discordUser, d -> d.getFamily().getChildren(), true);
    }

    private void disownChild(final CommandEvent event) {
        final var discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "disown child", discordUser, d -> d.getFamily().getChildren(), false);
    }

    private void propose(final CommandEvent event) {
        final var discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "propose", discordUser, d -> d.getFamily().getSpouses(), true);
    }

    private void divorce(final CommandEvent event) {
        final var discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "divorce", discordUser, d -> d.getFamily().getSpouses(), false);
    }

    private void addOrRemoveFamily(final CommandEvent event, final String argName, final DiscordUser discordUser, final Function<DiscordUser, Set<String>> setGetter, final boolean add) {
        final var args = event.getArgs();
        final var set = setGetter.apply(discordUser);

        if (set == null) {
            event.replyError("Something went terribly wrong. Try again later");
            return;
        }

        if (CollectionUtils.size(event.getMessage().getMentions().getMembers()) == 1) {
            final var mentionedMember = event.getMessage().getMentions().getMembers().getFirst();

            if (mentionedMember.getUser().isBot()) {
                if (add) {
                    set.add(mentionedMember.getId());
                } else {
                    set.remove(mentionedMember.getId());
                }

                discordUserRepository.save(discordUser);
                event.replySuccess(event.getMember().getAsMention() + ", Successfully `" + argName + "`'d " + mentionedMember.getEffectiveName());
                return;
            }

            if (!add) {
                set.remove(mentionedMember.getId());
                discordUserRepository.save(discordUser);
                event.replySuccess(event.getMember().getAsMention() + ", Successfully `" + argName + "`'d " + mentionedMember.getEffectiveName());
                return;
            }

            if (set.contains(mentionedMember.getId())) {
                event.replySuccess(event.getMember().getAsMention() + ", " + mentionedMember.getEffectiveName() + " is already `" + argName + "`'d");
                return;
            }

            if (event.getGuild().getMemberById(mentionedMember.getId()) == null) {
                event.replyError("You can't add someone who is not a member of the swamp");
                return;
            }

            final var message = Constants.simpleEmbed(argName,
                    "Hello, " + mentionedMember.getAsMention() + "\n\n" + event.getMember().getAsMention() + " would like to `" + argName
                            + "` you.\nDo you accept?\n\n(You have 15 minutes to respond or else it defaults to reject)");

            event.getChannel().sendMessageEmbeds(message).queue(msg -> {
                msg.addReaction(Emoji.fromUnicode("✅")).queue();
                msg.addReaction(Emoji.fromUnicode("❌")).queue();

                Constants.addReactionTask(msg.getIdLong(), messageReactionAddEvent -> {
                    if (messageReactionAddEvent.getUserIdLong() == mentionedMember.getIdLong()) {
                        if ("✅".equals(messageReactionAddEvent.getReaction().getEmoji().getName())) {
                            final var relookupUser = discordUserRepository.findById(discordUser.getId()).orElse(null);

                            if (relookupUser == null) {
                                msg.editMessage("Something went horribly wrong. Oops, try again later.").queue();
                                return true;
                            }

                            setGetter.apply(relookupUser).add(mentionedMember.getId());
                            discordUserRepository.save(relookupUser);

                            final var messageSuccess = Constants.simpleEmbed(argName,
                                    mentionedMember.getAsMention() + " accepted " + event.getMember().getAsMention() + "'s `" + argName + '`');

                            msg.editMessageEmbeds(messageSuccess).queue();
                            return true;
                        } else if ("❌".equals(messageReactionAddEvent.getReaction().getEmoji().getName())) {
                            final var messageFail = Constants.simpleEmbed(argName,
                                    mentionedMember.getAsMention() + " rejected " + event.getMember().getAsMention() + "'s `" + argName + '`',
                                    Color.RED);

                            msg.editMessageEmbeds(messageFail).queue();
                            return true;
                        }
                    }
                    return false;
                });
            });
            return;
        }

        final var right = args.replace(argName, "").trim();
        if (StringUtils.isBlank(right)) {
            event.replyError("Please either mention a user or put a name. Example `!!fam " + argName + " Somebody`");
            return;
        }

        if (add) {
            set.add(right);
        } else {
            set.remove(right);
        }

        discordUserRepository.save(discordUser);
        event.replySuccess(event.getMember().getAsMention() + ", Successfully `" + argName + "`'d " + right);
    }

    private void image(final CommandEvent event) {
        final var member = event.getMember();
        final var image = event.getArgs().replace("image", "").trim();

        if (isNotEligible(member, event)) {
            return;
        }

        final var discordUser = getUserAndFamily(member);

        if (Constants.isUrl(image) && Constants.urlIsImage(image)) {
            discordUser.getFamily().setImage(image);
            discordUserRepository.save(discordUser);
            event.replySuccess("Successfully set image");
        } else {
            event.replyError("Could not recognize image as a url");
        }
    }

    private void show(final CommandEvent event) {
        var member = event.getMember();
        if (CollectionUtils.size(event.getMessage().getMentions().getMembers()) == 1) {
            member = event.getMessage().getMentions().getMembers().getFirst();
        }

        if (isNotEligible(member, event)) {
            return;
        }

        final var discordUser = getUserAndFamily(member);
        final var family = discordUser.getFamily();

        final var description = new StringBuilder();

        if (family.getIntro() != null) {
            description.append("**Introduction**\n")
                    .append(family.getIntro())
                    .append("\n\n");
        }

        append(family::getSpouses, "**Spouses**", description);
        append(family::getExes, "**Exes**", description);
        append(family::getChildren, "**Children**", description);
        append(family::getGrandchildren, "**Grandchildren**", description);
        append(family::getParents, "**Parents**", description);
        append(family::getGrandparents, "**Grandparents**", description);
        append(family::getSiblings, "**Siblings**", description);
        append(family::getCousins, "**Cousins**", description);
        append(family::getNiblings, "**Niblings**", description);
        append(family::getPiblings, "**Piblings**", description);

        try {
            final var msg = Constants.simpleEmbed(member.getEffectiveName() + "'s Family",
                    description.toString(),
                    Objects.nonNull(family.getImage()) ? family.getImage() : member.getEffectiveAvatarUrl());

            event.reply(msg);
        } catch (final Exception e) {
            description.append("Failed to load family member");
            event.reply(Constants.simpleEmbed(member.getEffectiveName() + "'s Family", description.toString()));
        }
    }

    private Set<Member> getMemberSet(final Set<String> set, final CommandEvent event) {
        return set.stream()
                .filter(NumberUtils::isParsable)
                .map(s -> event.getGuild().getMemberById(s))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void append(final Supplier<Set<String>> supplier, final String relation, final StringBuilder description) {
        if (CollectionUtils.isNotEmpty(supplier.get())) {
            description.append(relation).append(": ");
            final var joinedString = supplier.get().stream().map(id -> {
                if (NumberUtils.isParsable(id)) {
                    return "<@" + id + '>';
                }
                return id;
            }).collect(Collectors.joining(", "));

            description
                    .append(joinedString)
                    .append("\n");
        }
    }

    private boolean isNotEligible(final Member member, final CommandEvent event) {
        final var result = !(hasRole(member, Constants.CHAOS_CHILDREN_ROLE) || hasRole(member, Constants.EIGHTEEN_PLUS_ROLE));
        if (result) {
            event.replyError(member.getEffectiveName() + " is not 18+ or a chaos child, they cannot have a family.");
        }
        return result;
    }

    private DiscordUser getUserAndFamily(final Member member) {
        var discordUser = getDiscordUserByMember(member);
        if (discordUser.getFamily() == null) {
            discordUser.setFamily(new Family());
            discordUser = discordUserRepository.save(discordUser);
        }
        return discordUser;
    }

}
