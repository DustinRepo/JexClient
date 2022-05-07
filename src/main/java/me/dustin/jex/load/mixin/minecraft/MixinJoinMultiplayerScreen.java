package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.gui.account.AccountManagerScreen;
import me.dustin.jex.gui.thealtening.TheAlteningScreen;
import me.dustin.jex.gui.mcleaks.MCLeaksScreen;
import me.dustin.jex.gui.proxy.ProxyScreen;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public class MixinJoinMultiplayerScreen extends Screen {

    protected MixinJoinMultiplayerScreen(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    public void init(CallbackInfo ci) {
        this.addRenderableWidget(new Button(2, 2, 75, 20, Component.nullToEmpty("Alt Manager"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new AccountManagerScreen());
        }));
        this.addRenderableWidget(new Button(79, 2, 75, 20, Component.nullToEmpty("TheAltening"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new TheAlteningScreen((JoinMultiplayerScreen)(Object)this));
        }));
        this.addRenderableWidget(new Button(156, 2, 75, 20, Component.nullToEmpty("MCLeaks"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new MCLeaksScreen((JoinMultiplayerScreen)(Object)this, false));
        }));
        this.addRenderableWidget(new Button(width - 77, 2, 75, 20, Component.nullToEmpty("Proxy"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new ProxyScreen());
        }));
    }

}
