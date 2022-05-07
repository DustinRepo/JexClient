package me.dustin.jex.load.impl;

import java.util.List;
import net.minecraft.client.GuiMessage;
import net.minecraft.network.chat.Component;

public interface IChatComponent {
    boolean containsMessage(String message);
    List<GuiMessage<Component>> getMessages();
}
