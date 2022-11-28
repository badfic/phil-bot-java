package com.badfic.philbot.listeners.john;

import com.badfic.philbot.listeners.BaseTalk;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class JohnTalk extends BaseTalk {

    @Autowired
    @Qualifier("johnJda")
    @Lazy
    private JDA johnJda;

    public JohnTalk() {
        super("johnTalk");
    }

    @Override
    public JDA getJda() {
        return johnJda;
    }

}
