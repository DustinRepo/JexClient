package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.misc.EventItemStackDecrement;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @Inject(method = "shrink", at = @At("HEAD"))
    public void decrement1(int amount, CallbackInfo ci) {
        new EventItemStackDecrement((ItemStack)(Object)this, amount).run();
    }

}
