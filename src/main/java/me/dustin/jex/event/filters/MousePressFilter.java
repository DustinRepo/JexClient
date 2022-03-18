package me.dustin.jex.event.filters;

import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.event.misc.EventMouseButton;

import java.util.function.Predicate;

public class MousePressFilter implements Predicate<EventMouseButton> {

    private final EventMouseButton.ClickType pressType;
    private final int[] keys;
    public MousePressFilter(EventMouseButton.ClickType pressType, int... keys) {
        this.pressType = pressType;
        this.keys = keys;
    }

    @Override
    public boolean test(EventMouseButton eventMouseButton) {
        if (keys.length <= 0) {
            if (pressType != null)
                return eventMouseButton.getClickType() == pressType;
            return true;
        }
        for (int key : keys) {
            if (pressType != null)
                return key == eventMouseButton.getButton() && pressType == eventMouseButton.getClickType();
            return key == eventMouseButton.getButton();
        }
        return false;
    }
}
