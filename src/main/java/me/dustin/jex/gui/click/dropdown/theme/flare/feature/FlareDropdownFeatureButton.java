package me.dustin.jex.gui.click.dropdown.theme.flare.feature;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.types.*;
import me.dustin.jex.gui.click.dropdown.impl.button.DropdownButton;
import me.dustin.jex.gui.click.dropdown.impl.feature.DropdownFeatureButton;
import me.dustin.jex.gui.click.dropdown.impl.option.DropdownOptionButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.gui.click.dropdown.theme.flare.option.*;
import me.dustin.jex.gui.click.dropdown.theme.jex.option.*;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;

public class FlareDropdownFeatureButton extends DropdownFeatureButton {
    public FlareDropdownFeatureButton(DropdownWindow window, Feature feature, float x, float y, float width, float height) {
        super(window, feature, x, y, width, height);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        FontHelper.INSTANCE.drawWithShadow(matrixStack, isOpen() ? "-" : "+", getX() + 2, getY()  + (getHeight() / 2 - 4), isOpen() ? 0xff00ffff : -1);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, getFeature().getName(), getX() + 10, getY()  + (getHeight() / 2 - 4), -1);
        int colors[] = getFeature().getState() ? new int[]{0xff007a21, 0xff004600} : new int[]{0xff990014, 0xff550000};
        String onOffString = getFeature().getState() ? "ON" : "OFF";
        Render2DHelper.INSTANCE.gradientFill(matrixStack, getX() + getWidth() - 8 - FontHelper.INSTANCE.getStringWidth("OFF"), getY() + 2, getX() + getWidth() - 4, getY() + getHeight() - 2, colors[0], colors[1]);
        Render2DHelper.INSTANCE.fillAndBorder(matrixStack, getX() + getWidth() - 8 - FontHelper.INSTANCE.getStringWidth("OFF") - 1, getY() + 2 - 1, getX() + getWidth() - 4 + 1, getY() + getHeight() - 2 + 1, 0xff999999, 0x00ffffff, 1);
        FontHelper.INSTANCE.drawCenteredString(matrixStack, onOffString, getX() + getWidth() - 6 - FontHelper.INSTANCE.getStringWidth("OFF") / 2, getY()  + (getHeight() / 2 - 4), -1);
        if (isOpen()) {
            DropdownButton bottomOption = getVeryBottomOption();
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, getX() + 2, getY() + getHeight() + getWindow().getTheme().getOptionOffset() - 2, getX() + getWidth() - 2, bottomOption.getY() + bottomOption.getHeight() + (bottomOption instanceof DropdownOptionButton dropdownOptionButton && dropdownOptionButton.parentButton != null && dropdownOptionButton.parentButton.isOpen() ? getWindow().getTheme().getOptionOffset() : 0) + 2, 0xaa999999, 0x50000000, 1);
            this.getChildren().forEach(dropdownButton -> {
                dropdownButton.render(matrixStack);
            });
        }
        super.render(matrixStack);
    }

    @Override
    public void open() {
        super.open();
        getFeature().getOptions().forEach(option -> {
            if (!option.hasParent()) {
                if (option instanceof FloatOption floatOption) {
                    FlareFloatOptionButton floatOptionButton = new FlareFloatOptionButton(this.getWindow(), floatOption, getX() + getWindow().getTheme().getOptionWidthOffset(), (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 17);
                    this.getChildren().add(floatOptionButton);
                    buttonsHeight += floatOptionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                    return;
                } else if (option instanceof IntOption intOption) {
                    FlareIntOptionButton intOptionButton = new FlareIntOptionButton(this.getWindow(), intOption, getX() + getWindow().getTheme().getOptionWidthOffset(), (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 17);
                    this.getChildren().add(intOptionButton);
                    buttonsHeight += intOptionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                    return;
                } else if (option instanceof ColorOption colorOption) {
                    FlareColorOptionButton colorOptionButton = new FlareColorOptionButton(this.getWindow(), colorOption, getX() + getWindow().getTheme().getOptionWidthOffset(), (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 100);
                    this.getChildren().add(colorOptionButton);
                    buttonsHeight += colorOptionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                    return;
                } else if (option instanceof KeybindOption keybindOption) {
                    FlareKeybindOptionButton keybindOptionButton = new FlareKeybindOptionButton(this.getWindow(), keybindOption, getX() + getWindow().getTheme().getOptionWidthOffset(), (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, this.getHeight());
                    this.getChildren().add(keybindOptionButton);
                    buttonsHeight += keybindOptionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                    return;
                } else if (option instanceof BoolOption boolOption) {
                    FlareBooleanOptionButton booleanOptionButton = new FlareBooleanOptionButton(this.getWindow(), boolOption, getX() + getWindow().getTheme().getOptionWidthOffset(), (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, this.getHeight());
                    this.getChildren().add(booleanOptionButton);
                    buttonsHeight += booleanOptionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                    return;
                } else if (option instanceof StringOption stringOption) {
                    FlareStringOptionButton stringOptionButton = new FlareStringOptionButton(this.getWindow(), stringOption, getX() + getWindow().getTheme().getOptionWidthOffset(), (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 22);
                    this.getChildren().add(stringOptionButton);
                    buttonsHeight += stringOptionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                    return;
                } else if (option instanceof StringArrayOption stringArrayOption) {
                    FlareModeOptionButton modeOptionButton = new FlareModeOptionButton(this.getWindow(), stringArrayOption, getX() + getWindow().getTheme().getOptionWidthOffset(), (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, this.getHeight());
                    this.getChildren().add(modeOptionButton);
                    buttonsHeight += modeOptionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                    return;
                }

                DropdownOptionButton optionButton = new DropdownOptionButton(this.getWindow(), option, this.getX() + 1, (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - 2, this.getHeight());
                this.getChildren().add(optionButton);
                buttonsHeight += optionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
            }
        });
        buttonsHeight += getWindow().getTheme().getOptionOffset() * 2 - getWindow().getTheme().getOptionButtonOffset();

        allButtonsAfter().forEach(button -> {
            button.move(0, buttonsHeight);
        });
    }

    @Override
    public void addExtraButtons() {
        FlareDropdownKeybindButton dropdownKeybindButton = new FlareDropdownKeybindButton(this.getWindow(), getFeature(), getX() + getWindow().getTheme().getOptionWidthOffset(), getY() + this.getHeight() + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, this.getHeight());
        this.getChildren().add(dropdownKeybindButton);
        buttonsHeight += this.getHeight() + getWindow().getTheme().getOptionButtonOffset();
        this.getChildren().add(new FlareDropdownVisibleButton(this.getWindow(), getFeature(), getX() + getWindow().getTheme().getOptionWidthOffset(), getY() + this.getHeight() + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, this.getHeight()));
        buttonsHeight += this.getHeight() + getWindow().getTheme().getOptionButtonOffset();
        super.addExtraButtons();
        super.addExtraButtons();
    }

    @Override
    public void openOption(DropdownOptionButton dropdownOptionButton) {
        dropdownOptionButton.getOption().getChildren().forEach(option -> {
            DropdownOptionButton optionButton = new DropdownOptionButton(this.getWindow(), option, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 17);
            if (option instanceof FloatOption floatOption) {
                optionButton = new FlareFloatOptionButton(this.getWindow(), floatOption, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 17);
            } else if (option instanceof IntOption intOption) {
                optionButton = new FlareIntOptionButton(this.getWindow(), intOption, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 15);
            } else if (option instanceof ColorOption colorOption) {
                optionButton = new FlareColorOptionButton(this.getWindow(), colorOption, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 100);
            } else if (option instanceof KeybindOption keybindOption) {
                optionButton = new FlareKeybindOptionButton(this.getWindow(), keybindOption, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, getWindow().getTheme().getButtonSize());
            } else if (option instanceof BoolOption boolOption) {
                optionButton = new FlareBooleanOptionButton(this.getWindow(), boolOption, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, getWindow().getTheme().getButtonSize());
            } else if (option instanceof StringOption stringOption) {
                optionButton = new FlareStringOptionButton(this.getWindow(), stringOption, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 22);
            } else if (option instanceof StringArrayOption stringArrayOption) {
                optionButton = new FlareModeOptionButton(this.getWindow(), stringArrayOption, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, getWindow().getTheme().getButtonSize());
            }

            optionButton.masterButton = dropdownOptionButton.masterButton == null ? dropdownOptionButton : dropdownOptionButton.masterButton;
            optionButton.parentButton = dropdownOptionButton;

            if (optionButton.getOption().hasDependency() && dropdownOptionButton.getOption() instanceof StringArrayOption) {
                if (optionButton.getOption().getDependency().equalsIgnoreCase(((StringArrayOption) dropdownOptionButton.getOption()).getValue())) {
                    dropdownOptionButton.getChildren().add(optionButton);
                    dropdownOptionButton.buttonsHeight += optionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                }
            } else {
                dropdownOptionButton.getChildren().add(optionButton);
                dropdownOptionButton.buttonsHeight += optionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
            }
        });
        dropdownOptionButton.buttonsHeight += getWindow().getTheme().getOptionOffset() * 2 - getWindow().getTheme().getOptionButtonOffset();
        dropdownOptionButton.allButtonsAfter().forEach(button -> button.move(0, dropdownOptionButton.buttonsHeight));
        super.openOption(dropdownOptionButton);
    }

    private DropdownButton getVeryBottomOption() {
        if (getChildren().size() == 0)
            return null;
        DropdownButton last = getChildren().get(getChildren().size() - 1);
        while (last.hasChildren() && last.isOpen())
            last = last.getChildren().get(last.getChildren().size() - 1);
        return last;
    }
}
