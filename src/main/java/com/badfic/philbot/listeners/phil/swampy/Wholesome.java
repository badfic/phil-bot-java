package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.google.common.collect.ImmutableSet;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class Wholesome extends BaseSwampy {

    private static final Set<String> IMAGES = ImmutableSet.<String>builder()
            .add("https://cdn.discordapp.com/attachments/810711542541844541/875144501742682163/f1040527ebd7ae8849e62bef1cd3cd9dc162a60b.png")
            .add("https://cdn.discordapp.com/attachments/810711542541844541/875144576212537344/mullet_loki2.png")
            .add("https://cdn.discordapp.com/attachments/810711542541844541/875144702066827304/unknown.png")
            .add("https://cdn.discordapp.com/attachments/810711542541844541/875145009974882364/unknown.png")
            .add("https://cdn.discordapp.com/attachments/810711542541844541/875146467013173268/image0.jpg")
            .add("https://cdn.discordapp.com/attachments/810711542541844541/875146467520696350/image1.gif")
            .add("https://cdn.discordapp.com/attachments/810711542541844541/875146467860426802/image2.jpg")
            .add("https://cdn.discordapp.com/attachments/810711542541844541/875146468074352691/image3.png")
            .add("https://cdn.discordapp.com/attachments/810711542541844541/875146468334379008/image4.jpg")
            .add("https://cdn.discordapp.com/attachments/810711542541844541/875146468577652786/image5.jpg")
            .add("https://cdn.discordapp.com/attachments/810711542541844541/875146468976107520/image6.jpg")
            .add("https://cdn.discordapp.com/attachments/810711542541844541/875146728649682944/tommycap.png")
            .add("https://cdn.discordapp.com/attachments/810711542541844541/875147017234579516/BUCKUP.png")
            .add("https://cdn.discordapp.com/attachments/810711542541844541/875147153767555082/077718-17_Steve_Rogers.png")
            .add("https://cdn.discordapp.com/attachments/810711542541844541/875147170448281630/image1.jpg")
            .add("https://cdn.discordapp.com/attachments/810711542541844541/875147414707785788/image0.jpg")
            .add("https://cdn.discordapp.com/attachments/810711542541844541/875147879373746176/image0.png")
            .add("https://cdn.discordapp.com/attachments/810711542541844541/875149360365715516/FaceApp_1628722213496.jpg")
            .add("https://cdn.discordapp.com/attachments/810711542541844541/875149669980848160/FaceApp_1623894513216.jpg")
            .add("https://cdn.discordapp.com/attachments/707453916882665552/876366151481966622/unknown.png")
            .add("https://cdn.discordapp.com/attachments/707453916882665552/876366515795013682/unknown.png")
            .add("https://cdn.discordapp.com/attachments/707453916882665552/876366802991595530/unknown.png")
            .add("https://cdn.discordapp.com/attachments/707453916882665552/876367056621154324/unknown.png")
            .add("https://cdn.discordapp.com/attachments/323666308107599872/876370791728554014/frog.png")
            .add("https://cdn.discordapp.com/attachments/323666308107599872/876369930306617375/cat-love-and-affection-wholesome.png")
            .add("https://cdn.discordapp.com/attachments/323666308107599872/876369520003018762/yellow-octopus-wholesome-memes-20.png")
            .add("https://cdn.discordapp.com/attachments/323666308107599872/876369412129710080/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f776174747061642d6d656469612d736572766963652f53746f.png")
            .add("https://cdn.discordapp.com/attachments/707453916882665552/876369275764502588/c26c10746c3717c41d4ab49d990e8c12c6d59a43r1-659-567v2_uhq.png")
            .add("https://cdn.discordapp.com/attachments/707453916882665552/876369046306713600/unknown.png")
            .add("https://cdn.discordapp.com/attachments/707453916882665552/876368885765505024/14935fa63de1a0ef69018c6fbf268485.png")
            .add("https://cdn.discordapp.com/attachments/323666308107599872/876373002730754058/a55.png")
            .add("https://cdn.discordapp.com/attachments/323666308107599872/876373213184131072/unknown.png")
            .build();

    public Wholesome() {
        name = "wholesome";
        help = "display a wholesome image";
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply(Constants.pickRandom(IMAGES));
    }

}
