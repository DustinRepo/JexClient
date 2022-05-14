package me.dustin.jex.load;

import me.dustin.jex.addon.hat.Hat;
import me.dustin.jex.feature.plugin.JexPlugin;
import net.fabricmc.api.ModInitializer;

public class JexLoad implements ModInitializer {
    @Override
    public void onInitialize() {
        //Place for things that require fabric to load assets before being loaded
        new Hat().load();
        JexPlugin.fabricLoad();
    }
}
