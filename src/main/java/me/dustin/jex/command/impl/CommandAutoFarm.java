package me.dustin.jex.command.impl;

import me.dustin.jex.command.core.Command;
import me.dustin.jex.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.impl.world.AutoFarm;

@Cmd(name = "AutoFarm", syntax = ".autofarm pos1/pos2", description = "Set AutoFarm positions")
public class CommandAutoFarm extends Command {
    @Override
    public void runCommand(String command, String[] args) {
        try {
            String pos = args[1];
            if (pos.equalsIgnoreCase("pos1")) {
                AutoFarm.pos1 = Wrapper.INSTANCE.getLocalPlayer().getBlockPos();
                ChatHelper.INSTANCE.addClientMessage("Pos 1 set to current position");
            }else
            if (pos.equalsIgnoreCase("pos2")) {
                AutoFarm.pos2 = Wrapper.INSTANCE.getLocalPlayer().getBlockPos();
                ChatHelper.INSTANCE.addClientMessage("Pos 2 set to current position");
            }else
                giveSyntaxMessage();
        } catch (Exception e) {
            giveSyntaxMessage();
        }
    }
}
