package com.badfic.philbot.commands.bang.flag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class Clown extends BaseFlagCommand {
    Clown() {
        super("clown");
    }
}
