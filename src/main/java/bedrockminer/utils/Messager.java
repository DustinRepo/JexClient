package bedrockminer.utils;


import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class Messager {
    public static void actionBar(String message){
        Minecraft minecraftClient = Minecraft.getInstance();
        minecraftClient.gui.setOverlayMessage(Component.nullToEmpty(message),false);
    }

    public static void chat(String message){
        Minecraft minecraftClient = Minecraft.getInstance();
        minecraftClient.gui.getChat().addMessage(Component.nullToEmpty(message));
    }
}

