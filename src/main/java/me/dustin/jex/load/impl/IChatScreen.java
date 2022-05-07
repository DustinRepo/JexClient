package me.dustin.jex.load.impl;

import net.minecraft.client.gui.components.EditBox;

public interface IChatScreen {
    String getText();
    void setText(String text);
    EditBox getWidget();
}
