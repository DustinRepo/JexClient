package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import me.dustin.jex.feature.mod.core.Feature;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;

public class InventoryView extends Feature {

    public final Property<Location> locationProperty = new Property.PropertyBuilder<Location>(this.getClass())
            .name("Location")
            .value(Location.TOP)
            .build();
    public final Property<Boolean> drawBackgroundProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Draw Background")
            .value(true)
            .build();

    private final Identifier SHULKER_GUI = new Identifier("textures/gui/container/shulker_box.png");

    public InventoryView() {
        super(Category.VISUAL, "Show your inventory on your HUD");
    }

    @EventPointer
    private final EventListener<EventRender2D> eventRender2DEventListener = new EventListener<>(event -> {
        if (locationProperty.value() == Location.TOP && KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_TAB) && Wrapper.INSTANCE.getMinecraft().currentScreen == null)
            return;
        float y = locationProperty.value() == Location.TOP ? -10 : Render2DHelper.INSTANCE.getScaledHeight() - 140;
        float x = (Render2DHelper.INSTANCE.getScaledWidth() / 2.f) - 95;

        HashMap<Integer, ItemStack> stacks = InventoryHelper.INSTANCE.getStacksFromInventory(false);

        if (drawBackgroundProperty.value()) {
            Render2DHelper.INSTANCE.bindTexture(SHULKER_GUI);
            Scissor.INSTANCE.cut((int) x + 5, (int) y + 18, 185, 62);
            Render2DHelper.INSTANCE.drawTexture(event.getPoseStack(), (int) x, (int) y, 0, 0, 285, 285, 285, 285);
            Scissor.INSTANCE.seal();
        }
        int xCount = 0;
        int yCount = 0;

        for (int i = 0; i < 27; i++) {
            float xPos = x + 10 + (20 * xCount);
            float yPos = y + 20 + (20 * yCount);
            if (stacks.containsKey(i)) {
                ItemStack itemStack = stacks.get(i);
                Wrapper.INSTANCE.getMinecraft().getItemRenderer().zOffset = 300;
                Render2DHelper.INSTANCE.drawItem(itemStack, (int) xPos, (int) yPos);
                Wrapper.INSTANCE.getMinecraft().getItemRenderer().zOffset = 0;
            }
            xCount++;
            if (xCount > 8) {
                xCount = 0;
                yCount++;
            }
        }
    }, Priority.LAST);

    public enum Location {
        TOP, BOTTOM
    }
}
