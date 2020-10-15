package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.Family;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

@Component
public class Fam extends BaseSwampy implements PhilMarker {

    public Fam() {
        name = "fam";
        help = "!!fam\n" +
                "Note for all the propose/divorce/adopt/disown commands, " +
                "you can @ somebody or you can just type in a name if they are not currently on this server\n\n" +
                "`!!fam show`: Show your fam\n" +
                "`!!fam propose Somebody`: Add a spouse\n" +
                "`!!fam divorce Somebody`: Remove a spouse\n" +
                "`!!fam adopt child Somebody`: Add a child\n" +
                "`!!fam disown child Somebody`: Remove a child\n" +
                "`!!fam adopt grandchild Somebody`: Add a grandchild\n" +
                "`!!fam disown grandchild Somebody`: Remove a grandchild\n" +
                "`!!fam adopt parent Somebody`: Add a parent\n" +
                "`!!fam disown parent Somebody`: Remove a parent\n" +
                "`!!fam adopt sibling Somebody`: Add a sibling\n" +
                "`!!fam disown sibling Somebody`: Remove a sibling\n";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (isNotEligible(event.getMember(), event)) {
            return;
        }

        String args = event.getArgs();

        if (args.startsWith("help")) {
            event.replyInDm(simpleEmbed("Help", help));
        } else if (args.startsWith("show")) {
            show(event);
        } else if (args.startsWith("propose")) {
            propose(event);
        } else if (args.startsWith("divorce")) {
            divorce(event);
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
        } else if (args.isEmpty()) {
            show(event);
        } else {
            event.replyError("unrecognized fam command");
        }
    }

    private void adoptGrandparent(CommandEvent event) {
        DiscordUser discordUser = getDiscordUserByMember(event.getMember());
        addOrRemove(event, "adopt grandparent", discordUser, discordUser.getFamily().getGrandparents(), true);
    }

    private void disownGrandparent(CommandEvent event) {
        DiscordUser discordUser = getDiscordUserByMember(event.getMember());
        addOrRemove(event, "disown grandparent", discordUser, discordUser.getFamily().getGrandparents(), false);
    }

    private void adoptParent(CommandEvent event) {
        DiscordUser discordUser = getDiscordUserByMember(event.getMember());
        addOrRemove(event, "adopt parent", discordUser, discordUser.getFamily().getParents(), true);
    }

    private void disownParent(CommandEvent event) {
        DiscordUser discordUser = getDiscordUserByMember(event.getMember());
        addOrRemove(event, "disown parent", discordUser, discordUser.getFamily().getParents(), false);
    }

    private void adoptSibling(CommandEvent event) {
        DiscordUser discordUser = getDiscordUserByMember(event.getMember());
        addOrRemove(event, "adopt sibling", discordUser, discordUser.getFamily().getSiblings(), true);
    }

    private void disownSibling(CommandEvent event) {
        DiscordUser discordUser = getDiscordUserByMember(event.getMember());
        addOrRemove(event, "disown sibling", discordUser, discordUser.getFamily().getSiblings(), false);
    }

    private void adoptGrandchild(CommandEvent event) {
        DiscordUser discordUser = getDiscordUserByMember(event.getMember());
        addOrRemove(event, "adopt grandchild", discordUser, discordUser.getFamily().getGrandchildren(), true);
    }

    private void disownGrandchild(CommandEvent event) {
        DiscordUser discordUser = getDiscordUserByMember(event.getMember());
        addOrRemove(event, "disown grandchild", discordUser, discordUser.getFamily().getGrandchildren(), false);
    }

    private void adoptChild(CommandEvent event) {
        DiscordUser discordUser = getDiscordUserByMember(event.getMember());
        addOrRemove(event, "adopt child", discordUser, discordUser.getFamily().getChildren(), true);
    }

    private void disownChild(CommandEvent event) {
        DiscordUser discordUser = getDiscordUserByMember(event.getMember());
        addOrRemove(event, "disown child", discordUser, discordUser.getFamily().getChildren(), false);
    }

    private void propose(CommandEvent event) {
        DiscordUser discordUser = getDiscordUserByMember(event.getMember());
        addOrRemove(event, "propose", discordUser, discordUser.getFamily().getSpouses(), true);
    }

    private void divorce(CommandEvent event) {
        DiscordUser discordUser = getDiscordUserByMember(event.getMember());
        addOrRemove(event, "divorce", discordUser, discordUser.getFamily().getSpouses(), false);
    }

    private void addOrRemove(CommandEvent event, String argName, DiscordUser discordUser, Set<String> set, boolean add) {
        String args = event.getArgs();
        String right = args.replace(argName, "").trim();
        if (CollectionUtils.size(event.getMessage().getMentionedMembers()) == 1) {
            Member mentionedMember = event.getMessage().getMentionedMembers().get(0);
            if (isNotEligible(mentionedMember, event)) {
                return;
            }
            right = mentionedMember.getId();
        }

        if (StringUtils.isBlank(right)) {
            event.replyError("Please either mention a user or put a name. Example `!!fam " + argName + " Somebody`");
            return;
        }

        if (discordUser.getFamily() == null) {
            discordUser.setFamily(new Family());
        }

        if (add) {
            set.add(right);
        } else {
            set.remove(right);
        }

        discordUserRepository.save(discordUser);
        event.replySuccess(event.getMember().getAsMention() + ", Successfully `" + argName + "`'d " + right);
    }

    private boolean isNotEligible(Member member, CommandEvent event) {
        boolean result = !(hasRole(member, Constants.CHAOS_CHILDREN_ROLE) || hasRole(member, Constants.EIGHTEEN_PLUS_ROLE));
        if (result) {
            event.replyError(member.getEffectiveName() + " is not 18+ or a chaos child, they cannot have a family.");
        }
        return result;
    }

    private void show(CommandEvent event) {
        Member member = event.getMember();
        if (CollectionUtils.size(event.getMessage().getMentionedMembers()) == 1) {
            member = event.getMessage().getMentionedMembers().get(0);
        }

        if (isNotEligible(member, event)) {
            return;
        }

        DiscordUser discordUser = getDiscordUserByMember(member);

        if (discordUser.getFamily() == null) {
            discordUser.setFamily(new Family());
            discordUserRepository.save(discordUser);
        }

        Family family = discordUser.getFamily();

        StringBuilder description = new StringBuilder();

        append(family::getSpouses, "**Spouses**", description);
        append(family::getChildren, "**Children**", description);
        append(family::getGrandchildren, "**Grandchildren**", description);
        append(family::getParents, "**Parents**", description);
        append(family::getGrandparents, "**Grandparents**", description);
        append(family::getSiblings, "**Siblings**", description);

        event.reply(simpleEmbed(member.getEffectiveName() + "'s Family", description.toString()));
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
}
