package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.service.BaseService;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class CustomBangCommands extends BaseService {

    private static final Map<String, String> CUSTOM_COMMANDS = ImmutableMap.<String, String>builder()
            .put("adam", "https://www.youtube.com/watch?v=K0qbv8AatAU")
            .put("ahfuck", "https://www.youtube.com/watch?v=_X6VoFBCE9k")
            .put("angell", "https://cdn.discordapp.com/attachments/761398315119280158/823641140817166336/unknown.png")
            .put("aquarius", "https://cdn.discordapp.com/attachments/741053845098201099/748063936750026823/aquard.png")
            .put("aries", "https://cdn.discordapp.com/attachments/741053845098201099/748063947915001896/fieryboi1.png")
            .put("babies", "https://cdn.discordapp.com/attachments/530193785351569423/837365088239091722/babies.jpg")
            .put("bigmad", "https://cdn.discordapp.com/attachments/810711542541844541/838936079808725002/bigmad.gif")
            .put("cancer", "https://cdn.discordapp.com/attachments/741053845098201099/748063940549935174/cancerr.png")
            .put("capricorn", "https://cdn.discordapp.com/attachments/741053845098201099/748063942978306118/capricorn.png")
            .put("chickey", "https://media.giphy.com/media/3oxHQgJLWSmRrGaApO/giphy.gif")
            .put("chipotle", "https://www.youtube.com/watch?v=e56MuzEGZm4")
            .put("cinnamontoastfuck", "https://cdn.discordapp.com/attachments/742816049531060356/754680350634016778/image0.jpg")
            .put("cross", "https://cdn.discordapp.com/attachments/741024065812431018/742483198730305546/outdemon.gif")
            .put("danger", "https://cdn.discordapp.com/attachments/810711542541844541/866205240536989746/62578443_10157485922982682_6364942641326456832_n.png")
            .put("deand", "https://cdn.discordapp.com/attachments/741113400574345256/786375980868435968/deand.png")
            .put("destiel", "https://cdn.discordapp.com/attachments/530193785351569423/828712124691644468/destiel.png")
            .put("evaporate", "https://media1.tenor.com/images/e5c3693d63c437573093869dcacf7989/tenor.gif?itemid=4805888")
            .put("garyd", "https://cdn.discordapp.com/attachments/761398315119280158/781004270086127626/GARYD.png")
            .put("gay", "https://www.youtube.com/watch?v=QMojxV1dEpA")
            .put("gemini", "https://cdn.discordapp.com/attachments/741053845098201099/748063981377421343/Zodiac-gemini.png")
            .put("hellyea", "https://cdn.discordapp.com/attachments/741000350500126753/767641543032307722/ltq6fg1u0kh41.png")
            .put("hellyeaa", "https://tenor.com/view/hell-yeah-nice-awesome-dope-cool-gif-16564021")
            .put("holy", "https://cdn.discordapp.com/attachments/741024065812431018/742483096196087899/holywater.gif")
            .put("kink", "https://cdn.discordapp.com/attachments/741025286568542318/748192756962164856/kinkpositive.png")
            .put("leo", "https://cdn.discordapp.com/attachments/741053845098201099/748063951451062293/leo.png")
            .put("libra", "https://cdn.discordapp.com/attachments/741053845098201099/748063956551335936/libra.png")
            .put("lisa", "https://www.youtube.com/watch?v=amffOYclBD8")
            .put("mcfreakin", "https://www.youtube.com/watch?v=OVcENUGgspI")
            .put("murder", "https://cdn.discordapp.com/attachments/741113400574345256/786353129880682497/murder.png")
            .put("nelly", "https://media.giphy.com/media/XGJPJorgFN4laDllkj/giphy.gif")
            .put("nellyqueen", "https://cdn.discordapp.com/attachments/741000350500126753/797159603258654740/evil-queen-png-2.png")
            .put("nellytho", "https://cdn.discordapp.com/attachments/741027502528659458/765405660337930240/points.png")
            .put("nuggets", "https://media.giphy.com/media/1TE9nHMxLuukmWgt7h/giphy.gif")
            .put("offtopic", "https://www.youtube.com/watch?v=tA8LjcpjjKQ")
            .put("pisces", "https://cdn.discordapp.com/attachments/741053845098201099/748063961215139891/pissyboi.png")
            .put("riverjail", "https://cdn.discordapp.com/attachments/761398315119280158/783134887410008075/riverjail.png")
            .put("sagittarius", "https://cdn.discordapp.com/attachments/741053845098201099/748063963513749574/sagg.png")
            .put("sap", "https://cdn.discordapp.com/attachments/742152864113229895/766351587412672532/sappy.png")
            .put("schued", "https://cdn.discordapp.com/attachments/741113400574345256/791495440126312458/schued.png")
            .put("scorpio", "https://cdn.discordapp.com/attachments/741053845098201099/748063968890847362/scorpio.png")
            .put("shooketh", "https://tenor.com/view/shocked-gif-7443411")
            .put("simp", "https://cdn.discordapp.com/attachments/530193785351569423/786390800585064458/simping.png")
            .put("sirens", "https://tenor.com/view/gun-gun-show-fight-me-bring-it-on-show-off-gif-13092988")
            .put("smurder", "https://cdn.discordapp.com/attachments/741025219346563073/799710226018074694/murder_pink.png")
            .put("spectacular", "https://www.youtube.com/watch?v=vBk0P27QGmc")
            .put("spooky", "https://cdn.discordapp.com/attachments/741054108043051030/767647951455256596/ezgif.com-gif-maker1.gif")
            .put("sshocked", "https://cdn.discordapp.com/attachments/741000660291420182/786378508007243786/notsurprisedkirk.png")
            .put("steved", "https://cdn.discordapp.com/attachments/761398315119280158/781004149478916106/STEVED.png")
            .put("stirthepot", "https://cdn.discordapp.com/attachments/741022962337185994/765965006541160448/2p3k8s.jpg")
            .put("swamp", "https://cdn.discordapp.com/attachments/741000350500126753/787176045152960552/image0.gif")
            .put("taurus", "https://cdn.discordapp.com/attachments/741053845098201099/748063972359536650/taruts.png")
            .put("us", "https://www.youtube.com/watch?v=37LNH5foeUU")
            .put("validation", "https://cdn.discordapp.com/attachments/530193785351569423/778660314803470356/validation.png")
            .put("virgo", "https://cdn.discordapp.com/attachments/741053845098201099/748063991569449001/virgo.png")
            .put("why", "https://www.youtube.com/watch?v=DpxDl68brww")
            .put("yeet", "https://www.youtube.com/watch?v=2Bjy5YQ5xPc")
            .build();

    public void executeCustomCommand(String commandName, TextChannel textChannel) {
        String result = CUSTOM_COMMANDS.get(StringUtils.lowerCase(commandName));

        if (result != null) {
            textChannel.sendMessage(result).queue();
        }
    }

}
