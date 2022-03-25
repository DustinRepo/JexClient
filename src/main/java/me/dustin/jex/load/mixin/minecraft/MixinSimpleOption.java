package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.misc.EventSetSimpleOption;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.Consumer;

@Mixin(SimpleOption.class)
public class MixinSimpleOption<T> {

    @Shadow private T value;

    @Shadow @Final private Consumer<T> changeCallback;

    @Inject(method = "setValue", at = @At("HEAD"), cancellable = true)
    public void setValue(T object, CallbackInfo ci) {
        EventSetSimpleOption eventSetSimpleOption = new EventSetSimpleOption(getMe()).run();
        if (eventSetSimpleOption.isCancelled()) {
            ci.cancel();
        } else if (eventSetSimpleOption.isShouldIgnoreCheck()) {
            if (!MinecraftClient.getInstance().isRunning()) {
                this.value = object;
            } else {
                if (!Objects.equals(this.value, object)) {
                    this.value = object;
                    this.changeCallback.accept(this.value);
                }

            }
            ci.cancel();
        }
    }

    public SimpleOption<?> getMe() {
        return (SimpleOption<?>) (Object)this;
    }
}
