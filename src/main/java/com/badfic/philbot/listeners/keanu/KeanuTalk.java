package com.badfic.philbot.listeners.keanu;

import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.listeners.BaseTalk;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
public class KeanuTalk extends BaseTalk {

    public KeanuTalk() {
        super("keanuTalk");
    }

    @Override
    public Function<SwampyGamesConfig, String> usernameGetter() {
        return SwampyGamesConfig::getKeanuNickname;
    }

    @Override
    public Function<SwampyGamesConfig, String> avatarGetter() {
        return SwampyGamesConfig::getKeanuAvatar;
    }
}
