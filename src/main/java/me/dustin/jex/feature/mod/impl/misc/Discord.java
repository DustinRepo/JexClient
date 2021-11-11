package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.DiscordPresenceHelper;
import net.minecraft.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

@Feature.Manifest(category = Feature.Category.MISC, description = "Show on Discord that you are using Jex Client", enabled = true, visible = false)
public class Discord extends Feature {

    @Op(name = "Show Server IP")
    public boolean showServerIP = true;

    @Override
    public void onEnable() {
        if (isOnRaspberryPi()) {
            setState(false);
            return;
        }
        DiscordPresenceHelper.INSTANCE.init();
    }

    @Override
    public void onDisable() {
        if (isOnRaspberryPi()) {
            return;
        }
        DiscordPresenceHelper.INSTANCE.destroy();
    }

    public boolean isOnRaspberryPi() {
        //might as well just catch all ARM devices since discord does not support ARM and by extension, neither does the RPC.
        if (System.getProperty("os.arch").toLowerCase().contains("arm"))
            return true;
        if (Util.getOperatingSystem() == Util.OperatingSystem.LINUX) {
            final File file = new File("/etc", "os-release");
            try (FileInputStream fis = new FileInputStream(file); BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis))) {
                String string;
                while ((string = bufferedReader.readLine()) != null) {
                    if (string.toLowerCase().contains("raspbian")) {
                        if (string.toLowerCase().contains("name")) {
                            return true;
                        }
                    }
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
