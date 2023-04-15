package com.badfic.philbot.listeners.behrad;

import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.listeners.BaseTalk;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
public class BehradTalk extends BaseTalk {

    public BehradTalk() {
        super("behradTalk");
    }

    @Override
    public Function<SwampyGamesConfig, String> usernameGetter() {
        return SwampyGamesConfig::getBehradNickname;
    }

    @Override
    public Function<SwampyGamesConfig, String> avatarGetter() {
        return SwampyGamesConfig::getBehradAvatar;
    }
}
