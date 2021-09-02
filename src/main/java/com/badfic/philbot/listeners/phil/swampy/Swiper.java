package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.google.common.collect.ImmutableMap;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Swiper extends BaseSwampy {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // TODO: Investigate putting these in SwampyGamesConfig table
    public static final Map<String, TheSwiper> SWIPERS = ImmutableMap.<String, TheSwiper>builder()
            .put("Swiper No Swiping", new TheSwiper(
                    "Swiper No Swiping",
                    "Swiper Was Spotted Nearby",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882837086389026816/swiper_coming.png",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882837094685360179/swiper_lose.png",
                    "Swiper Escaped!",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882837105825443930/swiper_win.png"
            ))
            .put("Snarter No Snarting", new TheSwiper(
                    "Snarter No Snarting",
                    "Rory and Snart Were Spotted Nearby",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882905193413488650/snarter_comes.png",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882905202640945162/snarter_lose.png",
                    "Rory and Snart Escaped!",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882905213512585236/snarter_win.png"
            ))
            .put("Squidward No Squidding", new TheSwiper(
                    "Squidward No Squidding",
                    "Squidward Was Spotted Nearby",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882842774167228426/squidward_coming.png",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882842784783036467/squidward_lose.png",
                    "Squidward Squidded Away With Yo Points!",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882842796216713246/squidward_win.png"
            ))
            .put("Simper No Simping", new TheSwiper(
                    "Simper No Simping",
                    "Simps Spotted Nearby",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882898247545405460/simper_comes.png",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882898256202453022/simper_lose.png",
                    "The Simps Prospered!",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882899136859492392/simper_wins2.png"
            ))
            .put("Shrimper No Shrimping", new TheSwiper(
                    "Shrimper No Shrimping",
                    "Shrimp Spotted Nearby",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882890043411279942/shrimper_comes.png",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882890047177760818/shrimper_lose.png",
                    "The Shrimp Shrimped Away!",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882890051145588757/shrimper_wins.png"
            ))
            .put("Schuester No Schuesting", new TheSwiper(
                    "Schuester No Schuesting",
                    "Schuester Spotted Nearby",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882851218186199091/papa_coming.png",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882851229171073105/papa_lose.png",
                    "The Grinch Stole Your Soul!",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882851240315322428/papa_wins.png"
            ))
            .put("Chooster No Choosting", new TheSwiper(
                    "Chooster No Choosting",
                    "Thomas The Tank Engine Spotted",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882872146995597312/thomas_comes.png",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882872155161886730/thomas_lose.png",
                    "Sweet Sweet Murder",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882872163697324113/thomas_wins.png"
            ))
            .put("Panic No Discoing", new TheSwiper(
                    "Panic No Discoing",
                    "Brendon Urie Spotted",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882845495502962728/brendon_coming.png",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882845503979671612/brendon_lose.png",
                    "Panic! At The Crisco",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882845513215528970/brendon_win.png"
            ))
            .put("Doofen No Shmirtzing", new TheSwiper(
                    "Doofen No Shmirtzing",
                    "Dr. Doofenshmirtz Spotted Nearby!",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882885583758835732/doof_comes.png",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882885589333082132/doof_loses.png",
                    "Doofenshmirtz Evil Incorporated",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882885597927186472/doof_wins.png"
            ))
            .put("Sebastian No Stanning", new TheSwiper(
                    "Sebastian No Stanning",
                    "Sebastian Stan Spotted Nearby!",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882865239815778304/bucky_comes.png",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882865248850288670/bucky_lose.png",
                    "Get Stan'd",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882865258069385256/bucky_win.png"
            ))
            .put("Loki No Larceny", new TheSwiper(
                    "Loki No Larceny",
                    "Loki Spotted Nearby!",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882859510832709652/loki_coming.png",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882859517455515678/loki_lose.png",
                    "WOW, You just got Loki'd",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882859525512773662/loki_win.png"
            ))
            .build();

    public static final String SPIDERMAN_PNG = "https://cdn.discordapp.com/attachments/794506942906761226/883074130285588490/unknown.png";

    public Swiper() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "swiper";
        help = "Manually triggering swiper has been disabled";
    }

    @Override
    public void execute(CommandEvent event) {
        doSwiper();
    }

    @Scheduled(cron = "0 20 0,4,8,16,20 * * ?", zone = "GMT")
    public void swiper() {
        doSwiper();
    }

    private void doSwiper() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);

        if (swampyGamesConfig.getSwiperAwaiting() != null) {
            swampysChannel.sendMessage("There is currently a swiper running, you cannot trigger another").queue();
            return;
        }

        Member member = null;
        List<DiscordUser> allUsers = discordUserRepository.findAll();
        Collections.shuffle(allUsers);

        for (DiscordUser winningUser : allUsers) {
            try {
                Member memberById = philJda.getGuilds().get(0).getMemberById(winningUser.getId());
                if (memberById != null
                        && !isNotParticipating(memberById)
                        && winningUser.getXp() > swampyGamesConfig.getSwiperPoints()
                        && winningUser.getUpdateTime().isAfter(LocalDateTime.now().minusHours(22))) {
                    member = memberById;
                }
            } catch (Exception ignored) {}
        }

        if (member == null) {
            logger.info("Swiper did not find any eligible victim");
            return;
        }

        TheSwiper theSwiper = Constants.pickRandom(SWIPERS.values());

        swampyGamesConfig.setSwiperAwaiting(member.getId());
        swampyGamesConfig.setNoSwipingPhrase(theSwiper.getNoSwipingPhrase());
        swampyGamesConfig.setSwiperSavior(null);
        swampyGamesConfig.setSwiperExpiration(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(15));
        swampyGamesConfig = swampyGamesConfigRepository.save(swampyGamesConfig);

        String description = "They're trying to steal from <@!" + member.getId() + ">\nType '" + swampyGamesConfig.getNoSwipingPhrase()
                + "' in this channel within 15 minutes to stop them!";

        MessageEmbed message = Constants.simpleEmbed(theSwiper.getSpottedPhrase(), description, theSwiper.getSpottedImage(), null, null,
                member.getUser().getEffectiveAvatarUrl());

        swampysChannel.sendMessageEmbeds(message).queue();
    }

    public static class TheSwiper {
        private final String noSwipingPhrase;
        private final String spottedPhrase;
        private final String spottedImage;
        private final String swiperLostImage;
        private final String swiperWonPhrase;
        private final String swiperWonImage;

        public TheSwiper(String noSwipingPhrase, String spottedPhrase, String spottedImage, String swiperLostImage,
                         String swiperWonPhrase, String swiperWonImage) {
            this.noSwipingPhrase = noSwipingPhrase;
            this.spottedPhrase = spottedPhrase;
            this.spottedImage = spottedImage;
            this.swiperLostImage = swiperLostImage;
            this.swiperWonPhrase = swiperWonPhrase;
            this.swiperWonImage = swiperWonImage;
        }

        public String getNoSwipingPhrase() {
            return noSwipingPhrase;
        }

        public String getSpottedPhrase() {
            return spottedPhrase;
        }

        public String getSpottedImage() {
            return spottedImage;
        }

        public String getSwiperLostImage() {
            return swiperLostImage;
        }

        public String getSwiperWonPhrase() {
            return swiperWonPhrase;
        }

        public String getSwiperWonImage() {
            return swiperWonImage;
        }
    }

}
