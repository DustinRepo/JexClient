package me.dustin.jex.load.mixin.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import me.dustin.jex.feature.mod.impl.render.UIDisabler;
import net.minecraft.client.gui.hud.BossBarHud;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.feature.mod.core.FeatureManager;
import net.minecraft.client.util.math.MatrixStack;
import me.dustin.jex.event.render.EventRenderBossBar;

@Mixin(BossBarHud.class)
public class MixinBossBarHud {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(CallbackInfo info) {
        if (bossbarProperty.value()) 
            info.cancel();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;"))
    public Iterator<ClientBossBar> onRender(Collection<ClientBossBar> collection) {
        RenderBossBarEvent.BossIterator event = new EventRenderBossBar(EventRenderBossBar.BossIterator.get(collection.iterator()));
        return event.iterator;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ClientBossBar;getName()Lnet/minecraft/text/Text;"))
    public Text onAsFormattedString(ClientBossBar clientBossBar) {
        EventRenderBossBar.BossText event = new EventRenderBossBar(EventRenderBossBar.BossText.get(clientBossBar, clientBossBar.getName()));
        return event.name;
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 9, ordinal = 1))
    public int modifySpacingConstant(int j) {
        EventRenderBossBar.BossSpacing event = new EventRenderBossBar(EventRenderBossBar.BossSpacing.get(j));
        return event.spacing;
    }
}
