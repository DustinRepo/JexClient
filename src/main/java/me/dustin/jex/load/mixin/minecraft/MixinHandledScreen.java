package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.event.render.EventRenderToolTip;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.ToolTips;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.load.impl.IHandledScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class MixinHandledScreen extends Screen implements IHandledScreen {

    @Shadow @Nullable protected Slot focusedSlot;

    @Shadow @Final protected ScreenHandler handler;

    @Shadow protected int x;

    @Shadow protected int y;

    protected MixinHandledScreen(Text title) {
        super(title);
    }

    private static EventRenderToolTip.ToolTipData other;

    @Inject(method = "drawMouseoverTooltip", at = @At(value = "HEAD"), cancellable = true)
    public void drawMouseoverTooltip(MatrixStack matrixStack, int i, int j, CallbackInfo ci) {
        ItemStack stack = focusedSlot != null ? this.focusedSlot.getStack() : ItemStack.EMPTY;
        EventRenderToolTip eventRenderToolTip = new EventRenderToolTip(matrixStack, EventRenderToolTip.Mode.PRE, i, j, stack).run();
        if (eventRenderToolTip.getX() != i || eventRenderToolTip.getY() != j || eventRenderToolTip.getItemStack() != stack) {
            this.renderTooltip(matrixStack, eventRenderToolTip.getItemStack(), eventRenderToolTip.getX(), eventRenderToolTip.getY());
            ci.cancel();
            if (eventRenderToolTip.getOther() != null) {
                toolTipRender(matrixStack, eventRenderToolTip.getOther().itemStack(), eventRenderToolTip.getOther().x(), eventRenderToolTip.getOther().y());
                if (eventRenderToolTip.getOther().itemStack().getItem() == Items.FILLED_MAP && Feature.getState(ToolTips.class) && Feature.get(ToolTips.class).mapToolTipProperty.value()) {
                    Render2DHelper.INSTANCE.drawMap(eventRenderToolTip.getPoseStack(), eventRenderToolTip.getOther().x() + 9, eventRenderToolTip.getOther().y() - 165, eventRenderToolTip.getOther().itemStack());
                }
            }
            return;
        } else if (this.handler.getCursorStack().isEmpty() && this.focusedSlot != null && this.focusedSlot.hasStack()) {
            if (eventRenderToolTip.isCancelled()) {
                ci.cancel();
                return;
            }
        }
        if (eventRenderToolTip.getOther() != null)
            other = eventRenderToolTip.getOther();
    }

    @Inject(method = "drawMouseoverTooltip", at = @At(value = "RETURN"))
    public void drawMouseoverTooltipPOST(MatrixStack matrixStack, int i, int j, CallbackInfo ci) {
        EventRenderToolTip eventRenderToolTip = new EventRenderToolTip(matrixStack, EventRenderToolTip.Mode.POST, i, j, focusedSlot != null ? this.focusedSlot.getStack() : ItemStack.EMPTY).run();
        if (eventRenderToolTip.getOther() != null)
            other = eventRenderToolTip.getOther();
        if (other != null) {
            toolTipRender(matrixStack, other.itemStack(), other.x(), other.y());
            if (eventRenderToolTip.getOther().itemStack().getItem() == Items.FILLED_MAP && Feature.getState(ToolTips.class) && Feature.get(ToolTips.class).mapToolTipProperty.value()) {
                Render2DHelper.INSTANCE.drawMap(eventRenderToolTip.getPoseStack(), eventRenderToolTip.getOther().x() + 9, eventRenderToolTip.getOther().y() - 165, eventRenderToolTip.getOther().itemStack());
            }
        }
        other = null;
    }

    private void toolTipRender(MatrixStack matrixStack, ItemStack itemStack, int x, int y) {
        matrixStack.push();
        matrixStack.translate(0, 0, 50);
        this.renderTooltip(matrixStack, itemStack, x, y);
        matrixStack.translate(0, 0, -50);
        matrixStack.pop();
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void render(MatrixStack matrixStack, int mouseY, int i, float f, CallbackInfo ci) {
        new EventDrawScreen( this, matrixStack, EventDrawScreen.Mode.POST_CONTAINER).run();
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
