package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.listeners.BaseTalk;
import net.dv8tion.jda.api.JDA;
import org.springframework.stereotype.Component;

@Component
public class PhilTalk extends BaseTalk implements PhilMarker {

    public PhilTalk() {
        super("philTalk");
    }

    @Override
    public JDA getJda() {
        return philJda;
    }

}
