package me.dustin.jex.feature.impl.movement;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.core.Feature;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

@Feature.Manifest(name = "InvMove", category = Feature.Category.MOVEMENT, description = "Move while in your inventory.")
public class InventoryMove extends Feature {

    @EventListener(events = {EventPlayerPackets.class})
    public void runEvent(EventPlayerPackets event) {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            KeyBinding[] keys = {Wrapper.INSTANCE.getOptions().keyRight, Wrapper.INSTANCE.getOptions().keyLeft, Wrapper.INSTANCE.getOptions().keyBack, Wrapper.INSTANCE.getOptions().keyForward, Wrapper.INSTANCE.getOptions().keyJump, Wrapper.INSTANCE.getOptions().keySprint};
            KeyBinding[] arrayOfKeyBinding1;
            int nignog;
            int hereInMyGarage;
            if ((Wrapper.INSTANCE.getMinecraft().currentScreen instanceof HandledScreen) || Wrapper.INSTANCE.getMinecraft().currentScreen != null && !(Wrapper.INSTANCE.getMinecraft().currentScreen instanceof ChatScreen)) {
                nignog = (arrayOfKeyBinding1 = keys).length;
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
