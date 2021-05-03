package me.dustin.jex.gui.minecraft.blocklist.button;

import me.dustin.jex.gui.click.window.impl.Button;
import me.dustin.jex.gui.click.window.listener.ButtonListener;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;

public class BlockButton extends Button {

    private Block block;
    private boolean selected;

    public BlockButton(Block block, String name, float x, float y, float width, float height, ButtonListener listener) {
        super(null, name, x, y, width, height, listener);
        this.block = block;
    }

    @Override
    public void draw(MatrixStack matrixStack) {
        if (isSelected()) {
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), ColorHelper.INSTANCE.getClientColor(), 0x25ffffff, 1);
        }
        FontHelper.INSTANCE.drawCenteredString(matrixStack, new TranslatableText(block.getTranslationKey()), this.getX() + (this.getWidth() / 2), this.getY() + (this.getHeight() / 2) - 4, isEnabled() ? 0xffaaaaaa : 0xff676767);

        Wrapper.INSTANCE.getMinecraft().getItemRenderer().renderInGui(new ItemStack(block.asItem()), (int) (getX() + 2), (int) (getY() + 2));
        if (isHovered() && isEnabled())
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x25ffffff);
        this.getChildren().forEach(button -> {
            button.draw(matrixStack);
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
