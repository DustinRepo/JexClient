package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.world.AutoSign;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SignEditScreen.class)
public class MixinSignEditScreen extends Screen {

    @Shadow
    @Final
    private SignBlockEntity sign;

    protected MixinSignEditScreen(Text text_1) {
        super(text_1);
    }

    @Inject(method = "init", at = @At("RETURN"))
    public void init(CallbackInfo ci) {
        AutoSign autoSign = Feature.get(AutoSign.class);
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 142, 200, 20, new LiteralText("Set AutoSign Text"), (buttonWidget_1) -> {
            autoSign.signText[0] = sign.getTextOnRow(0, false);
            autoSign.signText[1] = sign.getTextOnRow(1, false);
            autoSign.signText[2] = sign.getTextOnRow(2, false);
            autoSign.signText[3] = sign.getTextOnRow(3, false);
            finishEditing();
        }));
    }

    private void finishEditing() {
        this.sign.markDirty();
        Wrapper.INSTANCE.getMinecraft().setScreen(null);
    }

}
