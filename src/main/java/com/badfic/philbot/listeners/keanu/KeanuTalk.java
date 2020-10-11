package com.badfic.philbot.listeners.keanu;

import com.badfic.philbot.config.KeanuMarker;
import com.badfic.philbot.listeners.BaseTalk;
import org.springframework.stereotype.Component;

@Component
public class KeanuTalk extends BaseTalk implements KeanuMarker {
    public KeanuTalk() {
        super("keanuTalk");
    }
}
