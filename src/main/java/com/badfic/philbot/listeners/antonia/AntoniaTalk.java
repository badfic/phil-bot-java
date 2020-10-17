package com.badfic.philbot.listeners.antonia;

import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.listeners.BaseTalk;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class AntoniaTalk extends BaseTalk implements PhilMarker {

    @Resource(name = "antoniaJda")
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
