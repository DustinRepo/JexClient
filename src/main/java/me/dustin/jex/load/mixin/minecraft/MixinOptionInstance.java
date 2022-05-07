package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.misc.EventSetOptionInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.Consumer;

@Mixin(OptionInstance.class)
public class MixinOptionInstance<T> {

    @Shadow private T value;

    @Shadow @Final private Consumer<T> onValueUpdate;

    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    public void setValue(T object, CallbackInfo ci) {
        EventSetOptionInstance eventSetOptionInstance = new EventSetOptionInstance(getMe()).run();
        if (eventSetOptionInstance.isCancelled()) {
            ci.cancel();
        } else if (eventSetOptionInstance.isShouldIgnoreCheck()) {
            if (!Minecraft.getInstance().isRunning()) {
                this.value = object;
            } else {
                if (!Objects.equals(this.value, object)) {
                    this.value = object;
                    this.onValueUpdate.accept(this.value);
                }

            }
            ci.cancel();
        }
    }

    public OptionInstance<?> getMe() {
        return (OptionInstance<?>) (Object)this;
    }
}
