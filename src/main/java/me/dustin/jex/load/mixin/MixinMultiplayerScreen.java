package me.dustin.jex.load.mixin;

import me.dustin.jex.gui.account.AccountManager;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public class MixinMultiplayerScreen extends Screen {

    protected MixinMultiplayerScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    public void init(CallbackInfo ci) {
        this.addButton(new ButtonWidget(2, 2, 75, 20, new LiteralText("Alt Manager"), button -> {
            Wrapper.INSTANCE.getMinecraft().openScreen(new AccountManager(new LiteralText("Account Manager")));
        }));
    }

}
