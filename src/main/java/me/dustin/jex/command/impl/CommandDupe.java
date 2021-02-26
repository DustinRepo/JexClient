package me.dustin.jex.command.impl;

import me.dustin.jex.command.core.Command;
import me.dustin.jex.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;

@Cmd(name = "Dupe", syntax = ".dupe", alias = {"d"}, description = "Relog. Put items you want to dupe in a chest. Hold a book and quill and run this command to reset your inventory.")
public class CommandDupe extends Command {

    private String firstPage = "";

    public CommandDupe() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 21845; i++) {
            sb.append("\uffff");
        }
        firstPage = sb.toString();
    }

    @Override
    public void runCommand(String command, String[] args) {
        ItemStack itemStack = PlayerHelper.INSTANCE.mainHandStack();
        if (itemStack != null && itemStack.getItem() == Items.WRITABLE_BOOK) {
            writeBook(itemStack);
        } else {
            ChatHelper.INSTANCE.addClientMessage("You must be holding a book & quill to use this.");
        }
    }

    private void writeBook(ItemStack itemStack) {
        ListTag listTag_1 = new ListTag();
        listTag_1.addTag(0, StringTag.of(firstPage));
        itemStack.putSubTag("pages", listTag_1);
        itemStack.putSubTag("author", StringTag.of(Wrapper.INSTANCE.getLocalPlayer().getName().getString()));
        itemStack.putSubTag("title", StringTag.of("a nice book"));

        NetworkHelper.INSTANCE.sendPacket(new BookUpdateC2SPacket(itemStack, true, InventoryHelper.INSTANCE.getInventory().selectedSlot));
    }

}
