package me.dustin.jex.gui.click.dropdown.impl.option;

import me.dustin.events.EventManager;
import me.dustin.jex.JexClient;
import me.dustin.jex.feature.option.Option;
import me.dustin.jex.feature.option.types.*;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.gui.click.dropdown.impl.button.DropdownButton;
import me.dustin.jex.gui.click.dropdown.impl.feature.DropdownFeatureButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;

public class DropdownOptionButton extends DropdownButton {
    private final Option option;
    public int buttonsHeight;
    public DropdownOptionButton masterButton;
    public DropdownOptionButton parentButton;
    public DropdownOptionButton(DropdownWindow window, Option option, float x, float y, float width, float height) {
        super(window, option.getName(), x, y, width, height, null);
        this.option = option;
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (hasChild())
            FontHelper.INSTANCE.drawWithShadow(matrixStack, ">", getX() + getWidth() - FontHelper.INSTANCE.getStringWidth(">") - 2, getY() + 2, isOpen() ? getWindow().getColor() : -1);
        if (isOpen())
            getChildren().forEach(dropdownButton -> {
                dropdownButton.render(matrixStack);
            });
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        if (isHovered() && int_1 == 1) {
            if (hasChild()) {
                this.setOpen(!this.isOpen());
                if (this.isOpen())
                    this.open();
                else
                    this.close();
            }
        }
        getChildren().forEach(button -> button.click(double_1, double_2, int_1));
    }

    public void tick() {
        getChildren().forEach(button -> button.tick());
    }

    protected boolean hasChild() {
        if (this.getOption() instanceof StringArrayOption sArrayOption) {
            for (Option option : this.getOption().getChildren()) {
                if (option.hasDependency()) {
                    if (option.getDependency().equalsIgnoreCase(sArrayOption.getValue()))
                        return true;
                } else {
                    return true;
                }
            }
        } else {
            return this.getOption().hasChild();
        }

        return false;
    }

    public void open() {
        buttonsHeight = 0;
        for (DropdownButton button : getWindow().getButtons()) {
            if (button instanceof DropdownFeatureButton dropdownFeatureButton) {
                dropdownFeatureButton.openOption(this);
                return;
            }
        }
    }

    public void close() {
        unregister();
        this.getChildren().forEach(button -> {
            if (button instanceof DropdownOptionButton dropdownOptionButton) {
                if (button.isOpen())
                    dropdownOptionButton.close();
            }
        });
        allButtonsAfter().forEach(button -> button.move(0, -buttonsHeight));
        this.getChildren().clear();
        this.setOpen(false);
    }

    @Override
    public ArrayList<DropdownButton> allButtonsAfter() {
        ArrayList<DropdownButton> buttons = new ArrayList<>();

        if (parentButton != null)
            parentButton.getChildren().forEach(button -> {
                if (parentButton.getChildren().indexOf(button) > parentButton.getChildren().indexOf(this)) {
                    buttons.add(button);
                    addAllChildren(buttons, button);
                }
            });

        getWindow().get(this.getOption().getFeature()).getChildren().forEach(button -> {
            if (this.masterButton == null) {
                if (getWindow().get(this.getOption().getFeature()).getChildren().indexOf(button) > getWindow().get(this.getOption().getFeature()).getChildren().indexOf(this)) {
                    buttons.add(button);
                    addAllChildren(buttons, button);
                }
            } else {
                if (getWindow().get(this.getOption().getFeature()).getChildren().indexOf(button) > getWindow().get(this.getOption().getFeature()).getChildren().indexOf(masterButton)) {
                    buttons.add(button);
                    addAllChildren(buttons, button);
                }
            }
        });
        buttons.addAll(super.allButtonsAfter(getWindow().get(this.getOption().getFeature())));
        return buttons;
    }

    protected DropdownButton getVeryBottomOption() {
        if (getChildren().size() == 0)
            return null;
        DropdownButton last = getChildren().get(getChildren().size() - 1);
        while (last.hasChildren() && last.isOpen())
            last = last.getChildren().get(last.getChildren().size() - 1);
        return last;
    }

    public void unregister() {
        EventManager.unregister(this);
        if (JexClient.INSTANCE.isAutoSaveEnabled())
            ConfigManager.INSTANCE.get(FeatureFile.class).write();
    }

    public Option getOption() {
        return this.option;
    }
}
