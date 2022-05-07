package me.dustin.jex.gui.click.dropdown.theme.aris.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.types.*;
import me.dustin.jex.gui.click.dropdown.impl.button.DropdownButton;
import me.dustin.jex.gui.click.dropdown.impl.feature.DropdownFeatureButton;
import me.dustin.jex.gui.click.dropdown.impl.option.DropdownOptionButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.gui.click.dropdown.theme.aris.option.*;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;

public class ArisDropdownFeatureButton extends DropdownFeatureButton {
    public ArisDropdownFeatureButton(DropdownWindow window, Feature feature, float x, float y, float width, float height) {
        super(window, feature, x, y, width, height);
    }

    @Override
    public void render(PoseStack matrixStack) {
        float bottomY = getY() + getHeight();
        if (isOpen()) {
            DropdownButton bottomOption = getVeryBottomOption();
            DropdownButton nextButton = getNextButton();
            if (nextButton != null)
                bottomY = nextButton.getY() - getWindow().getTheme().getOptionOffset();
            else if (bottomOption != null)
                bottomY = bottomOption.getY() + bottomOption.getHeight() + (bottomOption instanceof DropdownOptionButton dropdownOptionButton && dropdownOptionButton.parentButton != null && dropdownOptionButton.parentButton.isOpen() ? getWindow().getTheme().getOptionOffset() : 0) + 2;

        }
        if (bottomY < getWindow().getY() + getWindow().getTheme().getTopBarSize() || getY() > getWindow().getY() + getWindow().getHeight())
            return;

        Render2DHelper.INSTANCE.outlineAndFill(matrixStack, getX(), getY(), getX() + getWidth(), bottomY, 0xff000000, getFeature().getState() ? 0xff333333 : 0xff202020);
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), bottomY, 0x45000000);
        if (getFeature().getState())
            Render2DHelper.INSTANCE.drawThinHLine(matrixStack, getX(), getY() + 0.5f, getX() + getWidth() - 0.5f, 0xff4a4a4a);
        FontHelper.INSTANCE.drawCenteredString(matrixStack, getFeature().getName(), getX() + getWidth() / 2.f, getY()  + (getHeight() / 2 - 4), -1);
        if (isOpen()) {
            this.getChildren().forEach(dropdownButton -> {
                dropdownButton.render(matrixStack);
            });
        }
        super.render(matrixStack);
    }

    @Override
    public void open() {
        buttonsHeight = 0;
        setOpen(true);
        getFeature().getOptions().forEach(option -> {
            if (!option.hasParent()) {
                if (option instanceof FloatOption floatOption) {
                    ArisFloatOptionButton floatOptionButton = new ArisFloatOptionButton(this.getWindow(), floatOption, getX() + getWindow().getTheme().getOptionWidthOffset(), (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 17);
                    this.getChildren().add(floatOptionButton);
                    buttonsHeight += floatOptionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                    return;
                } else if (option instanceof IntOption intOption) {
                    ArisIntOptionButton intOptionButton = new ArisIntOptionButton(this.getWindow(), intOption, getX() + getWindow().getTheme().getOptionWidthOffset(), (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 17);
                    this.getChildren().add(intOptionButton);
                    buttonsHeight += intOptionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                    return;
                } else if (option instanceof ColorOption colorOption) {
                    ArisColorOptionButton colorOptionButton = new ArisColorOptionButton(this.getWindow(), colorOption, getX() + getWindow().getTheme().getOptionWidthOffset(), (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 100);
                    this.getChildren().add(colorOptionButton);
                    buttonsHeight += colorOptionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                    return;
                } else if (option instanceof KeybindOption keybindOption) {
                    ArisKeybindOptionButton keybindOptionButton = new ArisKeybindOptionButton(this.getWindow(), keybindOption, getX() + getWindow().getTheme().getOptionWidthOffset(), (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, this.getHeight());
                    this.getChildren().add(keybindOptionButton);
                    buttonsHeight += keybindOptionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                    return;
                } else if (option instanceof BoolOption boolOption) {
                    ArisBooleanOptionButton booleanOptionButton = new ArisBooleanOptionButton(this.getWindow(), boolOption, getX() + getWindow().getTheme().getOptionWidthOffset(), (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, this.getHeight());
                    this.getChildren().add(booleanOptionButton);
                    buttonsHeight += booleanOptionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                    return;
                } else if (option instanceof StringOption stringOption) {
                    ArisStringOptionButton stringOptionButton = new ArisStringOptionButton(this.getWindow(), stringOption, getX() + getWindow().getTheme().getOptionWidthOffset(), (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 22);
                    this.getChildren().add(stringOptionButton);
                    buttonsHeight += stringOptionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                    return;
                } else if (option instanceof StringArrayOption stringArrayOption) {
                    ArisModeOptionButton modeOptionButton = new ArisModeOptionButton(this.getWindow(), stringArrayOption, getX() + getWindow().getTheme().getOptionWidthOffset(), (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, this.getHeight());
                    this.getChildren().add(modeOptionButton);
                    buttonsHeight += modeOptionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
                    return;
                }

                DropdownOptionButton optionButton = new DropdownOptionButton(this.getWindow(), option, this.getX() + 1, (this.getY() + this.getHeight()) + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - 2, this.getHeight());
                this.getChildren().add(optionButton);
                buttonsHeight += optionButton.getHeight() + getWindow().getTheme().getOptionButtonOffset();
            }
        });
        addExtraButtons();
        buttonsHeight += getWindow().getTheme().getOptionOffset() * 2;

        allButtonsAfter().forEach(button -> {
            button.move(0, buttonsHeight);
        });
    }

    @Override
    public void addExtraButtons() {
        this.getChildren().add(new ArisDropdownVisibleButton(this.getWindow(), getFeature(), getX() + getWindow().getTheme().getOptionWidthOffset(), getY() + this.getHeight() + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, this.getHeight()));
        buttonsHeight += this.getHeight() + getWindow().getTheme().getOptionButtonOffset();
        this.getChildren().add(new ArisDropdownKeybindButton(this.getWindow(), getFeature(), getX() + getWindow().getTheme().getOptionWidthOffset(), getY() + this.getHeight() + buttonsHeight + getWindow().getTheme().getOptionOffset(), this.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, this.getHeight()));
        buttonsHeight += this.getHeight() + getWindow().getTheme().getOptionButtonOffset();
        super.addExtraButtons();
    }

    @Override
    public void openOption(DropdownOptionButton dropdownOptionButton) {
        dropdownOptionButton.getOption().getChildren().forEach(option -> {
            DropdownOptionButton optionButton = new DropdownOptionButton(this.getWindow(), option, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, getWindow().getTheme().getButtonSize());
            if (option instanceof FloatOption floatOption) {
                optionButton = new ArisFloatOptionButton(this.getWindow(), floatOption, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 17);
            } else if (option instanceof IntOption intOption) {
                optionButton = new ArisIntOptionButton(this.getWindow(), intOption, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 17);
            } else if (option instanceof ColorOption colorOption) {
                optionButton = new ArisColorOptionButton(this.getWindow(), colorOption, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 100);
            } else if (option instanceof KeybindOption keybindOption) {
                optionButton = new ArisKeybindOptionButton(this.getWindow(), keybindOption, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, getWindow().getTheme().getButtonSize());
            } else if (option instanceof BoolOption boolOption) {
                optionButton = new ArisBooleanOptionButton(this.getWindow(), boolOption, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, getWindow().getTheme().getButtonSize());
            } else if (option instanceof StringOption stringOption) {
                optionButton = new ArisStringOptionButton(this.getWindow(), stringOption, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, 22);
            } else if (option instanceof StringArrayOption stringArrayOption) {
                optionButton = new ArisModeOptionButton(this.getWindow(), stringArrayOption, dropdownOptionButton.getX() + getWindow().getTheme().getOptionWidthOffset(), (dropdownOptionButton.getY() + dropdownOptionButton.getHeight()) + dropdownOptionButton.buttonsHeight + getWindow().getTheme().getOptionOffset(), dropdownOptionButton.getWidth() - getWindow().getTheme().getOptionWidthOffset() * 2, getWindow().getTheme().getButtonSize());
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
        dropdownOptionButton.buttonsHeight += getWindow().getTheme().getOptionOffset() * 2;
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

    private DropdownButton getNextButton() {
        for (DropdownButton button : getWindow().getButtons()) {
            if (getWindow().getButtons().indexOf(button) > getWindow().getButtons().indexOf(this) && button.isVisible()) {
                return button;
            }
        }
        return null;
    }
}
