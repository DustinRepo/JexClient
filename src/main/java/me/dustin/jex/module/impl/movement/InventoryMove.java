package me.dustin.jex.module.impl.movement;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.options.KeyBinding;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

@ModClass(name = "InvMove", category = ModCategory.MOVEMENT, description = "Move while in your inventory.")
public class InventoryMove extends Module {

    @EventListener(events = {EventPlayerPackets.class})
    public void runEvent(EventPlayerPackets event) {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            KeyBinding[] keys = {Wrapper.INSTANCE.getOptions().keyRight, Wrapper.INSTANCE.getOptions().keyLeft, Wrapper.INSTANCE.getOptions().keyBack, Wrapper.INSTANCE.getOptions().keyForward, Wrapper.INSTANCE.getOptions().keyJump, Wrapper.INSTANCE.getOptions().keySprint};
            KeyBinding[] arrayOfKeyBinding1;
            int nignog;
            int hereInMyGarage;
            if ((Wrapper.INSTANCE.getMinecraft().currentScreen instanceof HandledScreen) || Wrapper.INSTANCE.getMinecraft().currentScreen != null && !(Wrapper.INSTANCE.getMinecraft().currentScreen instanceof ChatScreen)) {
                nignog = (arrayOfKeyBinding1 = keys).length;
                if (GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT) == 1) {
                    for (int i = 0; i < 8; i++) {
                        Wrapper.INSTANCE.getLocalPlayer().yaw++;
                    }
                }
                if (GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT) == 1) {
                    for (int i = 0; i < 8; i++) {
                        Wrapper.INSTANCE.getLocalPlayer().yaw--;
                    }
                }
                if (GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getHandle(), GLFW.GLFW_KEY_UP) == 1) {
                    for (int i = 0; i < 8; i++) {
                        Wrapper.INSTANCE.getLocalPlayer().pitch--;
                    }
                }
                if (GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getHandle(), GLFW.GLFW_KEY_DOWN) == 1) {
                    for (int i = 0; i < 8; i++) {
                        Wrapper.INSTANCE.getLocalPlayer().pitch++;
                    }
                }
                for (hereInMyGarage = 0; hereInMyGarage < nignog; hereInMyGarage++) {
                    KeyBinding key = arrayOfKeyBinding1[hereInMyGarage];
                    KeyBinding.setKeyPressed(key.getDefaultKey(), GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getHandle(), key.getDefaultKey().getCode()) == 1);
                }
            } else if (Objects.isNull(Wrapper.INSTANCE.getMinecraft().currentScreen)) {
                nignog = (arrayOfKeyBinding1 = keys).length;
                for (hereInMyGarage = 0; hereInMyGarage < nignog; hereInMyGarage++) {
                    KeyBinding bind = arrayOfKeyBinding1[hereInMyGarage];
                    if (GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getHandle(), bind.getDefaultKey().getCode()) != 1) {
                        KeyBinding.setKeyPressed(bind.getDefaultKey(), false);
                    }
                }
            }
        }
    }

}
