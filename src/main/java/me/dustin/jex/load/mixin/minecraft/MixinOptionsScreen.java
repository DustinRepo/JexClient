package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.gui.jex.JexOptionsScreen;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class MixinOptionsScreen extends Screen {

    protected MixinOptionsScreen(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    public void init(CallbackInfo ci) {
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 + 143, 200, 20, Component.nullToEmpty(ChatFormatting.AQUA + "Jex Options"),button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new JexOptionsScreen());
        }));
    }

}
