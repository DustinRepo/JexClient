package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import me.dustin.jex.feature.mod.core.Feature;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

@Feature.Manifest(category = Feature.Category.MOVEMENT, description = "Move while in your inventory.")
public class InventoryMove extends Feature {

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        KeyMapping[] keys = {Wrapper.INSTANCE.getOptions().keyRight, Wrapper.INSTANCE.getOptions().keyLeft, Wrapper.INSTANCE.getOptions().keyDown, Wrapper.INSTANCE.getOptions().keyUp, Wrapper.INSTANCE.getOptions().keyJump, Wrapper.INSTANCE.getOptions().keySprint};
        KeyMapping[] bindingArray;
        int keysLength;
        int i;
        if ((Wrapper.INSTANCE.getMinecraft().screen instanceof AbstractContainerScreen) || Wrapper.INSTANCE.getMinecraft().screen != null && !(Wrapper.INSTANCE.getMinecraft().screen instanceof ChatScreen)) {
            keysLength = (bindingArray = keys).length;
            for (i = 0; i < keysLength; i++) {
                KeyMapping key = bindingArray[i];
                KeyMapping.set(key.getDefaultKey(), GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getWindow(), key.getDefaultKey().getValue()) == 1);
            }
        } else if (Objects.isNull(Wrapper.INSTANCE.getMinecraft().screen)) {
            keysLength = (bindingArray = keys).length;
            for (i = 0; i < keysLength; i++) {
                KeyMapping bind = bindingArray[i];
                if (GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getWindow(), bind.getDefaultKey().getValue()) != 1) {
                    KeyMapping.set(bind.getDefaultKey(), false);
                }
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
