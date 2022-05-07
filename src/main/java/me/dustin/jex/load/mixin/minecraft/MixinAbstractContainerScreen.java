package me.dustin.jex.load.mixin.minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.event.render.EventRenderToolTip;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.ToolTips;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.load.impl.IHandledScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class MixinAbstractContainerScreen extends Screen implements IHandledScreen {

    @Shadow @Nullable protected Slot hoveredSlot;

    @Shadow @Final protected AbstractContainerMenu menu;

    @Shadow protected int leftPos;

    @Shadow protected int topPos;

    protected MixinAbstractContainerScreen(Component title) {
        super(title);
    }

    private static EventRenderToolTip.ToolTipData other;

    @Inject(method = "renderTooltip", at = @At(value = "HEAD"), cancellable = true)
    public void drawMouseoverTooltip(PoseStack matrixStack, int i, int j, CallbackInfo ci) {
        ItemStack stack = hoveredSlot != null ? this.hoveredSlot.getItem() : ItemStack.EMPTY;
        EventRenderToolTip eventRenderToolTip = new EventRenderToolTip(matrixStack, EventRenderToolTip.Mode.PRE, i, j, stack).run();
        if (eventRenderToolTip.getX() != i || eventRenderToolTip.getY() != j || eventRenderToolTip.getItemStack() != stack) {
            this.renderTooltip(matrixStack, eventRenderToolTip.getItemStack(), eventRenderToolTip.getX(), eventRenderToolTip.getY());
            ci.cancel();
            if (eventRenderToolTip.getOther() != null) {
                toolTipRender(matrixStack, eventRenderToolTip.getOther().itemStack(), eventRenderToolTip.getOther().x(), eventRenderToolTip.getOther().y());
                if (eventRenderToolTip.getOther().itemStack().getItem() == Items.FILLED_MAP && Feature.getState(ToolTips.class) && Feature.get(ToolTips.class).mapToolTip) {
                    Render2DHelper.INSTANCE.drawMap(eventRenderToolTip.getPoseStack(), eventRenderToolTip.getOther().x() + 9, eventRenderToolTip.getOther().y() - 165, eventRenderToolTip.getOther().itemStack());
                }
            }
            return;
        } else if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            if (eventRenderToolTip.isCancelled()) {
                ci.cancel();
                return;
            }
        }
        if (eventRenderToolTip.getOther() != null)
            other = eventRenderToolTip.getOther();
    }

    @Inject(method = "renderTooltip", at = @At(value = "RETURN"))
    public void drawMouseoverTooltipPOST(PoseStack matrixStack, int i, int j, CallbackInfo ci) {
        EventRenderToolTip eventRenderToolTip = new EventRenderToolTip(matrixStack, EventRenderToolTip.Mode.POST, i, j, hoveredSlot != null ? this.hoveredSlot.getItem() : ItemStack.EMPTY).run();
        if (eventRenderToolTip.getOther() != null)
            other = eventRenderToolTip.getOther();
        if (other != null) {
            toolTipRender(matrixStack, other.itemStack(), other.x(), other.y());
            if (eventRenderToolTip.getOther().itemStack().getItem() == Items.FILLED_MAP && Feature.getState(ToolTips.class) && Feature.get(ToolTips.class).mapToolTip) {
                Render2DHelper.INSTANCE.drawMap(eventRenderToolTip.getPoseStack(), eventRenderToolTip.getOther().x() + 9, eventRenderToolTip.getOther().y() - 165, eventRenderToolTip.getOther().itemStack());
            }
        }
        other = null;
    }

    private void toolTipRender(PoseStack matrixStack, ItemStack itemStack, int x, int y) {
        matrixStack.pushPose();
        matrixStack.translate(0, 0, 50);
        this.renderTooltip(matrixStack, itemStack, x, y);
        matrixStack.translate(0, 0, -50);
        matrixStack.popPose();
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void render(PoseStack matrixStack, int mouseY, int i, float f, CallbackInfo ci) {
        new EventDrawScreen( this, matrixStack, EventDrawScreen.Mode.POST_CONTAINER).run();
    }

    @Override
    public Slot focusedSlot() {
        return hoveredSlot;
    }

    @Override
    public int getX() {
        return this.leftPos;
    }

    @Override
    public int getY() {
        return this.topPos;
    }
}
