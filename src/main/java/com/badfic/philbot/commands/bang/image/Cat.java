package com.badfic.philbot.commands.bang.image;

import org.springframework.stereotype.Service;

@Service
class Cat extends BaseTwoUserImageMeme {
    Cat() {
        super(0, 0, 0, 80, 334, 643, "flags/cat.png", "cat");
        name = "cat";
        help = "`!!cat @someone` to put them on a cat image";
    }
}
