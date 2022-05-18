package me.dustin.jex.load;

import me.dustin.jex.feature.plugin.JexPluginManager;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;


public class PreLaunch implements PreLaunchEntrypoint {
    private final Logger LOGGER = LogManager.getFormatterLogger("JexPlugins");
    @Override
    public void onPreLaunch() {
        LOGGER.info("Adding Jex plugins to class path");
        JexPluginManager.INSTANCE.loadPlugins();
        JexPluginManager.INSTANCE.getPlugins().forEach(jexPlugin -> {
            if (!jexPlugin.getInfo().getMixinFile().isEmpty()) {
                LOGGER.info("Adding mixin config %s from plugin %s".formatted(jexPlugin.getInfo().getMixinFile(), jexPlugin.getInfo().getName()));
                Mixins.addConfiguration(jexPlugin.getInfo().getMixinFile());
            }
        });
    }
}