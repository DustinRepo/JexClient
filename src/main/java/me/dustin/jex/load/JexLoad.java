package me.dustin.jex.load;

import me.dustin.jex.feature.plugin.JexPlugin;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class JexLoad implements ModInitializer {
    @Override
    public void onInitialize() {
        //Place for things that require fabric to load assets before being loaded
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public void reload(ResourceManager manager) {
                ShaderHelper.INSTANCE.loadShaders();
            }

            @Override
            public Identifier getFabricId() {
                return new Identifier("jex", "shaders/core/");
            }
        });
        JexPlugin.fabricLoad();
    }
}
