package com.badfic.philbot.listeners.phil.swampy;

import org.springframework.stereotype.Service;

@Service
public class Cat extends BaseTwoUserImageMeme {

    public Cat() throws Exception {
        super(0, 0, 0, 80, 334, 643, "flags/cat.png", "cat");

        name = "cat";
        help = "`!!cat @someone` to put them on a cat image";
    }

}
