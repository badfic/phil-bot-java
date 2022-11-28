package com.badfic.philbot.listeners.antonia;

import com.badfic.philbot.listeners.BaseTalk;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class AntoniaTalk extends BaseTalk {

    @Autowired
    @Qualifier("antoniaJda")
    @Lazy
    private JDA antoniaJda;

    public AntoniaTalk() {
        super("antoniaTalk");
    }

    @Override
    public JDA getJda() {
        return antoniaJda;
    }

}
