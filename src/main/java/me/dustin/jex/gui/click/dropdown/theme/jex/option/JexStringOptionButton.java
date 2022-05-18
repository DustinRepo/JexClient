package me.dustin.jex.gui.click.dropdown.theme.jex.option;

import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.feature.option.types.StringOption;
import me.dustin.jex.gui.click.dropdown.impl.option.StringOptionButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;

public class JexStringOptionButton extends StringOptionButton {
    public JexStringOptionButton(DropdownWindow window, StringOption option, float x, float y, float width, float height) {
        super(window, option, x, y, width, height);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x40ffffff);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, this.getOption().getName(), this.getX() + 10, this.getY() + 2, 0xffaaaaaa);
        float fontWidth = FontHelper.INSTANCE.getStringWidth(stringOption.getValue());
        FontHelper.INSTANCE.drawWithShadow(matrixStack, stringOption.getValue(), this.getX() + 4 + (fontWidth > getWidth() - 4 ? getWidth() - fontWidth - 4 : 0), this.getY() + 12, 0xffaaaaaa);

        if (EventManager.isRegistered(this)) {
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), getWindow().getColor(), 0x00ffffff, 1);
        }
        super.render(matrixStack);
    }

    @EventPointer
    private final EventListener<EventKeyPressed> eventListener = new EventListener<>(event -> handleKey(event));
}
