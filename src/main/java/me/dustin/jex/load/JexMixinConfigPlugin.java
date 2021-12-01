package me.dustin.jex.load;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class JexMixinConfigPlugin implements IMixinConfigPlugin {
    private final Logger LOGGER = LogManager.getFormatterLogger("JexMixins");
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (FabricLoader.getInstance().isModLoaded("optifabric")) {
            if (mixinClassName.equalsIgnoreCase("me.dustin.jex.load.mixin.minecraft.MixinShader")) {
                LOGGER.info("Optifabric loaded. Ignoring " + mixinClassName + " that injects into " + targetClassName);
                return false;
            }
        } else if (mixinClassName.toLowerCase().contains("optifine")) {
            LOGGER.info("Optifabric not loaded. Ignoring " + mixinClassName + " that injects into " + targetClassName);
            return false;
        }
        //I use @Psuedo for sodium classes but someone experienced a weird crash so this is an extra fail-safe
        if (!FabricLoader.getInstance().isModLoaded("sodium")) {
            if (mixinClassName.contains("sodium.")) {
                LOGGER.info("Sodium not loaded. Ignoring " + mixinClassName + " that injects into " + targetClassName);
                return false;
            }
        } else if (mixinClassName.contains("sodium.")){
            LOGGER.info("Sodium loaded. Using " + mixinClassName + " that injects into " + targetClassName);
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
