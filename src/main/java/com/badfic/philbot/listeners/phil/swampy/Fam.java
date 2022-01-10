package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.Family;
import com.badfic.philbot.listeners.phil.PhilMessageListener;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.awt.Color;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

@Component
public class Fam extends BaseSwampy {

    private final String modHelp;

    public Fam() {
        name = "fam";
        help = """
                !!fam
                Note for all the propose/divorce/adopt/disown commands,
                You can @ somebody or you can just type in any name if they are not on this server

                `!!fam`: Show your fam
                `!!fam show @Santiago`: Show somebody else's fam
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
    protected void execute(CommandEvent event) {
        if (isNotEligible(event.getMember(), event)) {
            return;
        }

        String args = event.getArgs();

        if (args.startsWith("help")) {
            if (hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
                event.replyInDm(Constants.simpleEmbed("Help", modHelp));
            } else {
                event.replyInDm(Constants.simpleEmbed("Help", help));
            }
        } else if (args.startsWith("show")) {
            show(event);
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
        } else if (args.isEmpty() || args.startsWith("<@!")) {
            show(event);
        } else {
            event.replyError("unrecognized fam command");
        }
    }

    private void tag(CommandEvent event) {
        StringBuilder msg = new StringBuilder();
        String endOfMessage = event.getArgs().replace("tag", "");

        DiscordUser discordUser = getUserAndFamily(event.getMember());

        Family family = discordUser.getFamily();

        String familyMentions = Stream.of(collectionToMentions(family.getSpouses()), collectionToMentions(family.getExes()),
                collectionToMentions(family.getChildren()), collectionToMentions(family.getGrandchildren()), collectionToMentions(family.getParents()),
                collectionToMentions(family.getGrandparents()), collectionToMentions(family.getSiblings()))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(" "));

        msg.append(familyMentions)
                .append(endOfMessage);

        event.reply(msg.toString());
    }

    private String collectionToMentions(Collection<String> collection) {
        return collection.stream().filter(NumberUtils::isParsable).map(s -> "<@!" + s + ">").collect(Collectors.joining(" "));
    }

    private void nuke(CommandEvent event) {
        if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
            event.replyError(Constants.ADMIN_ROLE + " is required to use that command");
            return;
        }

        if (CollectionUtils.size(event.getMessage().getMentionedMembers()) != 1) {
            event.replyError("Please mention a user to nuke. Example `!!fam nuke @Santiago`");
            return;
        }

        Member memberToNuke = event.getMessage().getMentionedMembers().get(0);
        DiscordUser discordUserToNuke = getUserAndFamily(memberToNuke);
        discordUserToNuke.setFamily(new Family());
        discordUserRepository.save(discordUserToNuke);

        event.replySuccess("Putting the 'nuke' in nuclear family: " + memberToNuke.getEffectiveName() + "'s family has been nuked.");
    }

    private void adoptGrandparent(CommandEvent event) {
        DiscordUser discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "adopt grandparent", discordUser, "getGrandparents", true);
    }

    private void disownGrandparent(CommandEvent event) {
        DiscordUser discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "disown grandparent", discordUser, "getGrandparents", false);
    }

    private void addEx(CommandEvent event) {
        DiscordUser discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "add ex", discordUser, "getExes", true);
    }

    private void removeEx(CommandEvent event) {
        DiscordUser discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "remove ex", discordUser, "getExes", false);
    }

    private void addCousin(CommandEvent event) {
        DiscordUser discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "add cousin", discordUser, "getCousins", true);
    }

    private void removeCousin(CommandEvent event) {
        DiscordUser discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "remove cousin", discordUser, "getCousins", false);
    }

    private void adoptParent(CommandEvent event) {
        DiscordUser discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "adopt parent", discordUser, "getParents", true);
    }

    private void disownParent(CommandEvent event) {
        DiscordUser discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "disown parent", discordUser, "getParents", false);
    }

    private void adoptSibling(CommandEvent event) {
        DiscordUser discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "adopt sibling", discordUser, "getSiblings", true);
    }

    private void disownSibling(CommandEvent event) {
        DiscordUser discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "disown sibling", discordUser, "getSiblings", false);
    }

    private void adoptNibling(CommandEvent event) {
        DiscordUser discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "adopt nibling", discordUser, "getNiblings", true);
    }

    private void disownNibling(CommandEvent event) {
        DiscordUser discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "disown nibling", discordUser, "getNiblings", false);
    }

    private void adoptPibling(CommandEvent event) {
        DiscordUser discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "adopt pibling", discordUser, "getPiblings", true);
    }

    private void disownPibling(CommandEvent event) {
        DiscordUser discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "disown pibling", discordUser, "getPiblings", false);
    }

    private void adoptGrandchild(CommandEvent event) {
        DiscordUser discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "adopt grandchild", discordUser, "getGrandchildren", true);
    }

    private void disownGrandchild(CommandEvent event) {
        DiscordUser discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "disown grandchild", discordUser, "getGrandchildren", false);
    }

    private void adoptChild(CommandEvent event) {
        DiscordUser discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "adopt child", discordUser, "getChildren", true);
    }

    private void disownChild(CommandEvent event) {
        DiscordUser discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "disown child", discordUser, "getChildren", false);
    }

    private void propose(CommandEvent event) {
        DiscordUser discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "propose", discordUser, "getSpouses", true);
    }

    private void divorce(CommandEvent event) {
        DiscordUser discordUser = getUserAndFamily(event.getMember());
        addOrRemoveFamily(event, "divorce", discordUser, "getSpouses", false);
    }

    private void addOrRemoveFamily(CommandEvent event, String argName, DiscordUser discordUser, String methodNameForSet, boolean add) {
        String args = event.getArgs();
        Method method = ReflectionUtils.findMethod(Family.class, methodNameForSet);
        //noinspection unchecked
        Set<String> set = (Set<String>) ReflectionUtils.invokeMethod(method, discordUser.getFamily());

        if (set == null) {
            event.replyError("Something went terribly wrong. Try again later");
            return;
        }

        if (CollectionUtils.size(event.getMessage().getMentionedUsers()) == 1) {
            User mentionedMember = event.getMessage().getMentionedUsers().get(0);

            if (mentionedMember.isBot()) {
                if (add) {
                    set.add(mentionedMember.getId());
                } else {
                    set.remove(mentionedMember.getId());
                }

                discordUserRepository.save(discordUser);
                event.replySuccess(event.getMember().getAsMention() + ", Successfully `" + argName + "`'d " + mentionedMember.getName());
                return;
            }

            if (!add) {
                set.remove(mentionedMember.getId());
                discordUserRepository.save(discordUser);
                event.replySuccess(event.getMember().getAsMention() + ", Successfully `" + argName + "`'d " + mentionedMember.getName());
                return;
            }

            if (set.contains(mentionedMember.getId())) {
                event.replySuccess(event.getMember().getAsMention() + ", " + mentionedMember.getName() + " is already `" + argName + "`'d");
                return;
            }

            if (event.getGuild().getMemberById(mentionedMember.getId()) == null) {
                event.replyError("You can't add someone who is not a member of the swamp");
                return;
            }

            MessageEmbed message = Constants.simpleEmbed(argName,
                    "Hello, " + mentionedMember.getAsMention() + "\n\n" + event.getMember().getAsMention() + " would like to `" + argName
                            + "` you.\nDo you accept?\n\n(You have 15 minutes to respond or else it defaults to reject)",
                    Constants.SWAMP_GREEN);

            event.getChannel().sendMessageEmbeds(message).queue(msg -> {
                msg.addReaction("✅").queue();
                msg.addReaction("❌").queue();

                PhilMessageListener.addReactionTask(msg.getId(), messageReactionAddEvent -> {
                    if (messageReactionAddEvent.getUserId().equalsIgnoreCase(mentionedMember.getId())) {
                        if ("✅".equals(messageReactionAddEvent.getReactionEmote().getName())) {
                            DiscordUser relookupUser = discordUserRepository.findById(discordUser.getId()).orElse(null);

                            if (relookupUser == null) {
                                msg.editMessage("Something went horribly wrong. Oops, try again later.").queue();
                                return true;
                            }

                            //noinspection unchecked
                            ((Set<String>) ReflectionUtils.invokeMethod(method, relookupUser.getFamily())).add(mentionedMember.getId());
                            discordUserRepository.save(relookupUser);

                            MessageEmbed messageSuccess = Constants.simpleEmbed(argName,
                                    mentionedMember.getAsMention() + " accepted " + event.getMember().getAsMention() + "'s `" + argName + '`',
                                    Constants.SWAMP_GREEN);

                            msg.editMessageEmbeds(messageSuccess).queue();
                            return true;
                        } else if ("❌".equals(messageReactionAddEvent.getReactionEmote().getName())) {
                            MessageEmbed messageFail = Constants.simpleEmbed(argName,
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

        String right = args.replace(argName, "").trim();
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

    private void show(CommandEvent event) {
        Member member = event.getMember();
        if (CollectionUtils.size(event.getMessage().getMentionedMembers()) == 1) {
            member = event.getMessage().getMentionedMembers().get(0);
        }

        if (isNotEligible(member, event)) {
            return;
        }

        DiscordUser discordUser = getUserAndFamily(member);
        Family family = discordUser.getFamily();

        StringBuilder description = new StringBuilder();

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
        description.append("\n\nRandom family member spotlight: ");

        try {
            Member familyMember = getRandomFamilyMember(family, event, member);
            description.append(familyMember.getAsMention());
            MessageEmbed msg = Constants.simpleEmbed(member.getEffectiveName() + "'s Family",
                    description.toString(),
                    familyMember.getEffectiveAvatarUrl());

            event.reply(msg);
        } catch (Exception e) {
            description.append("Failed to load family member");
            event.reply(Constants.simpleEmbed(member.getEffectiveName() + "'s Family", description.toString()));
        }
    }

    private Member getRandomFamilyMember(Family family, CommandEvent event, Member rootMember) {
        Set<Member> allMembers = new HashSet<>();
        allMembers.add(rootMember);
        allMembers.addAll(getMemberSet(family.getSpouses(), event));
        allMembers.addAll(getMemberSet(family.getExes(), event));
        allMembers.addAll(getMemberSet(family.getChildren(), event));
        allMembers.addAll(getMemberSet(family.getGrandchildren(), event));
        allMembers.addAll(getMemberSet(family.getParents(), event));
        allMembers.addAll(getMemberSet(family.getGrandparents(), event));
        allMembers.addAll(getMemberSet(family.getSiblings(), event));
        allMembers.addAll(getMemberSet(family.getCousins(), event));

        return Constants.pickRandom(allMembers);
    }

    private Set<Member> getMemberSet(Set<String> set, CommandEvent event) {
        return set.stream()
                .filter(NumberUtils::isParsable)
                .map(s -> event.getGuild().getMemberById(s))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void append(Supplier<Set<String>> supplier, String relation, StringBuilder description) {
        if (CollectionUtils.isNotEmpty(supplier.get())) {
            description.append(relation).append(": ");
            String joinedString = supplier.get().stream().map(id -> {
                if (NumberUtils.isParsable(id)) {
                    return "<@!" + id + '>';
                }
                return id;
            }).collect(Collectors.joining(", "));

            description
                    .append(joinedString)
                    .append("\n");
        }
    }

    private boolean isNotEligible(Member member, CommandEvent event) {
        boolean result = !(hasRole(member, Constants.CHAOS_CHILDREN_ROLE) || hasRole(member, Constants.EIGHTEEN_PLUS_ROLE));
        if (result) {
            event.replyError(member.getEffectiveName() + " is not 18+ or a chaos child, they cannot have a family.");
        }
        return result;
    }

    private DiscordUser getUserAndFamily(Member member) {
        DiscordUser discordUser = getDiscordUserByMember(member);
        if (discordUser.getFamily() == null) {
            discordUser.setFamily(new Family());
            discordUser = discordUserRepository.save(discordUser);
        }
        return discordUser;
    }

}
