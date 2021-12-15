package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ShulkerBoxScreenHandler;

import java.util.HashMap;

@Cmd(name = "peek", description = "See inside of shulkers without placing them")
public class CommandPeek extends Command {
    ShulkerBoxScreen shulkerBoxScreen;


    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer() == null) {
            EventManager.unregister(this);
            return;
        }
        if (Wrapper.INSTANCE.getMinecraft().currentScreen == null) {
            Wrapper.INSTANCE.getMinecraft().setScreen(shulkerBoxScreen);
            EventManager.unregister(this);
        }
    });

    @Override
    public void registerCommand() {
        dispatcher.register(literal(this.name).executes(this));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        ItemStack stack = Wrapper.INSTANCE.getLocalPlayer().getMainHandStack();
        ShulkerBoxScreenHandler shulkerBoxScreenHandler = new ShulkerBoxScreenHandler(0, InventoryHelper.INSTANCE.getInventory());
        if (InventoryHelper.INSTANCE.isShulker(stack)) {
            HashMap<Integer, ItemStack> stackHashMap = InventoryHelper.INSTANCE.getStacksFromShulker(stack);
            stackHashMap.keySet().forEach(slot -> {
                shulkerBoxScreenHandler.setStackInSlot(slot, shulkerBoxScreenHandler.nextRevision(), stackHashMap.get(slot));
            });
            shulkerBoxScreen = new ShulkerBoxScreen(shulkerBoxScreenHandler, InventoryHelper.INSTANCE.getInventory(), stack.getName());
            EventManager.register(this);
        } else {
            ChatHelper.INSTANCE.addClientMessage("You must be holding a Shulker Box to use this command.");
        }
        return 1;
    }
}
