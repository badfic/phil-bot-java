package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.listeners.BaseTalk;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
public class PhilTalk extends BaseTalk {

    public PhilTalk() {
        super("philTalk");
    }

    @Override
    public Function<SwampyGamesConfig, String> usernameGetter() {
        return null;
    }

    @Override
    public Function<SwampyGamesConfig, String> avatarGetter() {
        return null;
    }
}
