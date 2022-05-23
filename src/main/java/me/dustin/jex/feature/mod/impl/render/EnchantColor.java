package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventGetGlintShaders;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.load.impl.IShader;
import net.minecraft.client.gl.GlUniform;
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
            .inc(10)
            .parent(rainbowProperty)
            .depends(parent -> (boolean) parent.value())
            .build();

    private int col;
    private final StopWatch stopWatch = new StopWatch();
    private GlUniform glintColorU;
    private GlUniform crazyRainbowU;
    private GlUniform saturationU;
    private GlUniform alphaU;
    private GlUniform mathModeU;

    public EnchantColor() {
        super(Category.VISUAL, "Change the color of the enchanment glint (or make it rainbow!)");
    }

    @EventPointer
    private final EventListener<EventGetGlintShaders> eventGetGlintShadersEventListener = new EventListener<>(event -> {
        if (glintColorU == null || crazyRainbowU == null || saturationU == null || mathModeU == null) {
            IShader iShader = (IShader) ShaderHelper.getRainbowEnchantShader();
            if (iShader != null) {
                glintColorU = iShader.getCustomUniform("GlintColor");
                crazyRainbowU = iShader.getCustomUniform("CrazyRainbow");
                saturationU = iShader.getCustomUniform("Saturation");
                alphaU = iShader.getCustomUniform("Alpha");
                mathModeU = iShader.getCustomUniform("MathMode");
            }
        }
        if (glintColorU != null) {
            Color setColor = rainbowProperty.value() ? ColorHelper.INSTANCE.getColorViaHue(col) : colorProperty.value();
            glintColorU.set(setColor.getRed() / 255.f, setColor.getGreen() / 255.f, setColor.getBlue() / 255.f, 1);
        }
        if (crazyRainbowU != null) {
            crazyRainbowU.set(modeProperty.value() == EffectMode.SHADER_RAINBOW ? 1 : 0);
        }
        if (saturationU != null) {
            saturationU.set(saturationProperty.value());
        }
        if (alphaU != null) {
            alphaU.set(alphaProperty.value());
        }
        if (mathModeU != null) {
            mathModeU.set(getShaderMode());
        }
        event.setShader(ShaderHelper.getRainbowEnchantShader());
        event.cancel();

        if (stopWatch.hasPassed(25)) {
            col+=rainbowSpeedProperty.value();
            if (col > 270)
                col-=270;
            stopWatch.reset();
        }
    });

    public int getShaderMode() {
        switch (shaderModeProperty.value()) {
            case RAINBOW -> {return 0;}
            case TRANS_RIGHTS -> {return 1;}
            case TV -> {return 2;}
            case TEST -> {return 3;}
        }
        return 0;
    }

    public enum EffectMode {
        SHADER_RAINBOW, CUSTOMIZE
    }

    public enum ShaderMode {
        RAINBOW, TRANS_RIGHTS, TV, TEST
    }
}
