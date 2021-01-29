package com.badfic.philbot.listeners.john;

import com.badfic.philbot.listeners.BaseTalk;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class JohnTalk extends BaseTalk {

    @Resource(name = "johnJda")
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
