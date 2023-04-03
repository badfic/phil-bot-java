package com.badfic.philbot.commands.image;

import org.springframework.stereotype.Service;

@Service
public class Boop extends BaseTwoUserImageMeme {

    public Boop() throws Exception {
        super(156, 265, 63, 382, 459, 40, "flags/boop.png", "boop");

        name = "boop";
        help = "`!!boop @someone` to boop them";
    }

}
