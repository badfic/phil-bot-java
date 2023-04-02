package com.badfic.philbot.service;

import com.badfic.philbot.commands.BaseCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.UUID;

public abstract class NonCommandSwampy extends BaseCommand {

    public NonCommandSwampy() {
        name = UUID.randomUUID().toString();
        ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {

    }
}
