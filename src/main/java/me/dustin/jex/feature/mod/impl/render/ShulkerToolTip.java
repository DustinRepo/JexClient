package me.dustin.jex.feature.mod.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.event.render.EventRenderToolTip;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import me.dustin.jex.load.impl.IHandledScreen;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

import java.util.HashMap;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Show the contents of a shulker box as a tooltip.")
public class ShulkerToolTip extends Feature {

    private final Identifier SHULKER_GUI = new Identifier("textures/gui/container/shulker_box.png");

    @EventPointer
    private final EventListener<EventDrawScreen> eventDrawScreenEventListener = new EventListener<>(event -> {
        if (event.getMode() == EventDrawScreen.Mode.PRE && event.getScreen() instanceof HandledScreen) {
            Wrapper.INSTANCE.getMinecraft().getItemRenderer().zOffset = -200;
        }
        if (event.getMode() == EventDrawScreen.Mode.POST_CONTAINER && event.getScreen() instanceof HandledScreen) {
            IHandledScreen screen = (IHandledScreen) event.getScreen();
            Slot slot = screen.focusedSlot();
            if (slot == null)
                return;
            ItemStack stack = slot.getStack();
            if (InventoryHelper.INSTANCE.isShulker(stack)) {
                ItemStack shulker = slot.getStack();
                HashMap<Integer, ItemStack> stacks = InventoryHelper.INSTANCE.getStacksFromShulker(shulker);
                float x = screen.getX() + slot.x + 18;
                if (x + (20 * 10) > Render2DHelper.INSTANCE.getScaledWidth())
                    x -= 20 * 10;
                float y = screen.getY() + slot.y;
                if (y + (20 * 3) > Render2DHelper.INSTANCE.getScaledHeight())
                    y -= 20 * 3;

                MatrixStack matrixStack = event.getMatrixStack();
                matrixStack.push();

                RenderSystem.disableDepthTest();
                Render2DHelper.INSTANCE.bindTexture(SHULKER_GUI);
                Scissor.INSTANCE.cut((int) x, (int) y, 285, 85);
                matrixStack.translate(0.0F, 0.0F, 32.0F);
                DrawableHelper.drawTexture(event.getMatrixStack(), (int) x, (int) y, 0, 0, 285, 285, 285, 285);
                Scissor.INSTANCE.seal();
                FontHelper.INSTANCE.draw(event.getMatrixStack(), shulker.getName().getString(), x + 9, y + 7, 0xff202020);

                int xCount = 0;
                int yCount = 0;

                for (int i = 0; i < 27; i++) {
                    float xPos = x + 10 + (20 * xCount);
                    float yPos = y + 20 + (20 * yCount);
                    if (stacks.containsKey(i)) {
                        ItemStack itemStack = stacks.get(i);
                        Wrapper.INSTANCE.getMinecraft().getItemRenderer().zOffset = 200;
                        Render2DHelper.INSTANCE.drawItem(itemStack, (int) xPos, (int) yPos);
                        Wrapper.INSTANCE.getMinecraft().getItemRenderer().zOffset = 0;
                    }
                    xCount++;
                    if (xCount > 8) {
                        xCount = 0;
                        yCount++;
                    }
                }
                matrixStack.translate(0.0F, 0.0F, -32.0F);

                RenderSystem.enableDepthTest();
                matrixStack.pop();
            }
        }
    });
    @EventPointer
    private final EventListener<EventRenderToolTip> eventRenderToolTipEventListener = new EventListener<>(event -> {
        if (InventoryHelper.INSTANCE.isShulker(event.getItemStack()))
            event.cancel();
    });
}
