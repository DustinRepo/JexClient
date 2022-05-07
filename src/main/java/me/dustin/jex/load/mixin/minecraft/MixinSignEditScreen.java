package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.world.AutoSign;
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

    protected MixinSignEditScreen(Component text_1) {
        super(text_1);
    }

    @Inject(method = "init", at = @At("RETURN"))
    public void init(CallbackInfo ci) {
        AutoSign autoSign = Feature.get(AutoSign.class);
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 142, 200, 20, Component.nullToEmpty("Set AutoSign Text"), (buttonWidget_1) -> {
            autoSign.signText[0] = sign.getMessage(0, false);
            autoSign.signText[1] = sign.getMessage(1, false);
            autoSign.signText[2] = sign.getMessage(2, false);
            autoSign.signText[3] = sign.getMessage(3, false);
            finishEditing();
        }));
    }

    private void finishEditing() {
        this.sign.setChanged();
        Wrapper.INSTANCE.getMinecraft().setScreen(null);
    }

}
