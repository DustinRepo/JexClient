package me.dustin.jex.feature.mod.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ToolTipFilter;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.event.render.EventRenderToolTip;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import me.dustin.jex.load.impl.IHandledScreen;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.HashMap;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Show the contents of a shulker box as a tooltip.")
public class ShulkerToolTip extends Feature {

    private final Identifier SHULKER_GUI = new Identifier("jex", "gui/mc/shulker_background.png");

    @EventPointer
    private final EventListener<EventRenderToolTip> eventDrawScreenEventListener = new EventListener<>(event -> {
            ItemStack stack = event.getItemStack();
            if (InventoryHelper.INSTANCE.isShulker(stack)) {
                ItemStack shulker = stack;
                BlockItem shulkerBoxItem = (BlockItem) stack.getItem();
                HashMap<Integer, ItemStack> stacks = InventoryHelper.INSTANCE.getStacksFromShulker(shulker);
                float x = MouseHelper.INSTANCE.getMouseX() + 8;
                if (x + (20 * 10) > Render2DHelper.INSTANCE.getScaledWidth())
                    x -= 20 * 10;
                float y = MouseHelper.INSTANCE.getMouseY() - 85;
                if (y + (20 * 3) > Render2DHelper.INSTANCE.getScaledHeight())
                    y -= 20 * 3;

                MatrixStack matrixStack = event.getMatrixStack();
                matrixStack.push();

                RenderSystem.disableDepthTest();
                Render2DHelper.INSTANCE.bindTexture(SHULKER_GUI);
                if (shulkerBoxItem != Items.SHULKER_BOX)
                    Render2DHelper.INSTANCE.shaderColor(shulkerBoxItem.getBlock().getDefaultMapColor().color);
                matrixStack.translate(0.0F, 0.0F, 32.0F);
                DrawableHelper.drawTexture(event.getMatrixStack(), (int) x, (int) y, 0, 0, 180, 69, 180, 69);

                int xCount = 0;
                int yCount = 0;

                for (int i = 0; i < 27; i++) {
                    float xPos = x + 8 + (18.5f * xCount);
                    float yPos = y + 8 + (18.5f * yCount);
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
    }, new ToolTipFilter(EventRenderToolTip.Mode.POST));

    @Override
    public void onEnable() {
        super.onEnable();
    }
}
