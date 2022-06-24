package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.world.EventPlaySound;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
public class MixinSoundSystem {

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    private void onPlayPre(SoundInstance soundInstance, CallbackInfo ci) {
        EventPlaySound eventPlaySound = new EventPlaySound(EventPlaySound.Mode.PRE, soundInstance.getId()).run();
        if (eventPlaySound.isCancelled())
            ci.cancel();
    }

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("RETURN"), cancellable = true)
    private void onPlayPost(SoundInstance soundInstance, CallbackInfo ci) {
        EventPlaySound eventPlaySound = new EventPlaySound(EventPlaySound.Mode.POST, soundInstance.getId()).run();
        if (eventPlaySound.isCancelled())
            ci.cancel();
    }
}