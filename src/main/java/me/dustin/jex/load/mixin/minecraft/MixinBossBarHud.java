package me.dustin.jex.load.mixin.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import me.dustin.jex.feature.mod.impl.render.UIDisabler;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import me.dustin.jex.feature.property.Property;
import net.minecraft.client.util.math.MatrixStack;
import me.dustin.jex.event.render.EventRenderBossBar;
import net.minecraft.text.Text;
import java.util.Collection;
import java.util.Iterator;

@Mixin(BossBarHud.class)
public class MixinBossBarHud {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(CallbackInfo info) {
        if (bossbarProperty.value()) 
            info.cancel();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;"))
    public Iterator<ClientBossBar> onRender(Collection<ClientBossBar> collection) {
        EventRenderBossBar.BossIterator event = EventRenderBossBar(EventRenderBossBar.BossIterator.get(collection.iterator()));
        return event.iterator;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ClientBossBar;getName()Lnet/minecraft/text/Text;"))
    public Text onAsFormattedString(ClientBossBar clientBossBar) {
        EventRenderBossBar.BossText event = EventRenderBossBar(EventRenderBossBar.BossText.get(clientBossBar, clientBossBar.getName()));
        return event.name;
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 9, ordinal = 1))
    public int modifySpacingConstant(int j) {
        EventRenderBossBar.BossSpacing event = EventRenderBossBar(EventRenderBossBar.BossSpacing.get(j));
        return event.spacing;
    }
}
