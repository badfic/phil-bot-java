package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.listeners.BaseTalk;
import org.springframework.stereotype.Component;

@Component
public class PhilTalk extends BaseTalk implements PhilMarker {
    public PhilTalk() {
        super("philTalk");
    }
}
