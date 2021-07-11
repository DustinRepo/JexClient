package me.dustin.jex.feature.command.impl;

import me.dustin.events.api.EventAPI;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ShulkerBoxScreenHandler;

import java.util.HashMap;

@Cmd(name = "Peek", description = "See inside of shulkers without placing them")
public class CommandPeek extends Command {
    ShulkerBoxScreen shulkerBoxScreen;
    @Override
    public void runCommand(String command, String[] args) {
        ItemStack stack = Wrapper.INSTANCE.getLocalPlayer().getMainHandStack();
        ShulkerBoxScreenHandler shulkerBoxScreenHandler = new ShulkerBoxScreenHandler(0, InventoryHelper.INSTANCE.getInventory());
        if (InventoryHelper.INSTANCE.isShulker(stack)) {
            HashMap<Integer, ItemStack> stackHashMap = InventoryHelper.INSTANCE.getStacksFromShulker(stack);
            stackHashMap.keySet().forEach(slot -> {
                shulkerBoxScreenHandler.setStackInSlot(slot, shulkerBoxScreenHandler.nextRevision(), stackHashMap.get(slot));
            });
            shulkerBoxScreen = new ShulkerBoxScreen(shulkerBoxScreenHandler, InventoryHelper.INSTANCE.getInventory(), stack.getName());
            EventAPI.getInstance().register(this);
        } else {
            ChatHelper.INSTANCE.addClientMessage("You must be holding a Shulker Box to use this command.");
        }
    }

    @EventListener(events = {EventTick.class})
    private void runMethod(EventTick eventTick) {
        if (Wrapper.INSTANCE.getLocalPlayer() == null) {
            while (EventAPI.getInstance().alreadyRegistered(this))
                EventAPI.getInstance().unregister(this);
            return;
        }
        if (Wrapper.INSTANCE.getMinecraft().currentScreen == null) {
            Wrapper.INSTANCE.getMinecraft().openScreen(shulkerBoxScreen);
            while (EventAPI.getInstance().alreadyRegistered(this))
                EventAPI.getInstance().unregister(this);
        }
    }

}
