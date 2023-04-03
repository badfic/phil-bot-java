package com.badfic.philbot.service;

import com.badfic.philbot.commands.BaseNormalCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.UUID;

public abstract class NonCommandSwampy extends BaseNormalCommand {

    public NonCommandSwampy() {
        name = UUID.randomUUID().toString();
        ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {

    }
}
