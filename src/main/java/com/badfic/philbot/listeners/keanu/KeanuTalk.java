package com.badfic.philbot.listeners.keanu;

import com.badfic.philbot.listeners.BaseTalk;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class KeanuTalk extends BaseTalk {

    private final JDA keanuJda;

    public KeanuTalk(@Qualifier("keanuJda") JDA keanuJda) {
        super("keanuTalk");
        this.keanuJda = keanuJda;
    }

    @Override
    public JDA getJda() {
        return keanuJda;
    }

}
