package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventDisplayScreen;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.EventRenderWidget;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.load.impl.ISliderWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;

@Feature.Manifest(name = "CustomWidgets", category = Feature.Category.VISUAL, description = "Change the visuals on widgets like buttons from Minecraft", enabled = true, visible = false)
public class CustomWidgets extends Feature {

    private Map<ClickableWidget, Integer> hoverChecks = new HashMap<>();
    private Map<ClickableWidget, Float> offsets = new HashMap<>();

    @EventListener(events = {EventRenderWidget.class, EventTick.class, EventDisplayScreen.class})
    private void runMethod(Event event) {
        if (event instanceof EventRenderWidget eventRenderWidget) {
            int hoverColor = ColorHelper.INSTANCE.setAlpha(ColorHelper.INSTANCE.getClientColor(), 150);
            int nonHoverColor = 0x80000000;
            int borderColor = 0xff999999;
            int activeTextColor = 0xffbbbbbb;
            int inactiveTextColor = 0xff333333;

            ClickableWidget widget = eventRenderWidget.getClickableWidget();
            MatrixStack matrixStack = eventRenderWidget.getMatrixStack();
            if (!hoverChecks.containsKey(widget)) {
                hoverChecks.put(widget, 0);
                offsets.put(widget, 0.f);
            }
            int hoverTime = hoverChecks.get(widget);
            float offset = offsets.get(widget);
            float newOffset;
            if (hoverTime != 0){
                newOffset = 5 / (10.f / hoverTime);
                offset = offset + ((newOffset - offset) * Wrapper.INSTANCE.getMinecraft().getTickDelta());
            } else {
                newOffset = 0;
            }
            offsets.replace(widget, newOffset);
            if (widget instanceof ButtonWidget || widget instanceof CyclingButtonWidget) {
                Render2DHelper.INSTANCE.fillAndBorder(matrixStack, widget.x + offset, widget.y + (offset / 3.f), widget.x + widget.getWidth() - offset, widget.y + widget.getHeight() - (offset / 3.f), borderColor, widget.active && widget.isHovered() ? hoverColor : nonHoverColor, 1);
            } else if (widget instanceof SliderWidget sliderWidget) {
                ISliderWidget islider = (ISliderWidget) sliderWidget;
                double value = islider.getValue();
                float x = widget.x + 4;
                float width = widget.getWidth() - 8;
                float pos = (float)value * width;
                Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x - 1, widget.y, x + width + 1, widget.y + widget.getHeight(), borderColor, widget.active ? nonHoverColor : 0x99545454, 1);

                Render2DHelper.INSTANCE.fill(matrixStack, x, widget.y + 1, x + pos, widget.y + widget.getHeight() - 1, hoverColor);
            }
            eventRenderWidget.cancel();
            FontHelper.INSTANCE.drawCenteredString(matrixStack, widget.getMessage().getString(), widget.x + (widget.getWidth() / 2.f), widget.y + (widget.getHeight() / 2.f) - 4.5f, widget.active ? activeTextColor : inactiveTextColor);

        } else if (event instanceof EventTick) {
            for (ClickableWidget widget : hoverChecks.keySet()) {
                int hovered = hoverChecks.get(widget);
                if (widget.isHovered() && widget.active) {
                    if (hovered < 10)
                        hovered+=3;
                } else if (hovered > 0) {
                    hovered-=1.3f;
                }
                hovered = MathHelper.clamp(hovered, 0, 10);
                hoverChecks.replace(widget, hovered);
            }
        } else if (event instanceof EventDisplayScreen) {
            hoverChecks.clear();
        }
    }

}
