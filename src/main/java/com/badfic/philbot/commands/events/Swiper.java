package com.badfic.philbot.commands.events;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.BaseNormalCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.service.MinuteTickable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Swiper extends BaseNormalCommand implements MinuteTickable {
    // TODO: Investigate putting these in SwampyGamesConfig table
    public static final Map<String, TheSwiper> SWIPERS = Map.ofEntries(
            Map.entry("Swiper No Swiping", new TheSwiper(
                    "Swiper No Swiping",
                    "Swiper Was Spotted Nearby",
                    "https://cdn.discordapp.com/attachments/794506942906761226/986488638370107452/SwiperArrives.png",
                    "https://cdn.discordapp.com/attachments/917315206009483294/946224026144084008/image0.jpg",
                    "Swiper Escaped!",
                    "https://cdn.discordapp.com/attachments/917315206009483294/946217897800396830/image0.jpg"
            )),
            Map.entry("Snarter No Snarting", new TheSwiper(
                    "Snarter No Snarting",
                    "Rory and Snart Were Spotted Nearby",
                    "https://cdn.discordapp.com/attachments/741030569307275436/924881936592289832/familyjewels.png",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882905202640945162/snarter_lose.png",
                    "Rory and Snart Escaped!",
                    "https://cdn.discordapp.com/attachments/741030569307275436/924881936890097694/Snarter.gif"
            )),
            Map.entry("Squidward No Squidding", new TheSwiper(
                    "Squidward No Squidding",
                    "Squidward Was Spotted Nearby",
                    "https://cdn.discordapp.com/attachments/741024218938081340/924150658041532476/IMG_4941.png",
                    "https://cdn.discordapp.com/attachments/741024218938081340/924150658314141716/phonto.jpg",
                    "Squidward Squidded Away With Yo Points!",
                    "https://cdn.discordapp.com/attachments/741024218938081340/924150658540638208/phonto.jpg"
            )),
            Map.entry("Shrimper No Shrimping", new TheSwiper(
                    "Shrimper No Shrimping",
                    "Shrimp Spotted Nearby",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882890043411279942/shrimper_comes.png",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882890047177760818/shrimper_lose.png",
                    "The Shrimp Shrimped Away!",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882890051145588757/shrimper_wins.png"
            )),
            Map.entry("Schuester No Schuesting", new TheSwiper(
                    "Schuester No Schuesting",
                    "Schuester Spotted Nearby",
                    "https://cdn.discordapp.com/attachments/746148999144407211/966418471443398706/IMG_2662.png",
                    "https://cdn.discordapp.com/attachments/746148999144407211/966422230307336272/IMG_2663.jpg",
                    "The Grinch Stole Your Soul!",
                    "https://cdn.discordapp.com/attachments/746148999144407211/966422230546403430/phonto.jpg"
            )),
            Map.entry("Chooster No Choosting", new TheSwiper(
                    "Chooster No Choosting",
                    "Thomas The Tank Engine Spotted",
                    "https://cdn.discordapp.com/attachments/771565238624845846/945863411730907186/choochoocomingforyou.png",
                    "https://cdn.discordapp.com/attachments/771565238624845846/945864978475384842/caboose.png",
                    "Sweet Sweet Murder",
                    "https://cdn.discordapp.com/attachments/323666308107599872/972380655658872872/chooster-won.png"
            )),
            Map.entry("Panic No Discoing", new TheSwiper(
                    "Panic No Discoing",
                    "Brendon Urie Spotted",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882845495502962728/brendon_coming.png",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882845503979671612/brendon_lose.png",
                    "Panic! At The Crisco",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882845513215528970/brendon_win.png"
            )),
            Map.entry("Doofen No Shmirtzing", new TheSwiper(
                    "Doofen No Shmirtzing",
                    "Dr. Doofenshmirtz Spotted Nearby!",
                    "https://cdn.discordapp.com/attachments/761398315119280158/928159786388820019/doof-arrives.png",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882885589333082132/doof_loses.png",
                    "Doofenshmirtz Evil Incorporated",
                    "https://cdn.discordapp.com/attachments/794506942906761226/882885597927186472/doof_wins.png"
            )),
            Map.entry("Sebastian No Stanning", new TheSwiper(
                    "Sebastian No Stanning",
                    "Sebastian Stan Spotted Nearby!",
                    "https://cdn.discordapp.com/attachments/741024218938081340/923715944310779984/nostanning.png",
                    "https://cdn.discordapp.com/attachments/741024218938081340/923715944700837958/sebstopped.png",
                    "Get Stan'd",
                    "https://cdn.discordapp.com/attachments/741024218938081340/923715945388716094/yesstanning.png"
            )),
            Map.entry("Loki No Larceny", new TheSwiper(
                    "Loki No Larceny",
                    "Loki Spotted Nearby!",
                    "https://cdn.discordapp.com/attachments/323666308107599872/947617999869849662/loki-arrives-edit.png",
                    "https://cdn.discordapp.com/attachments/707453916882665552/947616510409576488/loki-lost.png",
                    "WOW, You just got Loki'd",
                    "https://cdn.discordapp.com/attachments/746148999144407211/946171616155541594/F4ABFC69-BACD-4285-9725-1FBFA94A21DC.jpg"
            )),
            Map.entry("Peggy No Pegging", new TheSwiper(
                    "Peggy No Pegging",
                    "Peggy Spotted Nearby!",
                    "https://cdn.discordapp.com/attachments/746148999144407211/984139370950959124/peggyarrives.png",
                    "https://cdn.discordapp.com/attachments/746148999144407211/984139371244564520/peggyloses.png",
                    "Get Pegged",
                    "https://cdn.discordapp.com/attachments/746148999144407211/984139371504631848/peggywins.png"
            )));

    public static final String SPIDERMAN_PNG = "https://cdn.discordapp.com/attachments/707453916882665552/928163455901511720/spidey.png";

    public Swiper() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "swiper";
        help = "Manually triggering swiper has been disabled";
    }

    @Override
    public void execute(CommandEvent event) {
        doSwiper();
    }

    @Scheduled(cron = "${swampy.schedule.events.swiper}", zone = "${swampy.schedule.timezone}")
    public void swiper() {
        doSwiper();
    }

    @Override
    public void runMinutelyTask() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);
        Guild guild = philJda.getGuildById(baseConfig.guildId);

        String swiperAwaiting = swampyGamesConfig.getSwiperAwaiting();
        int swiperPoints = swampyGamesConfig.getSwiperPoints();
        String swiperSavior = swampyGamesConfig.getSwiperSavior();
        String noSwipingPhrase = swampyGamesConfig.getNoSwipingPhrase();
        LocalDateTime swiperExpiration = swampyGamesConfig.getSwiperExpiration();

        if (swiperAwaiting != null && swiperExpiration.isBefore(LocalDateTime.now())) {
            swampyGamesConfig.setNoSwipingPhrase(null);
            swampyGamesConfig.setSwiperAwaiting(null);
            swampyGamesConfig.setSwiperSavior(null);
            swampyGamesConfig.setSwiperExpiration(null);
            saveSwampyGamesConfig(swampyGamesConfig);

            Swiper.TheSwiper theSwiper = Swiper.SWIPERS.get(noSwipingPhrase);
            Optional<DiscordUser> victim = discordUserRepository.findById(swiperAwaiting);

            MessageEmbed message = Constants.simpleEmbed(theSwiper.noSwipingPhrase(),
                    "Congratulations, <@" + philJda.getSelfUser().getId() + "> is a moron so nobody loses any points",
                    theSwiper.swiperLostImg(), null, null, philJda.getSelfUser().getEffectiveAvatarUrl());

            CompletableFuture<?> future = CompletableFuture.completedFuture(null);
            if (victim.isPresent()) {
                if (swiperSavior != null) {
                    try {
                        Optional<DiscordUser> savior = discordUserRepository.findById(swiperSavior);

                        String image = savior.isPresent() && savior.get().getId().equalsIgnoreCase(victim.get().getId())
                                ? Swiper.SPIDERMAN_PNG
                                : theSwiper.swiperLostImg();

                        message = Constants.simpleEmbed(theSwiper.noSwipingPhrase(),
                                "Congratulations, <@" + (savior.isPresent() ? savior.get().getId() : "somebody")
                                        + "> scared them away from <@" + victim.get().getId() + ">",
                                image);

                        if (savior.isPresent()) {
                            savior.get().setSwiperParticipations(savior.get().getSwiperParticipations() + 1);
                            discordUserRepository.save(savior.get());
                            Member saviorMember = guild.getMemberById(savior.get().getId());
                            if (saviorMember != null) {
                                future = givePointsToMember(700, saviorMember, PointsStat.SWIPER);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Exception with swiper savior branch", e);
                    }
                } else {
                    try {
                        Member memberById = guild.getMemberById(victim.get().getId());

                        if (memberById != null) {
                            future = takePointsFromMember(swiperPoints, memberById, PointsStat.SWIPER);
                            message = Constants.simpleEmbed(theSwiper.swiperWonPhrase(),
                                    "You didn't save <@" + victim.get().getId() + "> in time, they lost " + swiperPoints + " points",
                                    theSwiper.swiperWonImg());
                        }
                    } catch (Exception e) {
                        log.error("Exception looking up swiper victim [id={}] after they were not saved", victim.get().getId(), e);
                    }
                }
            }

            final MessageEmbed finalMessage = message;
            future.thenRun(() -> swampysChannel.sendMessageEmbeds(finalMessage).queue());
        }
    }

    private void doSwiper() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);
        Guild guild = philJda.getGuildById(baseConfig.guildId);

        if (swampyGamesConfig.getSwiperAwaiting() != null) {
            swampysChannel.sendMessage("There is currently a swiper running, you cannot trigger another").queue();
            return;
        }

        Member member = null;
        List<DiscordUser> allUsers = discordUserRepository.findAll();
        Collections.shuffle(allUsers);

        for (DiscordUser winningUser : allUsers) {
            try {
                Member memberById = guild.getMemberById(winningUser.getId());
                if (memberById != null
                        && !isNotParticipating(memberById)
                        && winningUser.getXp() > swampyGamesConfig.getSwiperPoints()
                        && winningUser.getUpdateTime().isAfter(LocalDateTime.now().minusHours(22))) {
                    member = memberById;
                }
            } catch (Exception ignored) {}
        }

        if (member == null) {
            log.info("Swiper did not find any eligible victim");
            return;
        }

        TheSwiper theSwiper = Constants.pickRandom(SWIPERS.values());

        swampyGamesConfig.setSwiperAwaiting(member.getId());
        swampyGamesConfig.setNoSwipingPhrase(theSwiper.noSwipingPhrase());
        swampyGamesConfig.setSwiperSavior(null);
        swampyGamesConfig.setSwiperExpiration(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(15));
        swampyGamesConfig = saveSwampyGamesConfig(swampyGamesConfig);

        String description = "They're trying to steal from <@" + member.getId() + ">\nType '" + swampyGamesConfig.getNoSwipingPhrase()
                + "' in this channel within 15 minutes to stop them!";

        MessageEmbed message = Constants.simpleEmbed(theSwiper.spottedPhrase(), description, theSwiper.spottedImg(), null, null,
                member.getEffectiveAvatarUrl());

        swampysChannel.sendMessageEmbeds(message).queue();
    }

    record TheSwiper(String noSwipingPhrase, String spottedPhrase, String spottedImg, String swiperLostImg, String swiperWonPhrase, String swiperWonImg) {}

}
