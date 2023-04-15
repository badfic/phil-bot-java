package com.badfic.philbot.listeners.john;

import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.listeners.BaseTalk;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
public class JohnTalk extends BaseTalk {

    public JohnTalk() {
        super("johnTalk");
    }

    @Override
    public Function<SwampyGamesConfig, String> usernameGetter() {
        return SwampyGamesConfig::getJohnNickname;
    }

    @Override
    public Function<SwampyGamesConfig, String> avatarGetter() {
        return SwampyGamesConfig::getJohnAvatar;
    }
}
