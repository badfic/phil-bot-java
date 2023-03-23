package com.badfic.philbot.listeners.antonia;

import com.badfic.philbot.listeners.BaseTalk;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AntoniaTalk extends BaseTalk {
    private final JDA antoniaJda;

    public AntoniaTalk(@Qualifier("antoniaJda") JDA antoniaJda) {
        super("antoniaTalk");
        this.antoniaJda = antoniaJda;
    }

    @Override
    public JDA getJda() {
        return antoniaJda;
    }
}
