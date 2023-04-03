package com.badfic.philbot.commands.image;

import org.springframework.stereotype.Service;

@Service
public class Crown extends BaseTwoUserImageMeme {

    public Crown() throws Exception {
        super(304, 447, 274, 351, 211, 699, "flags/crown.png", "crown");

        name = "crown";
        help = "`!!crown @someone` to crown them";
    }

}
