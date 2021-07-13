package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventGetGlintShaders;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.load.impl.IShader;
import net.minecraft.client.gl.GlUniform;

import java.awt.*;

@Feature.Manifest(name = "EnchantColor", category = Feature.Category.VISUAL, description = "Change the color of the enchanment glint (or make it rainbow!)")
public class EnchantColor extends Feature{

    @Op(name = "Mode", all = {"Shader Rainbow", "Customize"})
    public String mode = "Shader Rainbow";
    @OpChild(name = "Saturation", min = 0.1f, inc = 0.05f, parent = "Mode", dependency = "Shader Rainbow")
    public float saturation = 0.75f;
    @OpChild(name = "Alpha", min = 0.1f, inc = 0.05f, parent = "Mode", dependency = "Shader Rainbow")
    public float alpha = 1f;
    @OpChild(name = "Color", isColor = true, parent = "Mode", dependency = "Customize")
    public int color = new Color(0, 255, 0).getRGB();
    @OpChild(name = "Rainbow", parent = "Color")
    public boolean rainbow = false;
    @OpChild(name = "Speed", min = 1, max = 10, parent = "Rainbow")
    public int rainbowSpeed = 1;

    private int col;
    private Timer timer = new Timer();
    private GlUniform glintColor;
    private GlUniform crazyRainbow;
    private GlUniform saturationU;
    private GlUniform alphaU;

    @EventListener(events = {EventGetGlintShaders.class})
    private void runMethod(EventGetGlintShaders eventGetGlintShaders) {
        if (glintColor == null || crazyRainbow == null || saturationU == null) {
            IShader iShader = (IShader) ShaderHelper.getRainbowEnchantShader();
            glintColor = iShader.getCustomUniform("GlintColor");
            crazyRainbow = iShader.getCustomUniform("CrazyRainbow");
            saturationU = iShader.getCustomUniform("Saturation");
            alphaU = iShader.getCustomUniform("Alpha");
        }
        if (glintColor != null) {
            Color setColor = rainbow ? ColorHelper.INSTANCE.getColorViaHue(col) : ColorHelper.INSTANCE.getColor(color);
            glintColor.set(setColor.getRed() / 255.f, setColor.getGreen() / 255.f, setColor.getBlue() / 255.f, 1);
        }
        if (crazyRainbow != null) {
            crazyRainbow.set("Shader Rainbow".equalsIgnoreCase(mode) ? 1 : 0);
        }
        if (saturationU != null) {
            saturationU.set(saturation);
        }
        if (alphaU != null) {
            alphaU.set(alpha);
        }
        eventGetGlintShaders.setShader(ShaderHelper.getRainbowEnchantShader());
        eventGetGlintShaders.cancel();

        if (timer.hasPassed(25)) {
            col+=rainbowSpeed;
            if (col > 270)
                col-=270;
            timer.reset();
        }
    }
}
