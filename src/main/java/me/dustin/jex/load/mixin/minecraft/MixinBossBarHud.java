package me.dustin.jex.load.mixin.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import me.dustin.jex.feature.mod.impl.render.UIDisabler;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.util.math.MatrixStack;
import me.dustin.jex.event.render.EventRenderBossBar;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.text.Text;
import java.util.Collection;
import java.util.Iterator;

@Mixin(BossBarHud.class)
public class MixinBossBarHud {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(CallbackInfo info) {
        if (Feature.get(UIDisabler.class).onEnable()) 
            info.cancel();
    }
}
