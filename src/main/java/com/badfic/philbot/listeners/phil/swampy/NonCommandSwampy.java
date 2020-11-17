package com.badfic.philbot.listeners.phil.swampy;

import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.UUID;

public abstract class NonCommandSwampy extends BaseSwampy {

    public NonCommandSwampy() {
        name = UUID.randomUUID().toString();
        ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {

    }
}
