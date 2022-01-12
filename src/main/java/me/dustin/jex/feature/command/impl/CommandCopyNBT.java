package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.PrettyPrintTextFormatter;
import me.dustin.jex.helper.misc.Wrapper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtHelper;

@Cmd(name = "copynbt", description = "Copy the NBT data of your current item to clipboard", syntax = ".copynbt <display(optional)")
public class CommandCopyNBT extends Command {
    @Override
    public void registerCommand() {
        dispatcher.register(literal(this.name).executes(this).then(literal("display").executes(context -> {
            ItemStack itemStack = context.getSource().getPlayer().getMainHandStack();
            assert itemStack.getNbt() != null;
            String nbt = NbtHelper.toFormattedString(itemStack.getNbt());
            Wrapper.INSTANCE.getMinecraft().keyboard.setClipboard(itemStack.getNbt().toString().replace("\247", "\\247"));

            PrettyPrintTextFormatter prettyPrintTextFormatter = new PrettyPrintTextFormatter();
            prettyPrintTextFormatter.apply(itemStack.getNbt()).entriesAsText().forEach(text -> {
                Wrapper.INSTANCE.getMinecraft().inGameHud.getChatHud().addMessage(text);
            });
            ChatHelper.INSTANCE.addRawMessage("}");
            ChatHelper.INSTANCE.addClientMessage("NBT Copied to clipboard");
            return 1;
        })));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        if (context.getSource().getPlayer().getMainHandStack().getItem() == Items.AIR) {
            ChatHelper.INSTANCE.addClientMessage("You must be holding an item to use this command");
            return 0;
        }
        ItemStack itemStack = context.getSource().getPlayer().getMainHandStack();
        assert itemStack.getNbt() != null;
        String nbt = itemStack.getNbt().toString();
        Wrapper.INSTANCE.getMinecraft().keyboard.setClipboard(nbt.replace("\247", "\\247"));
        ChatHelper.INSTANCE.addClientMessage("NBT Copied to clipboard");
        return 1;
    }
}
