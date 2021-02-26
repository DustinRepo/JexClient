package me.dustin.jex.load.mixin;

import me.dustin.jex.load.impl.IKeyBinding;
import net.minecraft.client.options.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(KeyBinding.class)
public class MixinKeybinding implements IKeyBinding {

    @Shadow
    private boolean pressed;

    public boolean getPressed() {
        return pressed;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

}
