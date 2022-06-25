package me.dustin.jex.load;

import me.dustin.jex.helper.addon.hat.HatHelper;
import me.dustin.jex.feature.plugin.JexPlugin;
import net.fabricmc.api.ModInitializer;

public class JexLoad implements ModInitializer {
    @Override
    public void onInitialize() {
        //Place for things that require fabric to load assets before being loaded
        HatHelper.INSTANCE.load();
        JexPlugin.fabricLoad();
    }
}
