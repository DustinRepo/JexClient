package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

@Feature.Manifest(category = Feature.Category.MOVEMENT, description = "Move while in your inventory.")
public class InventoryMove extends Feature {

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        KeyBinding[] keys = {Wrapper.INSTANCE.getOptions().keyRight, Wrapper.INSTANCE.getOptions().keyLeft, Wrapper.INSTANCE.getOptions().keyBack, Wrapper.INSTANCE.getOptions().keyForward, Wrapper.INSTANCE.getOptions().keyJump, Wrapper.INSTANCE.getOptions().keySprint};
        KeyBinding[] arrayOfKeyBinding1;
        int keysLength;
        int i;
        if ((Wrapper.INSTANCE.getMinecraft().currentScreen instanceof HandledScreen) || Wrapper.INSTANCE.getMinecraft().currentScreen != null && !(Wrapper.INSTANCE.getMinecraft().currentScreen instanceof ChatScreen)) {
            keysLength = (arrayOfKeyBinding1 = keys).length;
            for (i = 0; i < keysLength; i++) {
                KeyBinding key = arrayOfKeyBinding1[i];
                KeyBinding.setKeyPressed(key.getDefaultKey(), GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getHandle(), key.getDefaultKey().getCode()) == 1);
            }
        } else if (Objects.isNull(Wrapper.INSTANCE.getMinecraft().currentScreen)) {
            keysLength = (arrayOfKeyBinding1 = keys).length;
            for (i = 0; i < keysLength; i++) {
                KeyBinding bind = arrayOfKeyBinding1[i];
                if (GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getHandle(), bind.getDefaultKey().getCode()) != 1) {
                    KeyBinding.setKeyPressed(bind.getDefaultKey(), false);
                }
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
