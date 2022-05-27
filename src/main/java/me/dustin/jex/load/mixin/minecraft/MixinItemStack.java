package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.misc.EventItemStackDecrement;
import me.dustin.jex.event.misc.EventItemStackSetCount;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Shadow
    public abstract int getCount();

    @Inject(method = "setCount", at = @At("HEAD"))
    public void setCountPre(int count, CallbackInfo ci) {
        new EventItemStackSetCount(EventItemStackSetCount.Mode.PRE, (ItemStack)(Object)this, count).run();
    }

    @Inject(method = "setCount", at = @At("HEAD"))
    public void setCountPost(int count, CallbackInfo ci) {
        new EventItemStackSetCount(EventItemStackSetCount.Mode.POST, (ItemStack)(Object)this, count).run();
    }

    @Inject(method = "decrement", at = @At("HEAD"))
    public void decrementPre(int amount, CallbackInfo ci) {
        new EventItemStackDecrement(EventItemStackDecrement.Mode.PRE, (ItemStack)(Object)this, amount, this.getCount()).run();
    }

    @Inject(method = "decrement", at = @At("RETURN"))
    public void decrementPost(int amount, CallbackInfo ci) {
        new EventItemStackDecrement(EventItemStackDecrement.Mode.POST, (ItemStack)(Object)this, amount, this.getCount()).run();
    }


}
