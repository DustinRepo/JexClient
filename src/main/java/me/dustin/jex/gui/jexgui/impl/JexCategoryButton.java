package me.dustin.jex.gui.jexgui.impl;

import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.helper.render.Button;
import me.dustin.jex.helper.render.ButtonListener;
import net.minecraft.client.util.math.MatrixStack;

public class JexCategoryButton extends Button {
    private final Category category;
    public JexCategoryButton(Category category, float x, float y, float width, float height, ButtonListener listener) {
        super(category.name(), x, y, width, height, listener);
        this.category = category;
        setBackgroundColor(0xa0000000);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        setTextColor(isHovered() ? category.color() : 0xff676767);
        super.render(matrixStack);
    }

    public Category getCategory() {
        return this.category;
    }
}
