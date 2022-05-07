package me.dustin.jex.helper.misc;

import com.mojang.blaze3d.platform.Window;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.player.Freecam;
import me.dustin.jex.load.impl.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.player.Player;

public enum Wrapper {
    INSTANCE;

    public Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }
    public IMinecraft getIMinecraft() {
        return (IMinecraft)Minecraft.getInstance();
    }

    public LocalPlayer getLocalPlayer() {
        return getMinecraft().player;
    }

    public Player getPlayer() {
        return Feature.getState(Freecam.class) ? Freecam.playerEntity : getLocalPlayer();
    }

    public ClientLevel getWorld() {
        return getMinecraft().level;
    }

    public Options getOptions() {
        return getMinecraft().options;
    }

    public MultiPlayerGameMode getMultiPlayerGameMode() {
        return getMinecraft().gameMode;
    }

    public IMultiPlayerGameMode getIMultiPlayerGameMode() { return (IMultiPlayerGameMode)getMinecraft().gameMode; }

    public Window getWindow() {
        return getMinecraft().getWindow();
    }

    public Font getTextRenderer() {
        return getMinecraft().font;
    }

    public LevelRenderer getWorldRenderer() {
        return getMinecraft().levelRenderer;
    }

    public GameRenderer getGameRenderer() {
        return getMinecraft().gameRenderer;
    }

}
