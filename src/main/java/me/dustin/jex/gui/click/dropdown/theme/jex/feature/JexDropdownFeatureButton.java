package me.dustin.jex.gui.click.dropdown.theme.jex.feature;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.types.*;
import me.dustin.jex.gui.click.dropdown.impl.feature.DropdownFeatureButton;
import me.dustin.jex.gui.click.dropdown.impl.option.*;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.gui.click.dropdown.theme.jex.option.*;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;

public class JexDropdownFeatureButton extends DropdownFeatureButton {
    public JexDropdownFeatureButton(DropdownWindow window, Feature feature, float x, float y, float width, float height) {
        super(window, feature, x, y, width, height);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x90000000);
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x40ffffff);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, getFeature().getName(), getX() + 2, getY() + (getHeight() / 2 - 4), getFeature().getState() ? getWindow().getColor() : -1);

        String indicator = isOpen() ? "!!!" : "...";
        FontHelper.INSTANCE.drawWithShadow(matrixStack, indicator, getX() + getWidth() - FontHelper.INSTANCE.getStringWidth(indicator), getY() + 2, isOpen() ? getWindow().getColor() : -1);
        if (isOpen()) {
            float bottomY = this == getWindow().getButtons().get(getWindow().getButtons().size() - 1) ? getWindow().getVeryBottomButton().getY() + getWindow().getVeryBottomButton().getHeight() : getWindow().getButtons().get(getWindow().getButtons().indexOf(this) + 1).getY();
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY() + getHeight() - 1, getX() + getWidth(), getY() + getHeight(), getWindow().getColor());
            Render2DHelper.INSTANCE.gradientFill(matrixStack, getX(), getY() + getHeight(), getX() + getWidth(), bottomY, 0xdd454545, 0xdd101010);
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), bottomY - 1, getX() + getWidth(), bottomY, getWindow().getColor());
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
                    JexFloatOptionButton floatOptionButton = new JexFloatOptionButton(this.getWindow(), floatOption, getX() + getWindow().getTheme().getOptionWidthOffset(), (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 15);
                    this.getChildren().add(floatOptionButton);
                    buttonsHeight += floatOptionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                    return;
                } else if (option instanceof IntOption intOption) {
                    JexIntOptionButton intOptionButton = new JexIntOptionButton(this.getWindow(), intOption, getX() + getWindow().getTheme().getOptionWidthOffset(), (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 15);
                    this.getChildren().add(intOptionButton);
                    buttonsHeight += intOptionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                    return;
                } else if (option instanceof ColorOption colorOption) {
                    JexColorOptionButton colorOptionButton = new JexColorOptionButton(this.getWindow(), colorOption, getX() + getWindow().getTheme().getOptionWidthOffset(), (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 100);
                    this.getChildren().add(colorOptionButton);
                    buttonsHeight += colorOptionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                    return;
                } else if (option instanceof KeybindOption keybindOption) {
                    JexKeybindOptionButton keybindOptionButton = new JexKeybindOptionButton(this.getWindow(), keybindOption, getX() + getWindow().getTheme().getOptionWidthOffset(), (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, this.getHeight());
                    this.getChildren().add(keybindOptionButton);
                    buttonsHeight += keybindOptionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                    return;
                } else if (option instanceof BoolOption boolOption) {
                    JexBooleanOptionButton booleanOptionButton = new JexBooleanOptionButton(this.getWindow(), boolOption, getX() + getWindow().getTheme().getOptionWidthOffset(), (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, this.getHeight());
                    this.getChildren().add(booleanOptionButton);
                    buttonsHeight += booleanOptionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                    return;
                } else if (option instanceof StringOption stringOption) {
                    JexStringOptionButton stringOptionButton = new JexStringOptionButton(this.getWindow(), stringOption, getX() + getWindow().getTheme().getOptionWidthOffset(), (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 22);
                    this.getChildren().add(stringOptionButton);
                    buttonsHeight += stringOptionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                    return;
                } else if (option instanceof StringArrayOption stringArrayOption) {
                    JexModeOptionButton modeOptionButton = new JexModeOptionButton(this.getWindow(), stringArrayOption, getX() + getWindow().getTheme().getOptionWidthOffset(), (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, this.getHeight());
                    this.getChildren().add(modeOptionButton);
                    buttonsHeight += modeOptionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                    return;
                }

                DropdownOptionButton optionButton = new DropdownOptionButton(this.getWindow(), option, this.getX() + 1, (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, this.getHeight());
                this.getChildren().add(optionButton);
                buttonsHeight += optionButton.getHeight();
            }
        });
        buttonsHeight += getWindow().getTheme().getOptionOffset() * 2 - getWindow().getTheme().getOptionButtonOffset();

        allButtonsAfter().forEach(button -> {
            button.move(0, buttonsHeight);
        });
    }

    @Override
    public void addExtraButtons() {
        JexDropdownKeybindButton dropdownKeybindButton = new JexDropdownKeybindButton(this.getWindow(), getFeature(), getX() + 1, getY() + this.getHeight() + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - 2, this.getHeight());
        this.getChildren().add(dropdownKeybindButton);
        buttonsHeight += this.getHeight() + getWindow().getTheme().getOptionButtonOffset();
        this.getChildren().add(new JexDropdownVisibleButton(this.getWindow(), getFeature(), getX() + 1, getY() + this.getHeight() + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - 2, this.getHeight()));
        buttonsHeight += this.getHeight() + getWindow().getTheme().getOptionButtonOffset();
        super.addExtraButtons();
    }

    @Override
    public void openOption(DropdownOptionButton dropdownOptionButton) {
        dropdownOptionButton.getOption().getChildren().forEach(option -> {
            DropdownOptionButton optionButton = new DropdownOptionButton(this.getWindow(), option, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 15);;
            if (option instanceof FloatOption floatOption) {
                optionButton = new JexFloatOptionButton(this.getWindow(), floatOption, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 15);
            } else if (option instanceof IntOption intOption) {
                optionButton = new JexIntOptionButton(this.getWindow(), intOption, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 15);
            } else if (option instanceof ColorOption colorOption) {
                optionButton = new JexColorOptionButton(this.getWindow(), colorOption, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 100);
            } else if (option instanceof KeybindOption keybindOption) {
                optionButton = new JexKeybindOptionButton(this.getWindow(), keybindOption, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, getWindow().getTheme().getButtonSize());
            } else if (option instanceof BoolOption boolOption) {
                optionButton = new JexBooleanOptionButton(this.getWindow(), boolOption, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, getWindow().getTheme().getButtonSize());
            } else if (option instanceof StringOption stringOption) {
                optionButton = new JexStringOptionButton(this.getWindow(), stringOption, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 22);
            } else if (option instanceof StringArrayOption stringArrayOption) {
                optionButton = new JexModeOptionButton(this.getWindow(), stringArrayOption, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, getWindow().getTheme().getButtonSize());
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
}
