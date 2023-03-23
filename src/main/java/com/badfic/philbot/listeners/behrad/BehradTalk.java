package com.badfic.philbot.listeners.behrad;

import com.badfic.philbot.listeners.BaseTalk;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class BehradTalk extends BaseTalk {
    private final JDA behradJda;

    public BehradTalk(@Qualifier("behradJda") JDA behradJda) {
        super("behradTalk");
        this.behradJda = behradJda;
    }

    @Override
    public JDA getJda() {
        return behradJda;
    }
}
