package me.dustin.jex.load.impl;

import net.minecraft.world.inventory.Slot;

public interface IHandledScreen {

    Slot focusedSlot();

    int getX();

    int getY();
}
