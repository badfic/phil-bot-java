package com.badfic.philbot.data.phil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum Rank {
    CINNAMON_ROLL("The Most Precious Cinnamon Roll", 0, "https://cdn.discordapp.com/attachments/323666308107599872/751748921063243846/cinnamon_roll.png", "Congratulations <name>! You're <rolename>, welcome to Cinnabon"),
    TWYLA("Twyla Sands", 1, "https://cdn.discordapp.com/attachments/323666308107599872/765465701140922398/Twyla_Sands.png", "Welcome to Café Tropical <name>! You're <rolename>, one of the nicest most cinnamony cinnamon rolls in the known universe."),
    PATRICK("Patrick Brewer", 2, "https://cdn.discordapp.com/attachments/323666308107599872/765465691151138826/patrick_brewer.png", "Grab your guitar <name>! You're <rolename>, a precious pastry with a penchant for serenading the people you love."),
    BEHRAD("Behrad Tarazi", 3, "https://cdn.discordapp.com/attachments/323666308107599872/751748895448760420/behrad_tarazi.png", "Congratulations <name>! You're <rolename> a chaotic cinnamon roll with a loom anxiety."),
    YUMYAN("Yumyan Hammerpaw", 4, "https://cdn.discordapp.com/attachments/741113400574345256/767590923415715850/yumyan_hammerpaw.png", "You own us all <name>! You're the absolute legend <rolename>, stalker of butterflies, father of fleas, and the great leader of the Timbercats."),
    ANDY("Andy Dwyer", 5, "https://cdn.discordapp.com/attachments/323666308107599872/751748890377846864/andy_dwyer.png", "Congratulations <name>! You're <rolename>! You can be a little bit of a jerk sometimes, but you've got a heart of gold and some serious karate skills."),
    WALLY("Wally West", 7, "https://cdn.discordapp.com/attachments/323666308107599872/751748936548483113/wally_west.png", "Congratulations <name>! You're <rolename>. You love a good prank, you've got a need for speed, and a nose for chaos!"),
    YOLANDA("Yolanda Montez", 8, "https://cdn.discordapp.com/attachments/323666308107599872/757390634570088448/yolanda_montez.png", "Congratulations <name>! You're <rolename>. You are a fierce protector of your friends, an excellent fighter, and an even better Wild Cat. Even though you mess up sometimes and you haven't had the best go of it lately, you've still got a good heart and good intentions. Stay wild, you chaotic child."),
    MONA("Mona Wu", 9, "https://cdn.discordapp.com/attachments/323666308107599872/751748946002444308/mona_wu.png", "Congratulations <name>! You're <rolename>, the best and most chaotic triple threat! If being a triple threat means you're a Literary Agent / Kaupe / Novelist."),
    LESLIE("Leslie Knope", 10, "https://cdn.discordapp.com/attachments/323666308107599872/751748935457964082/leslie_knope.png", "Hooray! <name> You're <rolename>! One of the best, most driven people to ever walk the earth. Does your ambition sometimes take over and make you seem chaotic? Who knows? But you always do right by your friends!"),
    KIM("Kim Seokjin", 11, "https://cdn.discordapp.com/attachments/323666308107599872/752737980929540136/kim_seokjin.png", "축하합니다, <name>! You’re <rolename>! A world-wide handsome pun-slinging 10 in a crowd of 8’s. Too busy taking care of your dongsaengs to be too chaotic."),
    KENDRA("Kendra Saunders", 12, "https://cdn.discordapp.com/attachments/323666308107599872/751748942185627648/kendra_saunders.png", "Nice! Congratulations <name>! You're <rolename> and you were *totally* just a barista before you became a ~~furry~~ Hawk Goddess."),
    ESPERANZA("Esperanza \"Spooner\" Cruz", 13, "https://cdn.discordapp.com/attachments/323666308107599872/754418359998742548/esperanza_cruz.png", "Congratulations <name>! You're <rolename>. We don't know much about you yet other than your thirst for conspiracy theories and love of creating bootleg tech, but we already love you. Welcome to the Legends Family! No matter what Phil and the rest of them say, you've already made a home in our hearts."),
    AMAYA("Amaya Jiwe", 14, "https://cdn.discordapp.com/attachments/323666308107599872/751748888586616912/amaya_jiwe.png", "Congratulations <name>! You're <rolename>, the mother of all Vixens and a badass totem master who is constantly the voice of reason."),
    CISCO("Cisco Ramon", 15, "https://cdn.discordapp.com/attachments/323666308107599872/751748917741355088/cisco_ramon.png", "Congratulations <name> You're <rolename> Badass hacker, serial monogamist, genius computer boi, and full of chaotic pun-slinging energy."),
    BEN("Ben Wyatt", 17, "https://cdn.discordapp.com/attachments/323666308107599872/751748897365557389/ben_wyatt.png", "It's all about the cones <name>! That's right, you're <rolename>! You're tired all the time and you just want to do your job, but sometimes the antics of those around you get in your way so you make some bullshit Claymation videos and cry into your cone game. Get rekt, I guess?"),
    ANNE("Anne Perkins", 18, "https://cdn.discordapp.com/attachments/323666308107599872/751748893540220958/anne_perkins.png", "Congratulations <name>, you beautiful sunfish! You're now <rolename>! You don't always do the right thing and your meddling can get in the way and mess up some of your friendships, but at the end of the day, you know what's important and how to hold onto it. You're also a nurse which is really cool! Stay safe out there!"),
    CHARLIE("Charlie", 19, "https://cdn.discordapp.com/attachments/323666308107599872/751748902956564583/charlie_clotho.png", "Congratulations <name>! You're <rolename>. Stay slutty, crispy, and shapeshiftery, you boss ass immortal goddess of fate."),
    JAX("Jefferson \"Jax\" Jackson", 20, "https://cdn.discordapp.com/attachments/323666308107599872/751748923583889418/jefferson_jax_jackson.png", "Congratulations <name>! You're <rolename>. You burned your way into our hearts as one half of Firestorm and you are the only valid man in existence."),
    RAY("Ray Palmer", 21, "https://cdn.discordapp.com/attachments/323666308107599872/751748931154608137/ray_palmer.png", "Congratulations <name>! You're now <rolename>! Always remember that SIZE MATTERS!"),
    ZARI("Zari Tarazi", 22, "https://cdn.discordapp.com/attachments/323666308107599872/751956097849622598/zari_tarazi.png", "Congratulations <name>! You're <rolename>, the queen of self-help videos and makeup tutorials. You built your business from the ground up with your dragon, Mithra, by your side and became one of the most chaotic queer queens of 2044. Chaos is your brand and we absolutely love you for it."),
    NATE("Nate Heywood", 23, "https://cdn.discordapp.com/attachments/323666308107599872/751748933897683084/nate_heywood.png", "Congratulations <name>! You're <rolename>! You like to screw things up for the better and/or more magical and that definitely makes you chaotic, but at least you've got a heart of gold. For the most part."),
    ARCHIE("Archie Andrews", 24, "https://cdn.discordapp.com/attachments/741113400574345256/767590904923029524/archie_andrews.png", "You’re <rolename>, <name>! You may be a dumbass but at least you know the triumphs and defeats, the epic highs and lows of high school football. You also survived a bear attack and will not hesitate to tell everyone about it."),
    TOM("Tom Haverford", 25, "https://cdn.discordapp.com/attachments/323666308107599872/751748936401682462/tom_haverford.png", "Treat yo' self. <name> You're <rolename>! You can get in your own way sometimes and you don't think twice about stepping on other people to get your own way. Which might make you a bit of a jerk, but you figure out what matters in the end!"),
    WILL("Will Schuester", 27, "https://cdn.discordapp.com/attachments/323666308107599872/753104930088157294/will_schuester.png", "Whoa, <name> you're <rolename>. You may think your heart is in the right place, but maybe don't peep at your students in the shower?"),
    GARY("Gary Green", 28, "https://cdn.discordapp.com/attachments/323666308107599872/753104927236161616/gary_green.png", "Stop Nip-Notizing people, <name>! You're <rolename>. You've got 3 nipples, a fluffy rabbit with IBS, and you once tried to take over a government agency using one of your nipples. Which is not only chaotic, it's kind of ballsy."),
    APRIL("April Ludgate", 29, "https://cdn.discordapp.com/attachments/323666308107599872/753104925361438720/april_ludgate.png", "<name> you're now chaotic enough to be <rolename>. You really like games that turn people against each other."),
    JOHNNY("Johnny Rose", 30, "https://cdn.discordapp.com/attachments/323666308107599872/765780153841877062/Johnny_Rose.png", "Congratulations, <name>! You're <rolename>. You've made a lot of mistakes in your past, but you would do anything to protect your family. Does that make you a bit chaotic? Definitely, but you're still a mad lad who can turn around even the shittiest situation."),
    STEVIE("Stevie Budd", 31, "https://cdn.discordapp.com/attachments/323666308107599872/765465701153636352/Stevie_Budd.png", "<name>, you're <rolename>. A sarcastic hotel receptionist who doesn't take shit from anyone. You're a little bit mean and a lotta bit chaotic."),
    JUGHEAD("Jughead Jones", 32, "https://cdn.discordapp.com/attachments/741113400574345256/767590921386590208/Jughead_Jones.png", "<name>, you’re <rolename>, a self-proclaimed weirdo. In case you haven’t noticed, you’re weird. You don’t fit in and you don’t wanna fit in."),
    ZED("Zed Martin", 33, "https://cdn.discordapp.com/attachments/323666308107599872/752738026920214580/zed_martin.png", "Hope you like being psychic and eating pickles, <name> because you're <rolename>!"),
    ZARI_TOMAZ("Zari Tomaz", 34, "https://cdn.discordapp.com/attachments/323666308107599872/752738022998671471/zari_tomaz.png", "Congratulations <name>! You're <rolename>. Enjoy being a hacker with wind powers and a bad attitude!"),
    RONNIE("Ronnie Lee", 35, "https://cdn.discordapp.com/attachments/323666308107599872/765465698096119809/Ronnie_Lee.png", "Congrats <name>, you're <rolename>! You're just trying to do your job, but people keep getting in the way. You're honestly the only person who actually has their shit together."),
    AVA("Ava Sharpe", 37, "https://cdn.discordapp.com/attachments/323666308107599872/752737963590549605/ava_sharpe.png", "Congratulations <name>! You're <rolename>. You're a lesbian who can kick ass in fights and likes spinning swords around high level swampy style."),
    DAVID("David Rose", 38, "https://cdn.discordapp.com/attachments/323666308107599872/765465688235704320/David_Rose.png", "Great job <name>, you're <rolename>. A bad bitch, business owner, and all around sarcastic little shit. You try to keep everyone at arm's length because you've been hurt so often in your life, but after your time in Schitt's Creek you've become a big ol' softie."),
    MARTIN("Martin Stein", 39, "https://cdn.discordapp.com/attachments/323666308107599872/752737983731466380/marin_stein.png", "ASTONISHING! Congratulations <name>! You're <rolename>."),
    DWIGHT("Dwight Schrute", 40, "https://cdn.discordapp.com/attachments/323666308107599872/761461556663025674/dwight_schrute.png", "FACT, some species of bears build nests in trees. Grab your beets <name> because you're <rolename>, bear fact extraordinare, weapons expert, and chaotic prank master supreme."),
    ROLAND("Roland Schitt", 41, "https://cdn.discordapp.com/attachments/323666308107599872/765465697627013140/Roland_Schitt.png", "Good luck <name>, you're <rolename>. You're just here for the memes."),
    ALEXIS("Alexis Rose", 42, "https://cdn.discordapp.com/attachments/323666308107599872/765465681256644608/Alexis_Rose.png", "Oh my God, <name>, you're <rolename>. Pubic Relations Afficianado who also happens to be good at *public* relations. It took you way too long to figure yourself out, but it's fine to go at your own pace. You definitely got your shit together at the end!"),
    SARA("Sara Lance", 43, "https://cdn.discordapp.com/attachments/323666308107599872/752738017889746984/sara_lance.png", "Congratulations <name>! You get to be <rolename>. ***You're*** the captain now!"),
    MOIRA("Moira Rose", 44, "https://cdn.discordapp.com/attachments/323666308107599872/765465691926167582/Moira_Rose.png", "Felicitations and Caw caw <name>! You're <rolename>. Incredible actress and the best scrabble player in the world."),
    NYSSA("Nyssa Al Ghul", 45, "https://cdn.discordapp.com/attachments/323666308107599872/757390596561305660/nyssa_al_ghul.png", "Mission complete, <name>! You're <rolename>, heir to the dragon and all an all around badass stabby lass. You're also very, very gay."),
    MIA("Mia Corvere", 47, "https://cdn.discordapp.com/attachments/323666308107599872/754856025760464956/mia_corvere.png", "Good job, <name> you're <rolename>, an assassin who likes to stab people with your dagger made of God's bone. You have a shadow cat named Mr. Kindly and you once named a horse Bastard. Oh, and you fucked your boyfriend's killer, but it's fine because she's hot."),
    RON("Ron Swanson", 48, "https://cdn.discordapp.com/attachments/323666308107599872/752738003964919878/ron_swanson.png", "<name> you've made it. You're now <rolename>. That's it."),
    NORA("Nora Darhk", 49, "https://cdn.discordapp.com/attachments/323666308107599872/752738010377879612/nora_darhk.png", "Congratulations <name>! You're now bastard enough to be <rolename>! You might've changed your ways now, but it doesn't erase the fact that you definitely tried to kill your future husband for an entire season."),
    DONNA("Donna Meagle", 50, "https://cdn.discordapp.com/attachments/323666308107599872/752737969697194064/donna_meagle.png", "<name> you're the baddest bitch in the game, the most chaotic of all of the Parks and Rec gang, <rolename>! You know what you want and you know how to getting. You've been serving looks up on a silver platter since you could walk. Honestly you're the best of the best and the worst of the worst depending on the game you're playing and how you wanna play it. Stay frosty you boss ass bitch."),
    ASTRA("Astra Logue", 51, "https://cdn.discordapp.com/attachments/323666308107599872/752737956237934672/astra_logue.png", "Congratulations <name>! You're <rolename>. You clawed your way back from hell and we love you and your tall queer energy."),
    HUGO("Hugo \"Scarlemagne\" Oak", 52, "https://cdn.discordapp.com/attachments/741113400574345256/767590919351959562/hugo_scarlemagne_oak.png", "<name>, you're <rolename> a beast of wonder that hasn't exactly had the easiest life. You were raised by two loving and supportive parents who had to leave you behind when they got separated and from there you fell under the care of an evil doctor hell bent on using your unique abilities to control your mother. After your escape, you tried to rule over Las Vistas the only way you knew how; through fear, but that didn't last long. You realized the error of your ways just in time to save everyone."),
    CHERYL("Cheryl \"Bombshell\" Blossom", 53, "https://cdn.discordapp.com/attachments/741113400574345256/767590920354660382/cheryl_blossom.png", "You’re <rolename>, <name> you wretched child. You’re hella gay, cuckoo-bananas, and you invented the color red. You ARE red."),
    MAZE("Mazikeen \"Maze\" Smith", 54, "https://cdn.discordapp.com/attachments/323666308107599872/752737981592502343/mazikeen_maze_smith.png", "Guess what, <name>! You’re <rolename>! Go forth and be hot and demonic."),
    FITZROY("Fitzroy Maplecourt", 55, "https://cdn.discordapp.com/attachments/323666308107599872/752737963825430598/fitzroy_maplecourt.png", "Congratulations <name>, you're <rolename> a knight in absentia of the realm of goodcastle. He is a half-elf barbarian sorceror from The Adventure Zone. He is a villain-in-training in possession of wild magic and has a love for sweet crepes."),
    GIDEON("Gideon", 57, "https://cdn.discordapp.com/attachments/323666308107599872/752737971848872147/gideon.png", "Congratulations <name>! You're <rolename>. You might have other people fooled with your sexy voice and your helpful quips, but not us. We know you're a master of chaos and we love you anyway."),
    MICK("Mick Rory", 58, "https://cdn.discordapp.com/attachments/323666308107599872/752739623180042270/mick_rory.png", "<name>, you're <rolename>. Enjoy setting everything on fire and grunting in response to literally everything that isn't about theft and murder."),
    SNART("Leonard Snart", 59, "https://cdn.discordapp.com/attachments/323666308107599872/752737982859182090/leonard_snart.png", "<name>, you're <rolename>. You're hot, you're bi, and you're a chaotic thieving mastermind. The only thing you've ever done wrong, aside from being a massive bastard and villain, is die."),
    RIP("Rip Hunter", 60, "https://cdn.discordapp.com/attachments/323666308107599872/752738000470933605/rip_hunter.png", "Congratulations <name>! You're <rolename>. You used to be the captain, but now you're second fiddle on your own time ship. Gideon might still love you cos she has to, but cake can only get you so far with the crew."),
    CONSTANTINE("John Constantine", 61, "https://cdn.discordapp.com/attachments/323666308107599872/752737976970379344/john_constantine.png", "Congratulations <name>! You're now chaotic enough <rolename>. Everyone thinks you're a right arse or a bastard and you never correct them, but mate, deep down you care too much and hurt too deep."),
    WITNESS("The Witness", 62, "https://cdn.discordapp.com/attachments/741113400574345256/767590923847598100/the_witness.png", "Time has spoken, <name>, you've been revealed as <rolename>. You're a cunning bastard always one step ahead. Your motivations, much like your identity, are complicated and mysterious. Are you the world ender, or its savior? Also, you may have created the coronavirus, so thanks for that, you jerk."),
    LUCIFER("Lucifer Morningstar", 63, "https://cdn.discordapp.com/attachments/323666308107599872/752739730692636712/lucifer_morningstar.png", "Congratulations <name>! You are <rolename>. You are extremely chaotic, but also very charming. Also. Your accent is hot. Now tell me, what do you really desire?"),
    BETTY("Betty Cooper", 64, "https://cdn.discordapp.com/attachments/741113400574345256/767591124843233280/better_cooper.png", "You’re <rolename>, <name> Riverdale’s resident sleuth and Girl Next Door with a dark side. You’re the Serpent Queen, a carrier of the serial killer gene, and your mom may or may not be in an organ-harvesting cult."),
    EMILIA("Dr. Emilia", 65, "https://cdn.discordapp.com/attachments/741113400574345256/767590912544604160/dr_emilia.png", "<name>, you're <rolename> an even bigger bastard than the devil. You revel in chaos and you clung so deeply to a lie that you would kill your own brother to preserve it. Even Lucifer is side eyeing you at this point. Yikes."),
    SUPREME_BASTARD("Super Mega Supreme Bastard", 100, "https://cdn.discordapp.com/attachments/707453916882665552/751403249394909184/LGN416a_0292br.png", "Congratulations <name>! You're <rolename>. I don't know how to say this, but you've somehow become more of a chaotic swampy wench than the actual devil. Are you okay? Do you need us to call someone?");

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
