package com.badfic.philbot.listeners.keanu;

import com.badfic.philbot.listeners.BaseTalk;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class KeanuTalk extends BaseTalk {

    @Autowired
    @Qualifier("keanuJda")
    private JDA keanuJda;

    public KeanuTalk() {
        super("keanuTalk");
    }

    @Override
    public JDA getJda() {
        return keanuJda;
    }

}
