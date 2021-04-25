package me.dustin.jex.command.impl;

import me.dustin.jex.command.core.Command;
import me.dustin.jex.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;

@Cmd(name = "Hat", description = "Put your current held item on your head (Creative only)")
public class CommandHat extends Command {

    @Override
    public void runCommand(String command, String[] args) {
        if (!Wrapper.INSTANCE.getLocalPlayer().isCreative()) {
            ChatHelper.INSTANCE.addClientMessage("This command is for creative mode only!");
        }
        ItemStack stack = InventoryHelper.INSTANCE.getInventory().getMainHandStack();

        NetworkHelper.INSTANCE.sendPacket(new CreativeInventoryActionC2SPacket(5, stack));
    }
}
