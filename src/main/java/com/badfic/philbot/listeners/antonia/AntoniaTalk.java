package com.badfic.philbot.listeners.antonia;

import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.listeners.BaseTalk;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
public class AntoniaTalk extends BaseTalk {
    public AntoniaTalk() {
        super("antoniaTalk");
    }

    @Override
    public Function<SwampyGamesConfig, String> usernameGetter() {
        return SwampyGamesConfig::getAntoniaNickname;
    }

    @Override
    public Function<SwampyGamesConfig, String> avatarGetter() {
        return SwampyGamesConfig::getAntoniaAvatar;
    }
}
