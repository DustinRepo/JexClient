package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.gui.jex.JexOptionsScreen;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class MixinOptionsScreen extends Screen {

    protected MixinOptionsScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    public void init(CallbackInfo ci) {
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 6 + 143, 200, 20, Text.of(Formatting.AQUA + "Jex Options"),button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new JexOptionsScreen());
        }));
    }

}
