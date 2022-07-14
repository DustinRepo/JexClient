package me.dustin.jex.feature.mod.impl.render;

import java.awt.Color;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRenderBackground;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Matrix4f;

public class CustomBG extends Feature {

    public final Property<Boolean> inGameOnlyProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("InGame Only")
            .value(true)
            .build();
    public final Property<Mode> modeProperty = new Property.PropertyBuilder<Mode>(this.getClass())
            .name("Draw Mode")
            .value(Mode.BOTH)
            .build();
    public final Property<Integer> blurIntensityProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Blur Intensity")
            .value(10)
            .min(5)
            .max(50)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.BLUR || parent.value() == Mode.BOTH)
            .build();

    float offset = 0;
    float a = 0.49f;
    boolean up = false;
    private final StopWatch stopWatch = new StopWatch();

    public CustomBG() {
        super(Category.VISUAL, "Draws a custom background rather than the simple dark one");
    }

    @EventPointer
    private final EventListener<EventRenderBackground> eventRenderBackgroundEventListener = new EventListener<>(event -> {
        if (inGameOnlyProperty.value() && Wrapper.INSTANCE.getLocalPlayer() == null)
            return;
        event.cancel();
        if (modeProperty.value() == Mode.BOTH || modeProperty.value() == Mode.RAINBOW) {
            Matrix4f matrix4f = event.getPoseStack().peek().getPositionMatrix();
            if (stopWatch.hasPassed(20)) {
                if (up) {
                    if (a < .49f)
                        a += 0.005f;
                    else
                        up = false;
                } else {
                    if (a > 0.01f)
                        a -= 0.005f;
                    else
                        up = true;
                }
                offset += 0.25f;
                if (offset > 270)
                    offset -= 270;
                stopWatch.reset();
            }
            float topLeftColor = offset;
            float topRightColor = offset + 80;
            float bottomRightColor = offset + (80 * 2);
            float bottomLeftColor = offset + (80 * 3);
            if (topRightColor > 270)
                topRightColor -= 270;
            if (bottomRightColor > 270)
                bottomRightColor -= 270;
            if (bottomLeftColor > 270)
                bottomLeftColor -= 270;

            Color topLeft = ColorHelper.INSTANCE.getColorViaHue(topLeftColor);
            Color topRight = ColorHelper.INSTANCE.getColorViaHue(topRightColor);
            Color bottomRight = ColorHelper.INSTANCE.getColorViaHue(bottomRightColor);
            Color bottomLeft = ColorHelper.INSTANCE.getColorViaHue(bottomLeftColor);

            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.defaultBlendFunc();

            float x = 0;
            float y = 0;
            float width = Render2DHelper.INSTANCE.getScaledWidth();
            float height = Render2DHelper.INSTANCE.getScaledHeight();

            if (Wrapper.INSTANCE.getLocalPlayer() == null) {
                Render2DHelper.INSTANCE.fill(event.getPoseStack(), 0, 0, width, height, 0xff7f7f7f);
            }
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(matrix4f, x + width, y, 0).color(topRight.getRed() / 255.f, topRight.getGreen() / 255.f, topRight.getBlue() / 255.f, 0.5f - a).next();
            bufferBuilder.vertex(matrix4f, x, y, 0).color(topLeft.getRed() / 255.f, topLeft.getGreen() / 255.f, topLeft.getBlue() / 255.f, a + 0.3f).next();
            bufferBuilder.vertex(matrix4f, x, y + height, 0).color(bottomLeft.getRed() / 255.f, bottomLeft.getGreen() / 255.f, bottomLeft.getBlue() / 255.f, 0.5f - a).next();
            bufferBuilder.vertex(matrix4f, x + width, y + height, 0).color(bottomRight.getRed() / 255.f, bottomRight.getGreen() / 255.f, bottomRight.getBlue() / 255.f, a + 0.3f).next();
            bufferBuilder.clear();
            BufferRenderer.drawWithShader(bufferBuilder.end());
            RenderSystem.enableTexture();
        }
        if (modeProperty.value() == Mode.BOTH || modeProperty.value() == Mode.BLUR) {
            Render2DHelper.INSTANCE.blur(blurIntensityProperty.value(), Wrapper.INSTANCE.getMinecraft().getFramebuffer());
        }
    });

    public enum Mode {
        BLUR, RAINBOW, BOTH
    }
}
