package me.dustin.jex.load.mixin.minecraft;

import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;
import me.dustin.jex.feature.mod.impl.render.UIDisabler;
import me.dustin.jex.event.render.EventRenderBossBar;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.feature.mod.core.Feature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;



@Mixin(BossBarHud.class)
public class MixinBossBarHud {
 @Inject(method = "render", at = @At(value = "HEAD", target = "Lnet/minecraft/client/gui/hud/ClientBossBar;render(Lnet/minecraft/client/util/math/MatrixStack;)V"))
    public void renderBossBarHud(ClientBossBar cbossbar, CallbackInfo info) {
     EventRenderBossBar eventRenderBossBar = new EventRenderBossBar(cbossbar).run();
        if (eventRenderBossBar.isCancelled()) {
            info.cancel();
        }
    }
}
