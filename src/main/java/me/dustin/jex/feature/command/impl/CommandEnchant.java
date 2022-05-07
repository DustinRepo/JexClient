package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.EnchantmentArgumentType;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

@Cmd(name = "ench", syntax = { ".ench <enchantment> <level (optional)>", ".ench all <level (optional)>" }, description = "Enchant the item in your hand at any level.", alias = "cench")
public class CommandEnchant extends Command {

	@Override
	public void registerCommand() {
		CommandNode<FabricClientCommandSource> node = dispatcher.register(literal(this.name).then(literal("all").executes(context -> {
			ItemStack stack = context.getSource().getPlayer().getMainHandItem();
			if (stack.getItem() == Items.AIR || !context.getSource().getPlayer().isCreative()) {
				ChatHelper.INSTANCE.addClientMessage("You must be in creative holding an item to enchant");
				return 0;
			}
			Registry.ENCHANTMENT.forEach(enchantment -> {
				stack.enchant(enchantment, (short) enchantment.getMaxLevel());
			});
			NetworkHelper.INSTANCE.sendPacket(new ServerboundSetCreativeModeSlotPacket(InventoryHelper.INSTANCE.getInventory().selected + 36, stack));
			ChatHelper.INSTANCE.addClientMessage("Your item is now enchanted.");
			return 1;
		}).then(argument("level", IntegerArgumentType.integer()).executes(context -> {
			int level = IntegerArgumentType.getInteger(context, "level");
			ItemStack stack = context.getSource().getPlayer().getMainHandItem();
			if (stack.getItem() == Items.AIR || !context.getSource().getPlayer().isCreative()) {
				ChatHelper.INSTANCE.addClientMessage("You must be in creative holding an item to enchant");
				return 0;
			}
			Registry.ENCHANTMENT.forEach(enchantment -> {
				stack.enchant(enchantment, (short) level);
			});
			NetworkHelper.INSTANCE.sendPacket(new ServerboundSetCreativeModeSlotPacket(InventoryHelper.INSTANCE.getInventory().selected + 36, stack));
			ChatHelper.INSTANCE.addClientMessage("Your item is now enchanted.");
			return 1;
		}))).then(argument("enchantment", EnchantmentArgumentType.enchantment()).executes(context -> {
			Enchantment enchantment = EnchantmentArgumentType.getEnchantment(context, "enchantment");
			ItemStack stack = context.getSource().getPlayer().getMainHandItem();
			if (stack.getItem() == Items.AIR || !context.getSource().getPlayer().isCreative()) {
				ChatHelper.INSTANCE.addClientMessage("You must be in creative holding an item to enchant");
				return 0;
			}
			stack.enchant(enchantment, (short) enchantment.getMaxLevel());
			NetworkHelper.INSTANCE.sendPacket(new ServerboundSetCreativeModeSlotPacket(InventoryHelper.INSTANCE.getInventory().selected + 36, stack));
			ChatHelper.INSTANCE.addClientMessage("Your item is now enchanted.");
			return 1;
		}).then(argument("level", IntegerArgumentType.integer()).executes(context -> {
			Enchantment enchantment = EnchantmentArgumentType.getEnchantment(context, "enchantment");
			int level = IntegerArgumentType.getInteger(context, "level");
			ItemStack stack = context.getSource().getPlayer().getMainHandItem();
			if (stack.getItem() == Items.AIR || !context.getSource().getPlayer().isCreative()) {
				ChatHelper.INSTANCE.addClientMessage("You must be in creative holding an item to enchant");
				return 0;
			}
			stack.enchant(enchantment, (short) level);
			NetworkHelper.INSTANCE.sendPacket(new ServerboundSetCreativeModeSlotPacket(InventoryHelper.INSTANCE.getInventory().selected + 36, stack));
			ChatHelper.INSTANCE.addClientMessage("Your item is now enchanted.");
			return 1;
		}))));
		dispatcher.register(literal("e").redirect(node));
	}

	@Override
	public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		return 0;
	}
}
