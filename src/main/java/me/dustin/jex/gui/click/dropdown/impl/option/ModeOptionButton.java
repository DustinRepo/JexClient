package me.dustin.jex.gui.click.dropdown.impl.option;

import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.filters.DrawScreenFilter;
import me.dustin.jex.event.filters.MousePressFilter;
import me.dustin.jex.event.misc.EventMouseButton;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.feature.option.types.StringArrayOption;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.file.impl.GuiFile;
import me.dustin.jex.gui.click.dropdown.DropDownGui;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;

public class ModeOptionButton extends DropdownOptionButton {
    protected StringArrayOption stringArrayOption;
    protected boolean isSelecting;
    public ModeOptionButton(DropdownWindow window, StringArrayOption option, float x, float y, float width, float height) {
        super(window, option, x, y, width, height);
        this.stringArrayOption = option;
    }

    @Override
    public void render(MatrixStack matrixStack) {
        super.render(matrixStack);
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        if (isSelecting && !isHovered() || int_1 != 0)
            if (JexClient.INSTANCE.isAutoSaveEnabled())
                ConfigManager.INSTANCE.get(FeatureFile.class).write();
        isSelecting = isHovered() && int_1 == 0;
        if (isSelecting && !EventManager.isRegistered(this))
            EventManager.register(this);
        super.click(double_1, double_2, int_1);
    }
}
