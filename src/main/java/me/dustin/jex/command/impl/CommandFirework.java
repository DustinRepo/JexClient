package me.dustin.jex.command.impl;

import me.dustin.jex.command.core.Command;
import me.dustin.jex.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.Random;

@Cmd(name = "Firework", description = "Create a hacked firework (Creative mode only)")
public class CommandFirework extends Command {

    @Override
    public void runCommand(String command, String[] args) {
        if (!Wrapper.INSTANCE.getLocalPlayer().isCreative()) {
            ChatHelper.INSTANCE.addClientMessage("This command is for creative mode only!");
        }

        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
        NbtCompound baseCompound = new NbtCompound();
        NbtCompound tagCompound = new NbtCompound();
        NbtList explosionList = new NbtList();

        for(int i = 0; i < 5000; i++)
        {
            NbtCompound explosionCompound = new NbtCompound();

            Random rand = new Random();
            explosionCompound.putByte("Type", (byte)rand.nextInt(5));

            int colors[] = {1973019,11743532,3887386,5320730,2437522,8073150,2651799,11250603,4408131,14188952,4312372,14602026,6719955,12801229,15435844,15790320};

            explosionCompound.putIntArray("Colors", colors);
            explosionList.add(explosionCompound);
        }


        tagCompound.putInt("Flight", 0);
        tagCompound.put("Explosions", explosionList);
        baseCompound.put("Fireworks", tagCompound);
        firework.setTag(baseCompound);
        InventoryHelper.INSTANCE.getInventory().setStack(InventoryHelper.INSTANCE.getInventory().selectedSlot, firework);
    }
}
