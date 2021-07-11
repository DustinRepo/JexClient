package me.dustin.jex.feature.command.impl;

import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Cmd(name = "Enchant", syntax = {".enchant <enchantment> <level>", ".enchant all <level>"}, description = "Enchant the item in your hand at any level.")
public class CommandEnchant extends Command {

    @Override
    public void runCommand(String command, String[] args) {
        try {
            if (!Wrapper.INSTANCE.getLocalPlayer().isCreative()) {
                ChatHelper.INSTANCE.addClientMessage("You must be in creative for this command!");
                return;
            }
            ItemStack stack = InventoryHelper.INSTANCE.getInventory().getStack(InventoryHelper.INSTANCE.getInventory().selectedSlot);
            if (Wrapper.INSTANCE.getLocalPlayer().isCreative() && stack != null && stack.getItem() != Items.AIR) {
                String enchant = args[1];
                int level = Integer.parseInt(args[2]);

                if (enchant.equalsIgnoreCase("all")) {
                    Registry.ENCHANTMENT.forEach(enchantment -> {
                        stack.addEnchantment(enchantment, (short)level);
                    });
                    NetworkHelper.INSTANCE.sendPacket(new CreativeInventoryActionC2SPacket(InventoryHelper.INSTANCE.getInventory().selectedSlot + 36, stack));
                    ChatHelper.INSTANCE.addClientMessage("Your item is now enchanted.");
                } else {
                    Enchantment enchantment = Registry.ENCHANTMENT.get(new Identifier(enchant));
                    if (enchant != null) {
                        stack.addEnchantment(enchantment, (short) level);
                        NetworkHelper.INSTANCE.sendPacket(new CreativeInventoryActionC2SPacket(InventoryHelper.INSTANCE.getInventory().selectedSlot + 36, stack));
                        ChatHelper.INSTANCE.addClientMessage("Your item is now enchanted.");
                    } else {
                        ChatHelper.INSTANCE.addClientMessage("No enchantment found. Be sure to use MC IDs. (e.g minecraft:sharpness)");
                    }
                }
            } else {
                ChatHelper.INSTANCE.addClientMessage("You must be in creative to use this command!");
            }
        } catch (Exception e) {
            giveSyntaxMessage();
        }
    }
}
