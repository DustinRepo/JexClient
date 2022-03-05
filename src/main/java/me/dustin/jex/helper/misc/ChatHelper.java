package me.dustin.jex.helper.misc;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public enum ChatHelper {
    INSTANCE;

    public void addClientMessage(String message) {
        Wrapper.INSTANCE.getMinecraft().inGameHud.getChatHud().addMessage(new LiteralText("\2478[\247bJex\2478]\247f: \2477" + message));
    }

    public void addRawMessage(String message) {
        Wrapper.INSTANCE.getMinecraft().inGameHud.getChatHud().addMessage(new LiteralText(message));
    }

    public void addRawMessage(Text message) {
        Wrapper.INSTANCE.getMinecraft().inGameHud.getChatHud().addMessage(message);
    }
}
