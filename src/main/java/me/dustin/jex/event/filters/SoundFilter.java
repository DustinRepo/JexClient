package me.dustin.jex.event.filters;

import me.dustin.jex.event.world.EventPlaySound;
import net.minecraft.util.Identifier;

import java.util.function.Predicate;

public class SoundFilter implements Predicate<EventPlaySound> {

    private final EventPlaySound.Mode mode;
    private final Identifier[] ids;

    @SafeVarargs
    public SoundFilter(EventPlaySound.Mode mode, Identifier... ids) {
        this.mode = mode;
        this.ids = ids;
    }

    @Override
    public boolean test(EventPlaySound eventPlaySound) {
        if (ids.length <= 0)
            if (mode != null)
                return mode == eventPlaySound.getMode();
            else
                return true;
        for (Identifier id : ids) {
            if (compare(id, eventPlaySound.getIdentifier())) {
                if (mode != null)
                    return mode == eventPlaySound.getMode();
                return true;
            }
        }
        return false;
    }

    private boolean compare(Identifier first, Identifier second) {
        return first.getNamespace().equalsIgnoreCase(second.getNamespace()) && first.getPath().equalsIgnoreCase(second.getPath());
    }
}
