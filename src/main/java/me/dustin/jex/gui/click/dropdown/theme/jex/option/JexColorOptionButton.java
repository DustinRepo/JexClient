package me.dustin.jex.gui.click.dropdown.theme.jex.option;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.feature.option.types.ColorOption;
import me.dustin.jex.gui.click.dropdown.impl.option.ColorOptionButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.util.Mth;

public class JexColorOptionButton extends ColorOptionButton {
    public JexColorOptionButton(DropdownWindow window, ColorOption option, float x, float y, float width, float height) {
        super(window, option, x, y, width, height);
    }

    @Override
    public void render(PoseStack matrixStack) {
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x40ffffff);
        float huepos = (((float) colorOption.getH() / 270)) * (80);

        float satpos = ((float) (colorOption.getS())) * (80);
        float brightpos = ((float) ((1 - colorOption.getB())) * 79);


        Render2DHelper.INSTANCE.drawGradientRect(this.getX() + 5, this.getY() + 15, this.getX() + 85, this.getY() + 95, -1, 0xff000000);
        drawGradientRect(matrixStack, this.getX() + 5, this.getY() + 15, this.getX() + 85, this.getY() + 95, ColorHelper.INSTANCE.getColorViaHue(colorOption.getH()).getRGB(), 0xff000000);
        Render2DHelper.INSTANCE.drawGradientRect(this.getX() + 5, this.getY() + 15, this.getX() + 85, this.getY() + 95, 0x20000000, 0xff000000);
        //color cursor
        Render2DHelper.INSTANCE.fill(matrixStack, this.getX() + 5 + satpos - 1, this.getY() + 15 + brightpos - 1, this.getX() + 5 + satpos + 1, this.getY() + 15 + brightpos + 1, -1);

        //hue slider
        Render2DHelper.INSTANCE.bindTexture(colorSlider);
        GuiComponent.blit(matrixStack, (int) this.getX() + (int) this.getWidth() - 5, (int) this.getY() + 15, 0, 0, 5, 80, 10, 80);
        //hue cursor
        Render2DHelper.INSTANCE.fill(matrixStack, this.getX() + this.getWidth() - 5, this.getY() + 15 + huepos - 1, this.getX() + this.getWidth(), this.getY() + 15 + huepos + 1, -1);

        FontHelper.INSTANCE.drawWithShadow(matrixStack, colorOption.getName(), this.getX() + 3, this.getY() + 3, colorOption.getValue());
        super.render(matrixStack);
    }

    @Override
    protected void handleSliders(ColorOption colorOption) {
        if (MouseHelper.INSTANCE.getMouseX() > this.getX() + this.getWidth() - 6) {
            float position = MouseHelper.INSTANCE.getMouseY() - (this.getY() + 15);
            float percent = Mth.clamp(position / 79, 0, 1);
            float value = percent * 270;
            colorOption.setH((int) value);
        } else {
            float position = MouseHelper.INSTANCE.getMouseX() - (this.getX() + 5);
            float percent = Mth.clamp(position / 80, 0, 1);
            colorOption.setS(percent);

            position = MouseHelper.INSTANCE.getMouseY() - (this.getY() + 15);
            percent = Mth.clamp(position / 79, 0, 1);
            colorOption.setB(1 - percent);
        }
        colorOption.setValue(ColorHelper.INSTANCE.getColorViaHue(colorOption.getH(), colorOption.getS(), colorOption.getB()).getRGB());
    }
}
