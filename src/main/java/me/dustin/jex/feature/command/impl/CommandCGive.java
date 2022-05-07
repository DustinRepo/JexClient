package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.ItemStack;

@Cmd(name = "cgive", description = "Give yourself items in creative mode", syntax = ".cgive <item> <amount (optional)>", alias = "i")
public class CommandCGive extends Command {
    @Override
    public void registerCommand() {
        CommandNode<FabricClientCommandSource> node = dispatcher.register(literal(this.name).then(argument("item", ItemArgument.item(commandRegistryAccess)).executes(context -> {
            if (!context.getSource().getPlayer().isCreative()) {
                ChatHelper.INSTANCE.addClientMessage("You must be in creative to use this command");
                return 0;
            }
            ItemStack stack = ItemArgument.getItem(context, "item").createItemStack(1, true);
            int amount = stack.getMaxStackSize();
            stack.setCount(amount);

            NetworkHelper.INSTANCE.sendPacket(new ServerboundSetCreativeModeSlotPacket(36 + InventoryHelper.INSTANCE.getInventory().selected, stack));
            ChatHelper.INSTANCE.addClientMessage("Item given");
            return 1;
        }).then(argument("amount", IntegerArgumentType.integer()).executes(this))));
        dispatcher.register(literal("i").redirect(node));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        if (!context.getSource().getPlayer().isCreative()) {
            ChatHelper.INSTANCE.addClientMessage("You must be in creative to use this command");
            return 0;
        }
        int amount = IntegerArgumentType.getInteger(context, "amount");
        ItemStack stack = ItemArgument.getItem(context, "item").createItemStack(amount, true);

        NetworkHelper.INSTANCE.sendPacket(new ServerboundSetCreativeModeSlotPacket(36 + InventoryHelper.INSTANCE.getInventory().selected, stack));
        ChatHelper.INSTANCE.addClientMessage("Item given");
        return 1;
    }
}
