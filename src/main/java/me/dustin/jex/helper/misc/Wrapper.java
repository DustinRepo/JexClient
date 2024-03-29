package me.dustin.jex.helper.misc;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.player.Freecam;
import me.dustin.jex.load.impl.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;

public enum Wrapper {
    INSTANCE;

    public MinecraftClient getMinecraft() {
        return MinecraftClient.getInstance();
    }
    public IMinecraft getIMinecraft() {
        return (IMinecraft)MinecraftClient.getInstance();
    }

    public ClientPlayerEntity getLocalPlayer() {
        return getMinecraft().player;
    }

    public PlayerEntity getPlayer() {
        return Feature.getState(Freecam.class) ? Freecam.playerEntity : getLocalPlayer();
    }

    public ClientWorld getWorld() {
        return getMinecraft().world;
    }

    public GameOptions getOptions() {
        return getMinecraft().options;
    }

    public ClientPlayerInteractionManager getClientPlayerInteractionManager() {
        return getMinecraft().interactionManager;
    }

    public IClientPlayerInteractionManager getIClientPlayerInteractionManager() { return (IClientPlayerInteractionManager)getMinecraft().interactionManager; }

    public Window getWindow() {
        return getMinecraft().getWindow();
    }

    public TextRenderer getTextRenderer() {
        return getMinecraft().textRenderer;
    }

    public WorldRenderer getWorldRenderer() {
        return getMinecraft().worldRenderer;
    }

    public GameRenderer getGameRenderer() {
        return getMinecraft().gameRenderer;
    }
}
