package me.dustin.jex.feature.mod.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.filters.ToolTipFilter;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.EventRenderToolTip;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Show the contents of a shulker box on your tooltip.")
public class ShulkerToolTip extends Feature {

    @Op(name = "Inspect Key", isKeybind = true)
    public int inspectKey = GLFW.GLFW_KEY_LEFT_CONTROL;

    private final Identifier SHULKER_GUI = new Identifier("jex", "gui/mc/shulker_background.png");
    private float inspectX = -99999, inspectY = -99999;
    private ItemStack inspectStack;

    @EventPointer
    private final EventListener<EventRenderToolTip> eventDrawScreenEventListener = new EventListener<>(event -> {
        ItemStack stack = event.getItemStack();
        if (inspectStack != null) {
            if (KeyboardHelper.INSTANCE.isPressed(inspectKey)) {
                stack = inspectStack;
                event.setItemStack(stack);
            } else {
                inspectStack = null;
            }
        }
        if (InventoryHelper.INSTANCE.isShulker(stack)) {
            BlockItem shulkerBoxItem = (BlockItem) stack.getItem();
            HashMap<Integer, ItemStack> stacks = InventoryHelper.INSTANCE.getStacksFromShulker(stack);
            float x = inspectX == -99999 ? MouseHelper.INSTANCE.getMouseX() + 8 : inspectX;
            if (x + 180 > Render2DHelper.INSTANCE.getScaledWidth())
                x -= (Render2DHelper.INSTANCE.getScaledWidth() - x);
            float y = inspectY == -99999 ? MouseHelper.INSTANCE.getMouseY() - 85 : inspectY;
            if (y + 69 > Render2DHelper.INSTANCE.getScaledHeight())
                y -= (Render2DHelper.INSTANCE.getScaledHeight() - y);

            if (KeyboardHelper.INSTANCE.isPressed(inspectKey)) {
                if (inspectStack == null)
                    inspectStack = stack;
                if (inspectX == -99999 || inspectY == -99999) {
                    inspectX = x;
                    inspectY = y;
                }
                event.setX((int) inspectX - 8);
                event.setY((int) inspectY + 84);
            } else {
                inspectX = -99999;
                inspectY = -99999;
                inspectStack = null;
            }

            MatrixStack matrixStack = event.getMatrixStack();
            matrixStack.push();

            RenderSystem.disableDepthTest();
            Render2DHelper.INSTANCE.bindTexture(SHULKER_GUI);
            if (shulkerBoxItem != Items.SHULKER_BOX)
                Render2DHelper.INSTANCE.shaderColor(shulkerBoxItem.getBlock().getDefaultMapColor().color);
            matrixStack.translate(0.0F, 0.0F, 32);
            DrawableHelper.drawTexture(event.getMatrixStack(), (int) x, (int) y, 0, 0, 180, 69, 180, 69);
            Render2DHelper.INSTANCE.shaderColor(0xffffffff);

            int xCount = 0;
            int yCount = 0;

            float hoverX = 0, hoverY = 0;
            for (int i = 0; i < 27; i++) {
                float xPos = x + 8 + (18.5f * xCount);
                float yPos = y + 8 + (18.5f * yCount);
                if (stacks.containsKey(i)) {
                    ItemStack itemStack = stacks.get(i);
                    Wrapper.INSTANCE.getMinecraft().getItemRenderer().zOffset = 199;
                    Render2DHelper.INSTANCE.drawItem(itemStack, (int) xPos, (int) yPos);
                    Wrapper.INSTANCE.getMinecraft().getItemRenderer().zOffset = 0;

                    if (KeyboardHelper.INSTANCE.isPressed(inspectKey) && Render2DHelper.INSTANCE.isHovered(xPos - 1, yPos - 1, 20, 20)) {
                        event.setOther(new EventRenderToolTip.ToolTipData(itemStack, MouseHelper.INSTANCE.getMouseX(), MouseHelper.INSTANCE.getMouseY()));
                        hoverX = xPos;
                        hoverY = yPos;
                    }
                }
                xCount++;
                if (xCount > 8) {
                    xCount = 0;
                    yCount++;
                }
            }

            if (hoverX != 0 || hoverY != 0) {
                Render2DHelper.INSTANCE.fill(matrixStack, hoverX - 1, hoverY - 0.5f, hoverX + 16, hoverY + 16, 0x60ffffff);
            }
            matrixStack.translate(0.0F, 0.0F, -32);

            RenderSystem.enableDepthTest();
            matrixStack.pop();
        }
    }, new ToolTipFilter(EventRenderToolTip.Mode.PRE));

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (!KeyboardHelper.INSTANCE.isPressed(inspectKey)) {
            inspectX = -99999;
            inspectY = -99999;
            inspectStack = null;
        }
    }, new TickFilter(EventTick.Mode.PRE));
}
