package me.dustin.jex.load.impl;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.Text;

import java.util.List;

public interface IChatHud {
    boolean containsMessage(String message);
    List<ChatHudLine<Text>> getMessages();
}
