package me.dustin.jex.load.impl;

import net.minecraft.screen.slot.Slot;

public interface IHandledScreen {

    Slot focusedSlot();

    int getX();

    int getY();
}
