package me.dustin.jex.gui.jex.selection.button;

import me.dustin.jex.helper.render.Button;
import me.dustin.jex.helper.render.ButtonListener;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ItemButton extends Button {

    private Item item;
    private boolean selected;

    public ItemButton(Item item, String name, float x, float y, float width, float height, ButtonListener listener) {
        super(name, x, y, width, height, listener);
        this.item = item;
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (isSelected()) {
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), ColorHelper.INSTANCE.getClientColor(), 0x25ffffff, 1);
        }
        FontHelper.INSTANCE.drawCenteredString(matrixStack, Text.translatable(item.getTranslationKey()), this.getX() + (this.getWidth() / 2), this.getY() + (this.getHeight() / 2) - 4, isEnabled() ? 0xffaaaaaa : 0xff676767);

        Render2DHelper.INSTANCE.drawItem(new ItemStack(item), (int) (getX() + 2), (int) (getY() + 2));
        if (isHovered() && isEnabled())
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x25ffffff);
        this.getChildren().forEach(button -> {
            button.render(matrixStack);
        });
    }

    public Item getItem() {
        return this.item;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
