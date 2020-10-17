package com.badfic.philbot.listeners.behrad;

import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.listeners.BaseTalk;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class BehradTalk extends BaseTalk implements PhilMarker {

    @Resource(name = "behradJda")
    @Lazy
    private JDA behradJda;

    public BehradTalk() {
        super("behradTalk");
    }

    @Override
    public JDA getJda() {
        return behradJda;
    }

}
