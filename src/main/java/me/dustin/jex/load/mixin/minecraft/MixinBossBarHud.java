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
 @Redirect(method = "render", at = @At(value = "INVOKE", cancellable = true, target = "Lnet/minecraft/client/gui/hud/BossBarHud;render(Lnet/minecraft/client/util/math/MatrixStack;)V"))
    public void renderBossBarHud(BossBarHud bossbarhud, MatrixStack matrixStack,CallbackInfo ci) {
        EventRenderBossBar eventRenderBossBar = new EventRenderBossBar(matrixStack).run();
        if (eventRenderBossBar.isCancelled()) {
            ();
        }
    }
 
 @Inject(at=@At("HEAD"), method = "render", cancellable = true)
    public void render(CallbackInfo info) {
        if (UIDisabler.INSTANCE.bossbarProperty) {
            info.cancel();
        }
    }
