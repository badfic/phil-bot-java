package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.listeners.BaseTalk;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class PhilTalk extends BaseTalk implements PhilMarker {

    @Resource(name = "philJda")
    @Lazy
    private JDA philJda;

    public PhilTalk() {
        super("philTalk");
    }

    @Override
    public JDA getJda() {
        return philJda;
    }

}
