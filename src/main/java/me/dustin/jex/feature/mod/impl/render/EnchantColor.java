package me.dustin.jex.feature.mod.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRenderWithShader;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.helper.render.shader.ShaderProgram;
import me.dustin.jex.helper.render.shader.impl.EnchantColorShader;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.Vector4f;

import java.awt.*;

public class EnchantColor extends Feature{

    public Property<EffectMode> modeProperty = new Property.PropertyBuilder<EffectMode>(this.getClass())
            .name("Mode")
            .value(EffectMode.SHADER_RAINBOW)
            .build();
    public Property<ShaderMode> shaderModeProperty = new Property.PropertyBuilder<ShaderMode>(this.getClass())
            .name("Shader Mode")
            .value(ShaderMode.RAINBOW)
            .parent(modeProperty)
            .depends(parent -> parent.value() == EffectMode.SHADER_RAINBOW)
            .build();
    public Property<Float> saturationProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Saturation")
            .value(0.75f)
            .min(0.1f)
            .inc(0.05f)
            .parent(modeProperty)
            .depends(parent -> parent.value() == EffectMode.SHADER_RAINBOW)
            .build();
    public Property<Float> alphaProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Alpha")
            .value(1f)
            .min(0.1f)
            .inc(0.05f)
            .parent(modeProperty)
            .depends(parent -> parent.value() == EffectMode.SHADER_RAINBOW)
            .build();
    public Property<Color> colorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Color")
            .value(new Color(0, 255, 0))
            .parent(modeProperty)
            .depends(parent -> parent.value() == EffectMode.CUSTOMIZE)
            .build();
    public Property<Boolean> rainbowProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Rainbow")
            .value(false)
            .parent(colorProperty)
            .build();
    public Property<Integer> rainbowSpeedProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Speed")
            .value(1)
            .min(1)
            .max(10)
            .inc(1)
            .parent(rainbowProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public Property<Integer> stopwatchProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("ColorTimer")
            .description("Set the time interval before the color change (MS).")
            .value(1)
            .min(1)
            .max(25)
            .inc(1)
            .parent(rainbowProperty)
            .depends(parent -> (boolean) parent.value())
            .build();

    private int col;
    private final StopWatch stopWatch = new StopWatch();

    public EnchantColor() {
        super(Category.VISUAL, "Change the color of the enchanment glint (or make it rainbow!)");
    }

    @EventPointer
    private final EventListener<EventRenderWithShader> eventRenderWithShaderEventListener = new EventListener<>(event -> {
        if (RenderSystem.getShader() == GameRenderer.getRenderTypeGlintDirectShader() || RenderSystem.getShader() == GameRenderer.getRenderTypeArmorEntityGlintShader() || RenderSystem.getShader() == GameRenderer.getRenderTypeArmorGlintShader()) {
            BufferBuilder.BuiltBuffer buffer = event.getBuffer();
            EnchantColorShader shader = ShaderHelper.INSTANCE.getEnchantColorShader();
            shader.setUpdateUniforms(this::setUniforms);
            shader.bind();
            RenderSystem.enableBlend();
            BufferRenderer.drawWithoutShader(buffer);
            ShaderHelper.INSTANCE.getEnchantColorShader().detach();
            if (stopWatch.hasPassed(stopwatchProperty.value())) {
                col+=rainbowSpeedProperty.value();
                if (col > 270)
                    col-=270;
                stopWatch.reset();
            }
            event.cancel();
        }
    });

    public void setUniforms() {
        EnchantColorShader shader = ShaderHelper.INSTANCE.getEnchantColorShader();
        Color setColor = rainbowProperty.value() ? ColorHelper.INSTANCE.getColorViaHue(col) : colorProperty.value();
        shader.getUniform("GlintColor").setVec(new Vector4f(setColor.getRed() / 255.f, setColor.getGreen() / 255.f, setColor.getBlue() / 255.f, 1));
        shader.getUniform("CrazyRainbow").setBoolean(modeProperty.value() == EffectMode.SHADER_RAINBOW);
        shader.getUniform("Saturation").setFloat(saturationProperty.value());
        shader.getUniform("Alpha").setFloat(alphaProperty.value());
        shader.getUniform("MathMode").setInt(shaderModeProperty.value().ordinal());
    }

    public enum EffectMode {
        SHADER_RAINBOW, CUSTOMIZE
    }

    public enum ShaderMode {
        RAINBOW, TRANS_RIGHTS, TV, TEST
    }
}
