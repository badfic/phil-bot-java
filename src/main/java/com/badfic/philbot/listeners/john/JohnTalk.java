package com.badfic.philbot.listeners.john;

import com.badfic.philbot.listeners.BaseTalk;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class JohnTalk extends BaseTalk {
    private final JDA johnJda;

    public JohnTalk(@Qualifier("johnJda") JDA johnJda) {
        super("johnTalk");
        this.johnJda = johnJda;
    }

    @Override
    public JDA getJda() {
        return johnJda;
    }
}
