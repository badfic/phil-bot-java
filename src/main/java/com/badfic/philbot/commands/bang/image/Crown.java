package com.badfic.philbot.commands.bang.image;

import org.springframework.stereotype.Service;

@Service
class Crown extends BaseTwoUserImageMeme {
    Crown() {
        super(304, 447, 274, 351, 211, 699, "flags/crown.png", "crown");
        name = "crown";
        help = "`!!crown @someone` to crown them";
    }
}
