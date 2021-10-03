package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;

import java.util.ArrayList;
import java.util.Optional;

@Cmd(name = "dupe", alias = {"d"}, description = "Relog. Put items you want to dupe in a chest. Hold a book and quill and run this command to reset your inventory.")
public class CommandDupe extends Command {

    private String firstPage = "";

    public CommandDupe() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 21845; i++) {
            sb.append("\uffff");
        }
        firstPage = sb.toString();
    }

    private void writeBook(ItemStack itemStack) {
        NbtList listTag_1 = new NbtList();
        listTag_1.add(0, NbtString.of(firstPage));
        itemStack.putSubTag("pages", listTag_1);
        itemStack.putSubTag("author", NbtString.of(Wrapper.INSTANCE.getLocalPlayer().getName().getString()));
        itemStack.putSubTag("title", NbtString.of("a nice book"));
        ArrayList<String> list = new ArrayList<>();
        list.add(firstPage);
        NetworkHelper.INSTANCE.sendPacket(new BookUpdateC2SPacket(InventoryHelper.INSTANCE.getInventory().selectedSlot, list, Optional.of("A nice book")));
    }
    @Override
    public void registerCommand() {
        dispatcher.register(literal("d").redirect(dispatcher.register(literal(this.name).executes(this))));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        ItemStack itemStack = PlayerHelper.INSTANCE.mainHandStack();
        if (itemStack != null && itemStack.getItem() == Items.WRITABLE_BOOK) {
            writeBook(itemStack);
        } else {
            ChatHelper.INSTANCE.addClientMessage("You must be holding a book & quill to use this.");
        }
        return 1;
    }
}
