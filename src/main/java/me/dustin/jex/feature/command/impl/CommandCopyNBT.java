package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.PrettyPrintTextFormatter;
import me.dustin.jex.helper.misc.Wrapper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Cmd(name = "copynbt", description = "Copy the NBT data of your current item to clipboard", syntax = ".copynbt <display(optional)")
public class CommandCopyNBT extends Command {
    @Override
    public void registerCommand() {
        dispatcher.register(literal(this.name).executes(this).then(literal("display").executes(context -> {
            ItemStack itemStack = context.getSource().getPlayer().getMainHandItem();
            assert itemStack.getTag() != null;
            Wrapper.INSTANCE.getMinecraft().keyboardHandler.setClipboard(itemStack.getTag().toString().replace("\247", "\\247"));

            PrettyPrintTextFormatter prettyPrintTextFormatter = new PrettyPrintTextFormatter();
            prettyPrintTextFormatter.apply(itemStack.getTag()).entriesAsText().forEach(text -> {
                Wrapper.INSTANCE.getMinecraft().gui.getChat().addMessage(text);
            });
            ChatHelper.INSTANCE.addClientMessage("NBT Copied to clipboard");
            return 1;
        })));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        if (context.getSource().getPlayer().getMainHandItem().getItem() == Items.AIR) {
            ChatHelper.INSTANCE.addClientMessage("You must be holding an item to use this command");
            return 0;
        }
        ItemStack itemStack = context.getSource().getPlayer().getMainHandItem();
        assert itemStack.getTag() != null;
        String nbt = itemStack.getTag().toString();
        Wrapper.INSTANCE.getMinecraft().keyboardHandler.setClipboard(nbt.replace("\247", "\\247"));
        ChatHelper.INSTANCE.addClientMessage("NBT Copied to clipboard");
        return 1;
    }
}
