package me.dustin.jex.load.mixin;

import me.dustin.jex.gui.minecraft.JexOptionsScreen;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
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
        this.method_37063(new ButtonWidget(2, 2, 80, 20, new LiteralText("Jex Options"), button -> {
            Wrapper.INSTANCE.getMinecraft().openScreen(new JexOptionsScreen());
        }));
    }

}
