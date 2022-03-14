package com.badfic.philbot.listeners.phil.swampy;

import org.springframework.stereotype.Service;

@Service
public class Rip extends BaseTwoUserImageMeme {

    public Rip() throws Exception {
        super(74, 264, 185, 128, 74, 115, "flags/rip.png", "rip");

        name = "rip";
        aliases = new String[] {"grave"};
        help = "`!!rip @someone` to display the barry allen grave meme on their profile picture";
    }

}
