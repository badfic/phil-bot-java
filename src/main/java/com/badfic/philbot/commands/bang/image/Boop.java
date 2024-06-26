package com.badfic.philbot.commands.bang.image;

import org.springframework.stereotype.Service;

@Service
class Boop extends BaseTwoUserImageMeme {
    Boop() {
        super(156, 265, 63, 382, 459, 40, "flags/boop.png", "boop");
        name = "boop";
        help = "`!!boop @someone` to boop them";
    }
}
