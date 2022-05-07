package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Cmd(name = "hat", description = "Put your current held item on your head (Creative only)")
public class CommandHat extends Command {

    @Override
    public void registerCommand() {
        dispatcher.register(literal(this.name).executes(this).then(argument("item", ItemArgument.item(commandRegistryAccess)).executes(context -> {

            if (!Wrapper.INSTANCE.getLocalPlayer().isCreative()) {
                ChatHelper.INSTANCE.addClientMessage("This command is for creative mode only!");
                return 0;
            }
            Item item = ItemArgument.getItem(context, "item").getItem();
            ItemStack stack = new ItemStack(item);
            NetworkHelper.INSTANCE.sendPacket(new ServerboundSetCreativeModeSlotPacket(5, stack));
            ChatHelper.INSTANCE.addClientMessage("Hat set");
            return 1;
        })));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        if (!Wrapper.INSTANCE.getLocalPlayer().isCreative()) {
            ChatHelper.INSTANCE.addClientMessage("This command is for creative mode only!");
            return 0;
        }
        ItemStack stack = InventoryHelper.INSTANCE.getInventory().getSelected();
        NetworkHelper.INSTANCE.sendPacket(new ServerboundSetCreativeModeSlotPacket(5, stack));
        ChatHelper.INSTANCE.addClientMessage("Hat set");
        return 1;
    }
}
