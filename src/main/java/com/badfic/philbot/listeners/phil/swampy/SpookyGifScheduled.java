package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.PhilMarker;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class SpookyGifScheduled extends BaseSwampy implements PhilMarker {

    private static final Set<String> GIFS = new HashSet<>(Arrays.asList(
            "https://tenor.com/view/spooky-gif-6177876",
            "https://tenor.com/view/happy-halloweens-eve-halloween-dance-michael-myers-gif-15439377",
            "https://tenor.com/view/halloween-scary-jack-skellington-nightmare-before-christmas-gif-12759576",
            "https://tenor.com/view/dancing-dance-halloween-grooves-moves-gif-16886734",
            "https://tenor.com/view/prepare-yourself-anight-of-spooks-and-scares-be-ready-spooky-scary-gif-15425085",
            "https://tenor.com/view/scary-scared-terrified-horror-gif-7740680",
            "https://tenor.com/view/halloween-halloween-dance-spooky-dance-gif-10137954",
            "https://tenor.com/view/black-cat-salem-sabrina-the-teenage-witch-gif-10710252",
            "https://tenor.com/view/hocus-pocus-gif-7591030",
            "https://tenor.com/view/vampira-queen-horror-vampire-goth-gif-9157475",
            "https://tenor.com/view/halloween-salem-ghost-boo-sabrina-gif-15297328",
            "https://tenor.com/view/familiaadams-tiochico-tchauzinho-oi-boanoite-gif-9484754",
            "https://tenor.com/view/itsshowtime-beetlejuice-gif-9944822",
            "https://tenor.com/view/beetlejuice-repeat-gif-6051364",
            "https://tenor.com/view/practical-magic-gif-18660382",
            "https://tenor.com/view/ahs-coven-supreme-witch-witch-hat-put-on-your-hat-gif-15030365",
            "https://tenor.com/view/hocus-pocus-gif-7591008",
            "https://tenor.com/view/jack-skellington-scary-meanie-roar-nightmare-before-christmas-gif-5576213",
            "https://cdn.discordapp.com/attachments/530193785351569423/771214667288281088/halloween.gif",
            "https://tenor.com/view/frickin-bats-ilove-halloween-katie-ryan-bats-halloween-gif-12113966",
            "https://tenor.com/view/glee-kurt-chris-colfer-bada-bing-trick-or-treat-gif-4841997",
            "https://tenor.com/view/halloween-costume-halloweengifs-mean-girls-gif-9561686",
            "https://tenor.com/view/zombie-sleepy-yawn-goodnight-grave-gif-4616504"
    ));

    public SpookyGifScheduled() {
        name = "spookyGif";
        help = "!!spookyGif\nMake phil respond with a spooky gif";
    }

    @Override
    protected void execute(CommandEvent event) {
        String gif = pickRandom(GIFS);
        event.getChannel()
                .sendTyping()
                .submit()
                .thenRun(() -> event.getChannel().sendMessage(gif).queueAfter(5, TimeUnit.SECONDS));
    }

}
