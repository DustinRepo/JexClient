package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.load.impl.IKeyBinding;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(KeyBinding.class)
public class MixinKeybinding implements IKeyBinding {

    @Shadow private InputUtil.Key boundKey;

    @Override
    public boolean isActuallyPressed() {
        long handle = Wrapper.INSTANCE.getWindow().getHandle();
        int code = this.boundKey.getCode();
        return InputUtil.isKeyPressed(handle, code);
    }
}
