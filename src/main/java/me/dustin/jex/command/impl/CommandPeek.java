package me.dustin.jex.command.impl;

import me.dustin.jex.command.core.Command;
import me.dustin.jex.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ShulkerBoxScreenHandler;

import java.util.HashMap;

@Cmd(name = "Peek", syntax = ".peek", description = "See inside of shulkers without placing them")
public class CommandPeek extends Command {

    @Override
    public void runCommand(String command, String[] args) {
        ItemStack stack = Wrapper.INSTANCE.getLocalPlayer().getMainHandStack();
        ShulkerBoxScreenHandler shulkerBoxScreenHandler = new ShulkerBoxScreenHandler(0, InventoryHelper.INSTANCE.getInventory());
        if (InventoryHelper.INSTANCE.isShulker(stack)) {
            HashMap<Integer, ItemStack> stackHashMap = InventoryHelper.INSTANCE.getStacksFromShulker(stack);
            stackHashMap.keySet().forEach(slot -> {
                shulkerBoxScreenHandler.setStackInSlot(slot, stackHashMap.get(slot));
            });
            ShulkerBoxScreen shulkerBoxScreen = new ShulkerBoxScreen(shulkerBoxScreenHandler, InventoryHelper.INSTANCE.getInventory(), stack.getName());
            new Thread(() -> {
                try {
                    Thread.sleep(50);
                    Wrapper.INSTANCE.getMinecraft().openScreen(shulkerBoxScreen);
                } catch (InterruptedException e) {
                }
            }).start();
        } else {
            ChatHelper.INSTANCE.addClientMessage("You must be holding a Shulker Box to use this command.");
        }
    }

}
