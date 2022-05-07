package me.dustin.jex.gui.click.dropdown.theme.jex.option;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.DrawScreenFilter;
import me.dustin.jex.event.filters.MousePressFilter;
import me.dustin.jex.event.misc.EventMouseButton;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.feature.option.types.StringArrayOption;
import me.dustin.jex.gui.click.dropdown.DropDownGui;
import me.dustin.jex.gui.click.dropdown.impl.option.ModeOptionButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;

public class JexModeOptionButton extends ModeOptionButton {
    public JexModeOptionButton(DropdownWindow window, StringArrayOption option, float x, float y, float width, float height) {
        super(window, option, x, y, width, height);
    }

    @Override
    public void render(PoseStack matrixStack) {
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x40ffffff);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, this.getOption().getName() + ": \247f" + stringArrayOption.getValue(), this.getX() + 2, getY() + (getHeight() / 2 - 4), 0xffaaaaaa);
        super.render(matrixStack);
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
            int i = 1;
            for (String s : stringArrayOption.getAll()) {
                boolean hovered = Render2DHelper.INSTANCE.isHovered(getX(), getY() + (i * 11), getWidth(), 11);
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
            int i = 1;
            for (String s : stringArrayOption.getAll()) {
                boolean hovered = Render2DHelper.INSTANCE.isHovered(getX(), getY() + (i * 11), getWidth(), 11);
                Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY() + (i * 11), getX() + getWidth(), getY() + (i * 11) + 11, 0xff000000);
                Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY() + (i * 11), getX() + 1, getY() + (i * 11) + 11, getWindow().getColor());
                Render2DHelper.INSTANCE.fill(matrixStack, getX() + getWidth() - 1, getY() + (i * 11), getX() + getWidth(), getY() + (i * 11) + 11, getWindow().getColor());
                FontHelper.INSTANCE.drawWithShadow(matrixStack, s, getX() + 2, getY() + 2 + (i * 11), hovered ? getWindow().getColor() :  -1);
                i++;
            }
        }
    }, new DrawScreenFilter(EventDrawScreen.Mode.POST, DropDownGui.class));
}
