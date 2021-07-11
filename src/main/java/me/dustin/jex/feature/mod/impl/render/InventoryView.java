package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.HashMap;

@Feature.Manifest(name = "InventoryView", category = Feature.Category.VISUAL, description = "Show your inventory on your HUD")
public class InventoryView extends Feature {

    @Op(name = "Location", all = {"Top", "Bottom"})
    public String location = "Top";
    @Op(name = "Draw Background")
    public boolean drawBackground = true;

    private final Identifier SHULKER_GUI = new Identifier("textures/gui/container/shulker_box.png");

    @EventListener(events = {EventRender2D.class})
    private void runMethod(EventRender2D eventRender2D) {
        float y = location.equalsIgnoreCase("Top") ? -10 : Render2DHelper.INSTANCE.getScaledHeight() - 140;
        float x = (Render2DHelper.INSTANCE.getScaledWidth() / 2.f) - 95;

        HashMap<Integer, ItemStack> stacks = InventoryHelper.INSTANCE.getStacksFromInventory(false);

        if (drawBackground) {
            Render2DHelper.INSTANCE.bindTexture(SHULKER_GUI);
            Scissor.INSTANCE.cut((int) x + 5, (int) y + 18, 185, 62);
            Render2DHelper.INSTANCE.drawTexture(eventRender2D.getMatrixStack(), (int) x, (int) y, 0, 0, 285, 285, 285, 285);
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
    }
}
