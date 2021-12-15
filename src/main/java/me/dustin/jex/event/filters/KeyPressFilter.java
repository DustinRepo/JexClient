package me.dustin.jex.event.filters;

import me.dustin.jex.event.misc.EventKeyPressed;

import java.util.function.Predicate;

public class KeyPressFilter implements Predicate<EventKeyPressed> {

    private final EventKeyPressed.PressType pressType;
    private final int[] keys;
    public KeyPressFilter(EventKeyPressed.PressType pressType, int... keys) {
        this.pressType = pressType;
        this.keys = keys;
    }

    @Override
    public boolean test(EventKeyPressed eventKeyPressed) {
        if (keys.length > 0) {
            for (int key : keys) {
                if (pressType != null)
                    return key == eventKeyPressed.getKey() && pressType == eventKeyPressed.getType();
                return key == eventKeyPressed.getKey();
            }
            return false;
        }
        if (pressType != null)
            return eventKeyPressed.getType() == pressType;
        return true;
    }
}
