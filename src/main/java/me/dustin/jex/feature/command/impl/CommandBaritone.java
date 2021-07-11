package me.dustin.jex.feature.command.impl;

import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.misc.ChatHelper;

@Cmd(name = "Baritone", description = "Access Baritone commands", syntax = ".b <command>", alias = {"b", "bar"})
public class CommandBaritone extends Command {
    @Override
    public void runCommand(String command, String[] args) {
        String cmd = command.replace(command.split(" ")[0] + " ", "*&");
        if (BaritoneHelper.INSTANCE.baritoneExists()) {
            ChatHelper.INSTANCE.addClientMessage("Sending command to Baritone.");
            BaritoneHelper.INSTANCE.sendCommand(cmd);
        } else {
            ChatHelper.INSTANCE.addClientMessage("Baritone not found. Please make sure to place it in your mods folder.");
        }
    }
}

