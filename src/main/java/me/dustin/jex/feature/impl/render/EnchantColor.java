package me.dustin.jex.feature.impl.render;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.EventGetGlintShaders;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.load.impl.IShader;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.client.gl.GlUniform;

import java.awt.*;

@Feature.Manifest(name = "EnchantColor", category = Feature.Category.VISUAL, description = "Change the color of the enchanment glint (or make it rainbow!)")
public class EnchantColor extends Feature{

    @Op(name = "Color", isColor = true)
    public int color = new Color(0, 255, 0).getRGB();
    @OpChild(name = "Rainbow", parent = "Color")
    public boolean rainbow = false;
    @OpChild(name = "Speed", min = 1, max = 10, parent = "Rainbow")
    public int rainbowSpeed = 1;

    private int col;

    @EventListener(events = {EventGetGlintShaders.class})
    private void runMethod(EventGetGlintShaders eventGetGlintShaders) {
        IShader iShader = (IShader)ShaderHelper.getRainbowEnchantShader();
        GlUniform uniform = iShader.getCustomUniform("GlintColor");
        if (uniform != null) {
            Color setColor = rainbow ? ColorHelper.INSTANCE.getColorViaHue(col) : ColorHelper.INSTANCE.getColor(color);
            uniform.set(setColor.getRed() / 255.f, setColor.getGreen() / 255.f, setColor.getBlue() / 255.f, 1);
        }
        eventGetGlintShaders.setShader(ShaderHelper.getRainbowEnchantShader());
        eventGetGlintShaders.cancel();
    }

    @EventListener(events = {EventTick.class})
    private void updateColor(EventTick eventTick) {
        col += rainbowSpeed;
        if (col > 270)
            col -=270;
    }
}
