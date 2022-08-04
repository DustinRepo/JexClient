package me.dustin.jex.load.impl;

import java.util.List;
import net.minecraft.client.gui.hud.ChatHudLine;

public interface IChatHud {
    boolean containsMessage(String message);
    List<ChatHudLine> getMessages();
}
