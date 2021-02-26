package me.dustin.jex.command.impl;

import me.dustin.jex.command.core.Command;
import me.dustin.jex.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

@Cmd(name = "Holo", description = "Create a floating message at your location. (Creative mode)", syntax = ".holo <message>")
public class CommandHolo extends Command {

    @Override
    public void runCommand(String command, String[] args) {
        if (!Wrapper.INSTANCE.getLocalPlayer().isCreative()) {
            ChatHelper.INSTANCE.addClientMessage("You must be in creative for this command!");
            return;
        }
        String message = args[1].replace("&", "\247");
        for (int i = 2; i < args.length; i++) {
            message = message + " " + args[i];
        }
            ItemStack stack = new ItemStack(Items.ARMOR_STAND);
            CompoundTag tag = new CompoundTag();
            ListTag listTag = new ListTag();
            listTag.add(DoubleTag.of(Wrapper.INSTANCE.getLocalPlayer().getX()));
            listTag.add(DoubleTag.of(Wrapper.INSTANCE.getLocalPlayer().getY()));
            listTag.add(DoubleTag.of(Wrapper.INSTANCE.getLocalPlayer().getZ()));
            tag.putBoolean("Invisible", true);
            tag.putBoolean("Invulnerable", true);
            tag.putBoolean("Interpret", true);
            tag.putBoolean("NoGravity", true);
            tag.putBoolean("CustomNameVisible", true);
            tag.putString("CustomName", Text.Serializer.toJson(new LiteralText(message)));
            tag.put("Pos", listTag);
            stack.putSubTag("EntityTag", tag);
            Wrapper.INSTANCE.getInteractionManager().clickCreativeStack(stack, 36 + InventoryHelper.INSTANCE.getInventory().selectedSlot);
    }
}
