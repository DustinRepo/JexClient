package me.dustin.jex.gui.click.dropdown.impl.option;

import me.dustin.jex.JexClient;
import me.dustin.jex.feature.option.types.BoolOption;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.file.impl.GuiFile;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import net.minecraft.client.util.math.MatrixStack;

public class BooleanOptionButton extends DropdownOptionButton {
    protected final BoolOption boolOption;
    public BooleanOptionButton(DropdownWindow window, BoolOption option, float x, float y, float width, float height) {
        super(window, option, x, y, width, height);
        this.boolOption = option;
    }

    @Override
    public void render(MatrixStack matrixStack) {
        super.render(matrixStack);
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        super.click(double_1, double_2, int_1);
        if (isHovered() && int_1 == 0)
            boolOption.setValue(!boolOption.getValue());
        if (JexClient.INSTANCE.isAutoSaveEnabled())
            ConfigManager.INSTANCE.get(FeatureFile.class).write();
    }
}
