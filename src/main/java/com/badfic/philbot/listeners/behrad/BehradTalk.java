package com.badfic.philbot.listeners.behrad;

import com.badfic.philbot.listeners.BaseTalk;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class BehradTalk extends BaseTalk {

    @Autowired
    @Qualifier("behradJda")
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
