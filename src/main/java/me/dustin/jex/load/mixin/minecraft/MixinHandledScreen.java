package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.event.render.EventRenderToolTip;
import me.dustin.jex.load.impl.IHandledScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class MixinHandledScreen implements IHandledScreen {

    @Shadow
    protected Slot focusedSlot;
    @Shadow
    protected int x;//x
    @Shadow
    protected int y;//y

    @Shadow @Final protected ScreenHandler handler;

    @Inject(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "net/minecraft/client/gui/screen/ingame/HandledScreen.renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemStack;II)V"), cancellable = true)
    public void drawMouseoverTooltip(MatrixStack matrixStack, int i, int j, CallbackInfo ci) {
        EventRenderToolTip eventRenderToolTip = new EventRenderToolTip(matrixStack, EventRenderToolTip.Mode.PRE, this.focusedSlot.getStack()).run();
        if (eventRenderToolTip.isCancelled())
            ci.cancel();
    }

    @Inject(method = "drawMouseoverTooltip", at = @At(value = "RETURN"), cancellable = true)
    public void drawMouseoverTooltipPOST(MatrixStack matrixStack, int i, int j, CallbackInfo ci) {
        if (this.handler.getCursorStack().isEmpty() && this.focusedSlot != null && this.focusedSlot.hasStack()) {
            EventRenderToolTip eventRenderToolTip = new EventRenderToolTip(matrixStack, EventRenderToolTip.Mode.POST, this.focusedSlot.getStack()).run();
            if (eventRenderToolTip.isCancelled())
                ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void render(MatrixStack matrixStack, int mouseY, int i, float f, CallbackInfo ci) {
        new EventDrawScreen((Screen) (Object) this, matrixStack, EventDrawScreen.Mode.POST_CONTAINER).run();
    }

    @Override
    public Slot focusedSlot() {
        return focusedSlot;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }
}
