package com.badfic.philbot.data.phil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum Rank {
    CINNAMON_ROLL("The Most Precious Cinnamon Roll", 0, "https://cdn.discordapp.com/attachments/707453916882665552/751404695980867594/classic-roll_314x256.png", "Congratulations <name>! You're <rolename>, welcome to Cinnabon"),
    BEHRAD("Behrad Tarazi", 1, "https://cdn.discordapp.com/attachments/707453916882665552/751404804223401984/Legends-of-Tomorrow-Behrad.png", "Congratulations <name>! You're <rolename> who is far too cinnamon roll to be a bastard!"),
    ANDY("Andy Dwyer", 2, "https://cdn.discordapp.com/attachments/707453916882665552/751404910473510932/iu.png", "Congratulations friend! You're <rolename>! You can be a little bit of a jerk sometimes, but you always have the best intentions at heart. So you're one of the least bastard!"),
    WALLY("Wally West", 3, "https://media.discordapp.net/attachments/707453916882665552/751405067126439936/5669e1b5dd0895f8398b4579.png", "Congratulations <name>! You're <rolename> one of the least bastard and the most precious speedy boi!"),
    MONA("Mona Wu", 5, "https://cdn.discordapp.com/attachments/707453916882665552/751405210047348796/tumblr_pjl46tz0p01tyusgdo2_500.png", "Congratulations <name>! You're <rolename> one of the least bastard and a boss ass Literary Agent/Werewolf!"),
    LESLIE("Leslie Knope", 7, "https://media.discordapp.net/attachments/707453916882665552/751405335440392192/5824aa9046e27a1c008b5eec.png?width=902&height=677", "Hooray! You're <rolename>! One of the best, most driven people to ever walk the earth. Does your ambition sometimes take over and make you seem like a bit of a bastard? Who knows? But you always do right by your friends!"),
    KENDRA("Kendra Saunders", 8, "https://media.discordapp.net/attachments/707453916882665552/751405470232608868/latest.png?width=1204&height=677", "Nice! Congratulations <name>! You're <rolename> one of the least bastard and you were totally **JUST** a barista before you became a *Furry* Hawk Goddess."),
    AMAYA("Amaya Jiwe", 10, "https://media.discordapp.net/attachments/707453916882665552/751405594916814848/amaya_jiwe-legends.png?width=1204&height=677", "Congratulations <name>i! You're <rolename>, the mother of all Vixens and a badass totem master who is constantly the voice of reason."),
    CISCO("Cisco Ramon", 11, "https://media.discordapp.net/attachments/707453916882665552/751405841189568592/05_01_The_Flash_S06-b61d54a-scaled.png", "Congratulations <name> You're <rolename> Badass hacker, serial monogamist, genius computer boi, and all around medium level bastard!"),
    BEN("Ben Wyatt", 13, "https://cdn.discordapp.com/attachments/707453916882665552/751406062065942569/ben-wyatt-parks-and-recreation-0df68e7a-6d3e-4c18-bb26-9707a4abc77-resize-750.png", "It's all about the cones <name>! That's right, you're now bastard enough to be <rolename>! You're tired all the time and you just want to do you job, but sometimes the antics of those around you get in your way so you make some bullshit Claymation videos and cry into your cone game. Get rekt, I guess?"),
    ANNE("Anne Perkins", 15, "https://cdn.discordapp.com/attachments/707453916882665552/751406209172766770/450.png", "Congratulations <name>you beautiful sunfish! You're now <rolename>! You don't always do the right thing and your meddling can get in the way and mess up some of your friendships, but at the end of the day, you know what's important and how to hold onto it. You're also a nurse which is really cool! Stay safe out there!"),
    CHARLIE("Charlie", 17, "https://cdn.discordapp.com/attachments/707453916882665552/751406328554979338/340.png", "Congratulations <name>! You're now bastard enough to be <rolename>. Stay slutty, crispy, and shapeshiftery, you boss ass immortal goddess of fate."),
    JAX("Jefferson \"Jax\" Jackson", 18, "https://cdn.discordapp.com/attachments/707453916882665552/751584618704142446/iu.png", "Congratulations <name>! You're now bastard enough to be <rolename>! You used to be less of a bastard, but then you started hanging out with Martin Stein."),
    RAY("Ray Palmer", 19, "https://media.discordapp.net/attachments/707453916882665552/751403347881230406/FlimsyAgonizingHippopotamus-max-1mb.png", "Congratulations <name>! You're now bastard enough to be <rolename>! Always remember that SIZE MATTERS!"),
    ZARI("Zari Tarazi", 20, "https://cdn.discordapp.com/attachments/707453916882665552/751406691093839912/latest.png", "Congratulations <name>! You're now bastard enough to be <rolename>. Enjoy being a hacker with wind powers and a bad attitude!"),
    NATE("Nate Heywood", 21, "https://cdn.discordapp.com/attachments/707453916882665552/751404402706743307/tumblr_p4ge460dE71ubfwvlo2_500.png", "Congratulations <name>! You're now bastard enough to be <rolename>! You like to screw things up for the better and/or more magical and that definitely makes you a bastard, but at least you've got a heart of gold. For the most part."),
    TOM("Tom Haverford", 22, "https://cdn.discordapp.com/attachments/707453916882665552/751406862485946439/0UdAfgfjdNZIEKS2t.png", "Treat yo' self. You're <rolename>! You can get in your own way sometimes and you don't think twice about stepping on other people to get your own way. Which sort of makes you a huge bastard, but you figured it out in the end. You know what matters most!"),
    WILL("Will Schuester", 23, "https://cdn.discordapp.com/attachments/707453916882665552/751406975769641070/latest.png", "Whoa, <name> you're bastard enough to be <rolename>. You may think your heart is in the right place, but maybe don't peep at your students in the shower?"),
    GARY("Gary Green", 24, "https://cdn.discordapp.com/attachments/707453916882665552/751404552833597440/360.png", "You're now bastard enough to be <rolename>. You've got 3 nipples, a fluffy rabbit with IBS, and you once tried to Nip-Notize an entire Federal Agency. So. That makes you kind of a big bastard, dude."),
    APRIL("April Ludgate", 25, "https://cdn.discordapp.com/attachments/707453916882665552/751407069705404496/360.png", "You're now bastard enough to be April Ludgate. You really like games that turn people against each other."),
    ZED("Zed Martin", 27, "https://cdn.discordapp.com/attachments/707453916882665552/751584769955069972/iu.png", "Hope you like being psychic and eating pickles, <name> because you're finally bastard enough to be <rolename>!"),
    ZARI_TOMAZ("Zari Tomaz", 28, "https://cdn.discordapp.com/attachments/707453916882665552/751403249394909184/LGN416a_0292br.png", "Congratulations <name>! You're now bastard enough to be <rolename>. Enjoy being a hacker with wind powers and a bad attitude!"),
    AVA("Ava Sharpe", 29, "https://cdn.discordapp.com/attachments/707453916882665552/751403249394909184/LGN416a_0292br.png", "Congratulations <name>! You're now bastard enough to be <rolename>. You're a lesbian who can kick ass in fights and likes spinning swords around high level bastard style."),
    MARTIN("Martin Stein", 30, "https://cdn.discordapp.com/attachments/707453916882665552/751403249394909184/LGN416a_0292br.png", "ASTONISHING! Congratulations <name>! You're now bastard enough to be <rolename>."),
    SARA("Sara Lance", 31, "https://cdn.discordapp.com/attachments/707453916882665552/751403249394909184/LGN416a_0292br.png", "Congratulations <name>! You're now bastard enough to be <rolename>. ***You're*** the captain now!"),
    RON("Ron Swanson", 32, "https://cdn.discordapp.com/attachments/707453916882665552/751403249394909184/LGN416a_0292br.png", "You've made it. You're now bastard enough to be <rolename>. That's it."),
    NORA("Nora Darhk", 33, "https://cdn.discordapp.com/attachments/707453916882665552/751403249394909184/LGN416a_0292br.png", "Congratulations <name>! You're now bastard enough to be <rolename>! You might've changed your ways now, but it doesn't erase the fact that you definitely tried to kill your future husband for an entire season."),
    DONNA("Donna Meagle", 34, "https://cdn.discordapp.com/attachments/707453916882665552/751403249394909184/LGN416a_0292br.png", "You're the baddest bitch in the game, the most bastard of all of the Parks and Rec gang, <rolename>! You know what you want and you know how to getting. You've been serving looks up on a silver platter since you could walk. Honestly you're the best of the best and the worst of the worst depending on the game you're playing and how you wanna play it. Stay frosty you boss ass bitch."),
    ASTRA("Astra Logue", 35, "https://cdn.discordapp.com/attachments/707453916882665552/751403249394909184/LGN416a_0292br.png", "Congratulations <name>! You're now bastard enough to be <rolename>. Just because you've clawed your way back out of hell, doesn't make you any less of a bastard. Even if we do love you and your tall queer energy."),
    MAZE("Mazikeen \"Maze\" Smith", 37, "https://cdn.discordapp.com/attachments/707453916882665552/751403249394909184/LGN416a_0292br.png", "Guess what, <name>! You’re <rolename>! Go forth and be hot and demonic."),
    FITZROY("Fitzroy Maplecourt", 38, "https://cdn.discordapp.com/attachments/707453916882665552/751403249394909184/LGN416a_0292br.png", "Congratulations <name>, you're now bastard enough to be <rolename> a knight in absentia of the realm of goodcastle. He is a half-elf barbarian sorceror from The Adventure Zone. He is a villain-in-training in possession of wild magic and has a love for sweet crepes."),
    GIDEON("Gideon", 39, "https://cdn.discordapp.com/attachments/707453916882665552/751403249394909184/LGN416a_0292br.png", "Congratulations <name>! You're now bastard enough to be <rolename>. You might have other people fooled with your sexy voice and your helpful quips, but not us. We know you're a massive bastard and we love you anyway."),
    MICK("Mick Rory", 40, "https://cdn.discordapp.com/attachments/707453916882665552/751403249394909184/LGN416a_0292br.png", "You're now bastard enough to be <rolename>. Enjoy setting everything on fire and grunting in response to literally everything that isn't about theft and murder."),
    SNART("Leonard Snart", 41, "https://cdn.discordapp.com/attachments/707453916882665552/751403249394909184/LGN416a_0292br.png", "You're now bastard enough to be <rolename>. You're hot, you're bi, and you're a chaotic thieving mastermind. The only thing you've ever done wrong, aside from being a massive bastard and villain, is die."),
    RIP("Rip Hunter", 42, "https://cdn.discordapp.com/attachments/707453916882665552/751403249394909184/LGN416a_0292br.png", "Congratulations <name>! You're now bastard enough to be <rolename>. You used to be the captain, but now you're second fiddle on your own time ship. Gideon might still love you cos she has to, but cake can only get you so far with the crew ."),
    CONSTANTINE("John Constantine", 43, "https://cdn.discordapp.com/attachments/707453916882665552/751403249394909184/LGN416a_0292br.png", "Congratulations <name>! You're now bastard enough to be <rolename>. The most bastardy bastard you could ever be."),
    LUCIFER("Lucifer Morningstar", 44, "https://cdn.discordapp.com/attachments/707453916882665552/751403249394909184/LGN416a_0292br.png", "Congratulations <name>! You are <rolename>. You are extremely bastard, but also very charming. Also. Your accent is hot. Now tell me, what do you really desire?"),
    SUPREME_BASTARD("Super Mega Supreme Bastard", 45, "https://cdn.discordapp.com/attachments/707453916882665552/751403249394909184/LGN416a_0292br.png", "Congratulations <name>! You're now bastard enough to be <rolename>. I don't know how to say this, but you've somehow become more of a bastard than John \"Bastard is my Whole Deal\" Constantine. Are you okay? Do you need us to call someone?"),
    SCORPIONS("Scorpions", 100, "https://cdn.discordapp.com/attachments/707453916882665552/751403249394909184/LGN416a_0292br.png", "I have no idea how you made it this far to this role, but congrats? You're bastard enough to be the worst thing on planet eart. A <rolename>. Please reevaluate your entire life.");

    public static final long LVL_MULTIPLIER = 3560;
    private static final Map<Long, Rank> LEVEL_MAP = new HashMap<>();
    private static final Set<String> LEVEL_ROLE_NAMES = new HashSet<>();

    static {
        for (Rank value : values()) {
            LEVEL_MAP.put(value.getLevel(), value);
            LEVEL_ROLE_NAMES.add(value.getRoleName());
        }
    }

    private final String roleName;
    private final long level;
    private final String rankUpImage;
    private final String rankUpMessage;

    Rank(String roleName, long level, String rankUpImage, String rankupMessage) {
        this.roleName = roleName;
        this.level = level;
        this.rankUpImage = rankUpImage;
        this.rankUpMessage = rankupMessage;
    }

    public String getRoleName() {
        return roleName;
    }

    public long getLevel() {
        return level;
    }

    public String getRankUpImage() {
        return rankUpImage;
    }

    public String getRankUpMessage() {
        return rankUpMessage;
    }

    public static Rank byXp(long xp) {
        long level = xp / LVL_MULTIPLIER;

        for (long i = level; i > 0; i--) {
            if (LEVEL_MAP.containsKey(i)) {
                return LEVEL_MAP.get(i);
            }
        }

        return Rank.CINNAMON_ROLL;
    }

    public static Set<String> getAllRoleNames() {
        return LEVEL_ROLE_NAMES;
    }

}
