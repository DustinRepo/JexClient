package me.dustin.jex.gui.click.dropdown.theme.flare.option;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.DrawScreenFilter;
import me.dustin.jex.event.filters.MousePressFilter;
import me.dustin.jex.event.misc.EventMouseButton;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.feature.option.types.StringArrayOption;
import me.dustin.jex.gui.click.dropdown.DropDownGui;
import me.dustin.jex.gui.click.dropdown.impl.button.DropdownButton;
import me.dustin.jex.gui.click.dropdown.impl.option.ModeOptionButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;

public class FlareModeOptionButton extends ModeOptionButton {
    public FlareModeOptionButton(DropdownWindow window, StringArrayOption option, float x, float y, float width, float height) {
        super(window, option, x, y, width, height);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x40ffffff);
        if (hasChild())
            FontHelper.INSTANCE.drawWithShadow(matrixStack, isOpen() ? "-" : "+", getX() + 2, getY() + (getHeight() / 2 - 4), isOpen() ? 0xff00ffff : -1);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, Formatting.WHITE + this.getOption().getName() + ": " + Formatting.RESET + stringArrayOption.getValue(), this.getX() + 10, getY() + (getHeight() / 2 - 4), 0xff00ffff);
        if (isOpen()) {
            DropdownButton bottomOption = getVeryBottomOption();
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, getX() + 2, getY() + getHeight() + getWindow().getTheme().getOptionOffset() - 2, getX() + getWidth() - 2, bottomOption.getY() + bottomOption.getHeight() + 2, 0xaa999999, 0x50000000, 1);
            this.getChildren().forEach(dropdownButton -> dropdownButton.render(matrixStack));
        }
    }

    @EventPointer
    private final EventListener<EventMouseButton> eventMouseButtonEventListener = new EventListener<>(event -> {
        if (!(Wrapper.INSTANCE.getMinecraft().currentScreen instanceof DropDownGui)) {
            unregister();
            return;
        }
        if (event.getButton() != 0) {
            unregister();
            return;
        }
        if (isSelecting) {
            int i = 1;
            for (String s : stringArrayOption.getAll()) {
                boolean hovered = Render2DHelper.INSTANCE.isHovered(getX(), getY() + (i * 12), getWidth(), 12);
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
        MatrixStack matrixStack = event.getMatrixStack();
        if (isSelecting) {
            int i = 1;
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, getX() - 1, getY() + 11, getX() + getWidth() + 1, getY() + 1 + ((stringArrayOption.getAll().length + 1) * 12), 0x99ffffff, 0xaa000000, 1);
            for (String s : stringArrayOption.getAll()) {
                boolean hovered = Render2DHelper.INSTANCE.isHovered(getX(), getY() + (i * 12), getWidth(), 12);
                if (hovered)
                    Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY() + (i * 12), getX() + getWidth(), getY() + (i * 12) + 12, 0x40ffffff);
                FontHelper.INSTANCE.drawWithShadow(matrixStack, s, getX() + 2, getY() + 2 + (i * 12), hovered ? 0xff00ffff :  -1);
                i++;
            }
        }
    }, new DrawScreenFilter(EventDrawScreen.Mode.POST, DropDownGui.class));
}
