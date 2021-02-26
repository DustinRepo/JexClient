package me.dustin.jex.module.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.load.impl.IHandledScreen;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

@ModClass(name = "InvAutoClicker", category = ModCategory.MISC, description = "Hold shift+click or ctrl+q to automatically do them.")
public class InventoryAutoClicker extends Module {

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof HandledScreen) {
                HandledScreen handledScreen = (HandledScreen) Wrapper.INSTANCE.getMinecraft().currentScreen;
                IHandledScreen iHandledScreen = (IHandledScreen) handledScreen;
                Slot slot = iHandledScreen.focusedSlot();
                if (slot != null && slot.hasStack() && getInvSlot(slot) != -1) {
                    if (GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == 1 && MouseHelper.INSTANCE.isMouseButtonDown(0))
                        InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, getInvSlot(slot), SlotActionType.QUICK_MOVE);
                    else if (GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getHandle(), GLFW.GLFW_KEY_Q) == 1 && GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == 1)
                        InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, getInvSlot(slot), SlotActionType.THROW, 1);
                }
            }
        }
    }

    int getInvSlot(Slot slot) {
        for (int i = 0; i < Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.slots.size(); i++) {
            Slot testSlot = Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.getSlot(i);
            if (slot == testSlot)
                return i;
        }
        return -1;
    }
}
