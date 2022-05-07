package me.dustin.jex.gui.jex.selection.button;

import me.dustin.jex.helper.render.Button;
import me.dustin.jex.helper.render.ButtonListener;
import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import me.dustin.jex.helper.render.Render2DHelper;

public class BlockButton extends Button {

    private Block block;
    private boolean selected;

    public BlockButton(Block block, String name, float x, float y, float width, float height, ButtonListener listener) {
        super(name, x, y, width, height, listener);
        this.block = block;
    }

    @Override
    public void render(PoseStack matrixStack) {
        if (isSelected()) {
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), ColorHelper.INSTANCE.getClientColor(), 0x25ffffff, 1);
        }
        FontHelper.INSTANCE.drawCenteredString(matrixStack, Component.translatable(block.getDescriptionId()), this.getX() + (this.getWidth() / 2), this.getY() + (this.getHeight() / 2) - 4, isEnabled() ? 0xffaaaaaa : 0xff676767);

        Render2DHelper.INSTANCE.drawItem(new ItemStack(block.asItem()), (int) (getX() + 2), (int) (getY() + 2));
        if (isHovered() && isEnabled())
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x25ffffff);
        this.getChildren().forEach(button -> {
            button.render(matrixStack);
        });
    }

    public Block getBlock() {
        return block;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
