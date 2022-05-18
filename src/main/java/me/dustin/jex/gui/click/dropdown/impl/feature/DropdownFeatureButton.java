package me.dustin.jex.gui.click.dropdown.impl.feature;

import me.dustin.jex.JexClient;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.Option;
import me.dustin.jex.feature.option.types.*;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.gui.click.dropdown.impl.button.DropdownButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import net.minecraft.client.util.math.MatrixStack;
import me.dustin.jex.gui.click.dropdown.impl.option.*;


public class DropdownFeatureButton extends DropdownButton {
    private final Feature feature;

    protected int buttonsHeight;
    public DropdownFeatureButton(DropdownWindow window, Feature feature, float x, float y, float width, float height) {
        super(window, feature.getName(), x, y, width, height, null);
        this.feature = feature;
    }

    @Override
    public void render(MatrixStack matrixStack) {

    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        if (isHovered()) {
            switch (int_1) {
                case 0:
                    this.feature.toggleState();
                    if (JexClient.INSTANCE.isAutoSaveEnabled())
                        ConfigManager.INSTANCE.get(FeatureFile.class).write();
                    break;
                case 1:
                    if (isOpen())
                        close();
                    else
                        open();
                    break;

            }
        } else {
            this.getChildren().forEach(dropdownButton -> dropdownButton.click(double_1, double_2, int_1));
        }
    }

    @Override
    public void tick() {
        this.getChildren().forEach(dropdownButton -> dropdownButton.tick());
        super.tick();
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() && feature != null && Feature.get(feature.getName()) != null;
    }

    public void open() {
        setOpen(true);
        buttonsHeight = 0;
        addExtraButtons();
    }

    public void close() {
        setOpen(false);
        this.getChildren().forEach(button -> {
            if (button instanceof DropdownOptionButton dropdownOptionButton) {
                if (dropdownOptionButton.isOpen())
                    dropdownOptionButton.close();
            }
        });
        this.getChildren().forEach(dropdownButton -> {
            if (dropdownButton instanceof DropdownKeybindButton keybindButton)
                keybindButton.unregister();
        });
        allButtonsAfter().forEach(button -> button.move(0, -buttonsHeight));
        this.getChildren().clear();
    }

    public void openOption(DropdownOptionButton dropdownOptionButton) {

    }

    public void addExtraButtons() {
    }

    public Feature getFeature() {
        return this.feature;
    }
}
