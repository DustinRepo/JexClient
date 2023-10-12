package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import me.dustin.jex.feature.mod.core.Feature;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class InventoryMove extends Feature {

    public InventoryMove() {
        super(Category.MOVEMENT);
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        KeyBinding[] keys = {Wrapper.INSTANCE.getOptions().rightKey, Wrapper.INSTANCE.getOptions().leftKey, Wrapper.INSTANCE.getOptions().backKey, Wrapper.INSTANCE.getOptions().forwardKey, Wrapper.INSTANCE.getOptions().jumpKey, Wrapper.INSTANCE.getOptions().sprintKey};
        KeyBinding[] bindingArray;
        int keysLength;
        int i;
        if ((Wrapper.INSTANCE.getMinecraft().currentScreen instanceof HandledScreen) || Wrapper.INSTANCE.getMinecraft().currentScreen != null && !(Wrapper.INSTANCE.getMinecraft().currentScreen instanceof ChatScreen)) {
            keysLength = (bindingArray = keys).length;
            for (i = 0; i < keysLength; i++) {
                KeyBinding key = bindingArray[i];
                KeyBinding.setKeyPressed(key.getDefaultKey(), GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getHandle(), key.getDefaultKey().getCode()) == 1);
            }
        } else if (Objects.isNull(Wrapper.INSTANCE.getMinecraft().currentScreen)) {
            keysLength = (bindingArray = keys).length;
            for (i = 0; i < keysLength; i++) {
                KeyBinding bind = bindingArray[i];
                if (GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getHandle(), bind.getDefaultKey().getCode()) != 1) {
                    KeyBinding.setKeyPressed(bind.getDefaultKey(), false);
                }
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
