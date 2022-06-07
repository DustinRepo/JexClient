package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.misc.SpeedCrafter;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.Items;

@Cmd(name = "speedcraft", alias = {"sc"}, syntax = ".speedcraft set <item (optional)>/clear", description = "Change the item used for SpeedCrafter mod (hold output item in hand or enter in command)")
public class CommandSpeedCraft extends Command {

    @Override
    public void registerCommand() {
        LiteralCommandNode<FabricClientCommandSource> node = dispatcher.register(literal(this.name).then(literal("clear").executes(context -> {
            //clear item
            SpeedCrafter speedCrafter = Feature.get(SpeedCrafter.class);
            speedCrafter.craftingItem = null;
            ChatHelper.INSTANCE.addClientMessage("SpeedCrafter item cleared");
            return 1;
        })).then(literal("set").executes(context -> {
            SpeedCrafter speedCrafter = Feature.get(SpeedCrafter.class);
            if (Wrapper.INSTANCE.getLocalPlayer().getMainHandStack().getItem() == Items.AIR) {
                ChatHelper.INSTANCE.addClientMessage("No item in hand");
            } else {
                speedCrafter.craftingItem = Wrapper.INSTANCE.getLocalPlayer().getMainHandStack().getItem();
                ChatHelper.INSTANCE.addClientMessage("SpeedCrafter item set to \247b" + speedCrafter.craftingItem.getName().getString());
            }
            return 1;
        }).then(argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess)).executes(context -> {
            SpeedCrafter speedCrafter = Feature.get(SpeedCrafter.class);
            speedCrafter.craftingItem = ItemStackArgumentType.getItemStackArgument(context, "item").getItem();
            ChatHelper.INSTANCE.addClientMessage("SpeedCrafter item set to \247b" + speedCrafter.craftingItem.getName().getString());
            return 1;
        }))));
        dispatcher.register(literal("sc").redirect(node));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        return 0;
    }
}
