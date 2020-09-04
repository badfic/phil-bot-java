package com.badfic.philbot.data.phil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum Rank {
    CINNAMON_ROLL("The Most Precious Cinnamon Roll", 0, "https://cdn.discordapp.com/attachments/707453916882665552/751404695980867594/classic-roll_314x256.png"),
    BEHRAD("Behrad Tarazi", 1, "https://cdn.discordapp.com/attachments/707453916882665552/751404804223401984/Legends-of-Tomorrow-Behrad.png"),
    ANDY("Andy Dwyer", 2, "https://cdn.discordapp.com/attachments/707453916882665552/751404910473510932/iu.png"),
    WALLY("Wally West", 3, "https://media.discordapp.net/attachments/707453916882665552/751405067126439936/5669e1b5dd0895f8398b4579.png"),
    MONA("Mona Wu", 5, "https://cdn.discordapp.com/attachments/707453916882665552/751405210047348796/tumblr_pjl46tz0p01tyusgdo2_500.png"),
    LESLIE("Leslie Knope", 7, "https://media.discordapp.net/attachments/707453916882665552/751405335440392192/5824aa9046e27a1c008b5eec.png?width=902&height=677"),
    KENDRA("Kendra Saunders", 8, "https://media.discordapp.net/attachments/707453916882665552/751405470232608868/latest.png?width=1204&height=677"),
    AMAYA("Amaya Jiwe", 10, "https://media.discordapp.net/attachments/707453916882665552/751405594916814848/amaya_jiwe-legends.png?width=1204&height=677"),
    CISCO("Cisco Ramon", 11, "https://media.discordapp.net/attachments/707453916882665552/751405841189568592/05_01_The_Flash_S06-b61d54a-scaled.png"),
    BEN("Ben Wyatt", 13, "https://cdn.discordapp.com/attachments/707453916882665552/751406062065942569/ben-wyatt-parks-and-recreation-0df68e7a-6d3e-4c18-bb26-9707a4abc77-resize-750.png"),
    ANNE("Anne Perkins", 15, "https://cdn.discordapp.com/attachments/707453916882665552/751406209172766770/450.png"),
    CHARLIE("Charlie", 17, "https://cdn.discordapp.com/attachments/707453916882665552/751406328554979338/340.png"),
    JAX("Jefferson \"Jax\" Jackson", 18, "https://cdn.discordapp.com/attachments/707453916882665552/751404268568838174/legends-of-tomorrow-jess-macallan-maisie-richardson-sellars.png"),
    RAY("Ray Palmer", 19, "https://media.discordapp.net/attachments/707453916882665552/751403347881230406/FlimsyAgonizingHippopotamus-max-1mb.png"),
    ZARI("Zari Tarazi", 20, "https://cdn.discordapp.com/attachments/707453916882665552/751406691093839912/latest.png"),
    NATE("Nate Heywood", 21, "https://cdn.discordapp.com/attachments/707453916882665552/751404402706743307/tumblr_p4ge460dE71ubfwvlo2_500.png"),
    TOM("Tom Haverford", 22, "https://cdn.discordapp.com/attachments/707453916882665552/751406862485946439/0UdAfgfjdNZIEKS2t.png"),
    WILL("Will Schuester", 23, "https://cdn.discordapp.com/attachments/707453916882665552/751406975769641070/latest.png"),
    GARY("Gary Green", 24, "https://cdn.discordapp.com/attachments/707453916882665552/751404552833597440/360.png"),
    APRIL("April Ludgate", 25, "https://cdn.discordapp.com/attachments/707453916882665552/751407069705404496/360.png"),
    ZED("Zed Martin", 27),
    ZARI_TOMAZ("Zari Tomaz", 28),
    AVA("Ava Sharpe", 29),
    MARTIN("Martin Stein", 30),
    SARA("Sara Lance", 31),
    RON("Ron Swanson", 32),
    NORA("Nora Darhk", 33),
    DONNA("Donna Meagle", 34),
    ASTRA("Astra Logue", 35),
    MAZE("Mazikeen \"Maze\" Smith", 37),
    FITZROY("Fitzroy Maplecourt", 38),
    GIDEON("Gideon", 39),
    MICK("Mick Rory", 40),
    SNART("Leonard Snart", 41),
    RIP("Rip Hunter", 42),
    CONSTANTINE("John Constantine", 43),
    LUCIFER("Lucifer Morningstar", 44),
    SUPREME_BASTARD("Super Mega Supreme Bastard", 45),
    SCORPIONS("Scorpions", 100);

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

    Rank(String roleName, long level) {
        this(roleName, level, "https://cdn.discordapp.com/attachments/707453916882665552/751403249394909184/LGN416a_0292br.png");
    }

    Rank(String roleName, long level, String rankUpImage) {
        this.roleName = roleName;
        this.level = level;
        this.rankUpImage = rankUpImage;
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
