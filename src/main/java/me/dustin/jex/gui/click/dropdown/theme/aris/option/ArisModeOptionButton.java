package me.dustin.jex.gui.click.dropdown.theme.aris.option;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.DrawScreenFilter;
import me.dustin.jex.event.filters.MousePressFilter;
import me.dustin.jex.event.misc.EventMouseButton;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.feature.option.types.StringArrayOption;
import me.dustin.jex.gui.click.dropdown.DropDownGui;
import me.dustin.jex.gui.click.dropdown.impl.button.DropdownButton;
import me.dustin.jex.gui.click.dropdown.impl.option.DropdownOptionButton;
import me.dustin.jex.gui.click.dropdown.impl.option.ModeOptionButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;

public class ArisModeOptionButton extends ModeOptionButton {
    public ArisModeOptionButton(DropdownWindow window, StringArrayOption option, float x, float y, float width, float height) {
        super(window, option, x, y, width, height);
    }

    @Override
    public void render(PoseStack matrixStack) {
        float bottomY = getY() + getHeight();
        if (isOpen()) {
            DropdownButton bottomOption = getVeryBottomOption();
            if (bottomOption != null)
                bottomY = bottomOption.getY() + bottomOption.getHeight() + (bottomOption instanceof DropdownOptionButton dropdownOptionButton && dropdownOptionButton.parentButton != null && dropdownOptionButton.parentButton.isOpen() ? getWindow().getTheme().getOptionOffset() : 0) + 2;
        }
        if (bottomY < getWindow().getY() + getWindow().getTheme().getTopBarSize() || getY() > getWindow().getY() + getWindow().getHeight())
            return;
        Render2DHelper.INSTANCE.outlineAndFill(matrixStack, getX(), getY(), getX() + getWidth(), bottomY, 0xff000000, !EventManager.isRegistered(this) ? 0xff333333 : 0xff202020);
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), bottomY, 0x45000000);
        if (!EventManager.isRegistered(this))
            Render2DHelper.INSTANCE.drawThinHLine(matrixStack, getX(), getY() + 0.5f, getX() + getWidth() - 0.5f, 0xff4a4a4a);
        if (hasChild())
            FontHelper.INSTANCE.drawWithShadow(matrixStack, isOpen() ? "-" : "+", getX() + getWidth() - 10, getY() + (getHeight() / 2 - 4), -1);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, this.getOption().getName(), this.getX() + 2, getY() + (getHeight() / 2 - 4), -1);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, stringArrayOption.getValue(), this.getX() + getWidth() - FontHelper.INSTANCE.getStringWidth(stringArrayOption.getValue()) - (hasChild() ? 12 : 2), getY() + (getHeight() / 2 - 4), -1);
        if (isOpen()) {
            this.getChildren().forEach(dropdownButton -> dropdownButton.render(matrixStack));
        }
    }

    @EventPointer
    private final EventListener<EventMouseButton> eventMouseButtonEventListener = new EventListener<>(event -> {
        if (!(Wrapper.INSTANCE.getMinecraft().screen instanceof DropDownGui)) {
            unregister();
            return;
        }
        if (event.getButton() != 0) {
            unregister();
            return;
        }
        if (isSelecting) {
            int i = 0;
            for (String s : stringArrayOption.getAll()) {
                boolean hovered = Render2DHelper.INSTANCE.isHovered(getX(), getY() + getHeight() + 3 + (i * getWindow().getTheme().getButtonSize() + 1), getWidth(), getWindow().getTheme().getButtonSize());
                if (hovered) {
                    if (this.isOpen())
                        this.close();
                    stringArrayOption.setValue(s);
                    isSelecting = false;
                    event.cancel();
                    unregister();
                    return;
                }
                i++;
            }
            event.cancel();
        }
    }, new MousePressFilter(EventMouseButton.ClickType.IN_MENU));

    @EventPointer
    private final EventListener<EventDrawScreen> eventDrawScreenEventListener = new EventListener<>(event -> {
        PoseStack matrixStack = event.getPoseStack();
        if (isSelecting) {
            int i = 0;
            Render2DHelper.INSTANCE.outlineAndFill(matrixStack, getX() - 1, getY() + getHeight() + 1, getX() + getWidth() + 1, getY() + getHeight() + 5 + (stringArrayOption.getAll().length * getWindow().getTheme().getButtonSize()), 0xff000000, 0xff333333);
            for (String s : stringArrayOption.getAll()) {
                boolean selected = s.equalsIgnoreCase(stringArrayOption.getValue());
                boolean hovered = Render2DHelper.INSTANCE.isHovered(getX(), getY() + getHeight() + 3 + (i * getWindow().getTheme().getButtonSize() + 1), getWidth(), getWindow().getTheme().getButtonSize());
                Render2DHelper.INSTANCE.outlineAndFill(matrixStack, getX() + 2, getY() + getHeight() + 3 + (i * getWindow().getTheme().getButtonSize() + 1), getX() + getWidth() - 2, getY() + getHeight() + 3 + (i * getWindow().getTheme().getButtonSize()) + getWindow().getTheme().getButtonSize(), 0xff000000, selected ? 0xff333333 : 0xff202020);
                if (hovered)
                    Render2DHelper.INSTANCE.fill(matrixStack, getX() + 2, getY() + getHeight() + 3 + (i * getWindow().getTheme().getButtonSize() + 1), getX() + getWidth() - 2, getY() + getHeight() + 3 + (i * getWindow().getTheme().getButtonSize()) + getWindow().getTheme().getButtonSize(), 0x45000000);
                if (selected)
                    Render2DHelper.INSTANCE.drawThinHLine(matrixStack, getX() + 2, getY() + getHeight() + 3.5f + (i * getWindow().getTheme().getButtonSize() + 1), getX() + getWidth() - 2.5f, 0xff4a4a4a);
                FontHelper.INSTANCE.drawWithShadow(matrixStack, s, getX() + 4, getY() + getHeight() + 7 + (i * getWindow().getTheme().getButtonSize()), -1);
                i++;
            }
        }
    }, new DrawScreenFilter(EventDrawScreen.Mode.POST, DropDownGui.class));
}
