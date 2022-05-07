package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.load.impl.IAbstractSliderButton;
import net.minecraft.client.gui.components.AbstractSliderButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractSliderButton.class)
public class MixinAbstractAbstractSliderButton implements IAbstractSliderButton {
    @Shadow protected double value;

    @Override
    public double getValue() {
        return this.value;
    }
}
