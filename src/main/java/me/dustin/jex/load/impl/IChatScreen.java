package me.dustin.jex.load.impl;

import net.minecraft.client.gui.widget.TextFieldWidget;

public interface IChatScreen {
    String getText();
    void setText(String text);
    TextFieldWidget getWidget();
}
