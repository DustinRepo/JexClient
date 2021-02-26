package me.dustin.jex.command.impl;

import me.dustin.jex.command.core.Command;
import me.dustin.jex.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.impl.world.AutoFarm;

@Cmd(name = "AutoFarm", syntax = ".autofarm pos1/pos2", description = "Set AutoFarm positions")
public class CommandAutoFarm extends Command {
    @Override
    public void runCommand(String command, String[] args) {
        try {
            String pos = args[1];
            if (pos.equalsIgnoreCase("pos1")) {
                AutoFarm.pos1 = Wrapper.INSTANCE.getLocalPlayer().getBlockPos();
            }
            if (pos.equalsIgnoreCase("pos2")) {
                AutoFarm.pos2 = Wrapper.INSTANCE.getLocalPlayer().getBlockPos();
            }
        } catch (Exception e) {
            giveSyntaxMessage();
        }
    }
}
