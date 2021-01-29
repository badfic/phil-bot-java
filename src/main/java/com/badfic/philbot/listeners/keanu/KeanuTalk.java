package com.badfic.philbot.listeners.keanu;

import com.badfic.philbot.listeners.BaseTalk;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import org.springframework.stereotype.Component;

@Component
public class KeanuTalk extends BaseTalk {

    @Resource(name = "keanuJda")
    private JDA keanuJda;

    public KeanuTalk() {
        super("keanuTalk");
    }

    @Override
    public JDA getJda() {
        return keanuJda;
    }

}
