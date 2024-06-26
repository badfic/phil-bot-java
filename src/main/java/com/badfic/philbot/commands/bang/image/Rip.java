package com.badfic.philbot.commands.bang.image;

import org.springframework.stereotype.Service;

@Service
class Rip extends BaseTwoUserImageMeme {
    Rip() {
        super(74, 264, 185, 128, 74, 115, "flags/rip.png", "rip");
        name = "rip";
        aliases = new String[] {"grave"};
        help = "`!!rip @someone` to display the barry allen grave meme on their profile picture";
    }
}
