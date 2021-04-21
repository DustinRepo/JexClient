package me.dustin.jex.feature.impl.render;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRenderCrosshair;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.client.util.math.MatrixStack;

@Feat(name = "Crosshair", category = FeatureCategory.VISUAL, description = "Draw a custom crosshair on-screen.")
public class Crosshair extends Feature {

    @Op(name = "Color", isColor = true)
    public int color = 0xffff0000;

    @Op(name = "Thickness", min = 0, max = 20, inc = 0.1f)
    public float thickness = 2;
    @Op(name = "Size", min = 0, max = 20, inc = 0.1f)
    public float size = 7;
    @Op(name = "Gap", min = 0, max = 20, inc = 0.1f)
    public float gap = 1;
    @Op(name = "Outline", min = 0, max = 10, inc = 0.5f)
    public float outline = 2;
    @Op(name = "Attack Indicator")
    public boolean attackIndicator = false;

    @EventListener(events = {EventRender2D.class, EventRenderCrosshair.class})
    public void runEvent(Event event) {
        if (event instanceof EventRenderCrosshair)
            event.cancel();
        else if (event instanceof EventRender2D) {
            float x = Render2DHelper.INSTANCE.getScaledWidth() / 2;
            float y = Render2DHelper.INSTANCE.getScaledHeight() / 2;
            MatrixStack matrixStack = ((EventRender2D) event).getMatrixStack();
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x - gap - size - thickness, y - thickness, x - gap - thickness, y + thickness, 0xff000000, color, outline);
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x + gap + thickness, y - thickness, x + gap + size + thickness, y + thickness, 0xff000000, color, outline);
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x - thickness, y - gap - size - thickness, x + thickness, y - gap - thickness, 0xff000000, color, outline);
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x - thickness, y + gap + thickness, x + thickness, y + gap + size + thickness, 0xff000000, color, outline);

            if (attackIndicator && Wrapper.INSTANCE.getLocalPlayer().getAttackCooldownProgress(0) < 1) {
                Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x - 15, y + gap + size + thickness + 10, x + 15, y + gap + size + thickness + 14, 0xff000000, 0x00ffffff, 1);
                float width = 30;
                if (Wrapper.INSTANCE.getLocalPlayer().getAttackCooldownProgress(0) > 0)
                    Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x - 15, y + gap + size + thickness + 10, x - 15 + (width * Wrapper.INSTANCE.getLocalPlayer().getAttackCooldownProgress(0)), y + gap + size + thickness + 14, 0x00000000, color, 1);
            }
        }
    }

}
