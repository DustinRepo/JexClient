package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.load.impl.IHandledScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import me.dustin.jex.feature.mod.core.Feature;
import org.lwjgl.glfw.GLFW;

public class InventoryAutoClicker extends Feature {

    public InventoryAutoClicker() {
        super("InvAutoClicker", Category.MISC, "Hold shift+click or ctrl+q to automatically do them.");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof HandledScreen<?> handledScreen) {
            IHandledScreen iHandledScreen = (IHandledScreen) handledScreen;
            Slot slot = iHandledScreen.focusedSlot();
            if (slot != null && slot.hasStack() && getInvSlot(slot) != -1) {
                if (GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == 1 && MouseHelper.INSTANCE.isMouseButtonDown(0))
                    InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, getInvSlot(slot), SlotActionType.QUICK_MOVE);
                else if (GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getHandle(), GLFW.GLFW_KEY_Q) == 1 && GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == 1)
                    InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, getInvSlot(slot), SlotActionType.THROW, 1);
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    int getInvSlot(Slot slot) {
        for (int i = 0; i < Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.slots.size(); i++) {
            Slot testSlot = Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.getSlot(i);
            if (slot == testSlot)
                return i;
        }
        return -1;
    }
}
