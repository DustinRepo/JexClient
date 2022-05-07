package me.dustin.jex.gui.click.dropdown.impl.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.gui.click.dropdown.impl.button.DropdownButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import org.apache.commons.lang3.StringUtils;

public class DropdownVisibleButton extends DropdownButton {
    private final Feature feature;
    public DropdownVisibleButton(DropdownWindow window, Feature feature, float x, float y, float width, float height) {
        super(window, "Visible: " + StringUtils.capitalize(String.valueOf(feature.isVisible())), x, y, width, height, null);
        this.feature = feature;
    }

    @Override
    public void render(PoseStack matrixStack) {
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        if (isHovered() && int_1 == 0) {
            feature.setVisible(!feature.isVisible());
            this.setName("Visible: " + StringUtils.capitalize(String.valueOf(feature.isVisible())));
        }
    }

    public Feature getFeature() {
        return this.feature;
    }
}
