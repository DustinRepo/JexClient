package me.dustin.jex.load;

import me.dustin.jex.addon.hat.Hat;
import net.fabricmc.api.ModInitializer;

public class JexLoad implements ModInitializer {
    @Override
    public void onInitialize() {
        new Hat().load();
    }
}
