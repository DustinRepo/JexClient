package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.load.impl.ISliderWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SliderWidget.class)
public class MixinSliderWidget implements ISliderWidget {
    @Shadow protected double value;

    @Override
    public double getValue() {
        return this.value;
    }
}
