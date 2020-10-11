package com.badfic.philbot.listeners.behrad;

import com.badfic.philbot.config.BehradMarker;
import com.badfic.philbot.listeners.BaseTalk;
import org.springframework.stereotype.Component;

@Component
public class BehradTalk extends BaseTalk implements BehradMarker {
    public BehradTalk() {
        super("behradTalk");
    }
}
