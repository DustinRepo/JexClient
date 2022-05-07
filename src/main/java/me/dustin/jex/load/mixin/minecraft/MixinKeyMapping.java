package me.dustin.jex.load.mixin.minecraft;

import com.mojang.blaze3d.platform.InputConstants;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.load.impl.IKeyBinding;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(KeyMapping.class)
public class MixinKeyMapping implements IKeyBinding {

    @Shadow private InputConstants.Key key;

    @Override
    public boolean isActuallyPressed() {
        long handle = Wrapper.INSTANCE.getWindow().getWindow();
        int code = this.key.getValue();
        return InputConstants.isKeyDown(handle, code);
    }
}
