package me.dustin.jex.load.impl;

import net.minecraft.world.item.ItemStack;

public interface IItemRenderer {

	void renderItemIntoGUI(ItemStack itemStack, float x, float y);

}
