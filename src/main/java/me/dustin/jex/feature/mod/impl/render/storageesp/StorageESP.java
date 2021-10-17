package me.dustin.jex.feature.mod.impl.render.storageesp;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.*;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.impl.render.storageesp.impl.OutlineStorageESP;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.storageesp.impl.BoxStorageESP;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.block.entity.*;
import net.minecraft.world.chunk.Chunk;

import java.awt.*;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Show storage blocks through walls")
public class StorageESP extends Feature {

    @Op(name = "Mode", all = {"Box", "Shader"})
    public String mode = "Shader";
    @Op(name = "Chest")
    public boolean chest = true;
    @Op(name = "Ender Chest")
    public boolean echest = true;
    @Op(name = "Shulker")
    public boolean shulker = true;
    @Op(name = "Barrel")
    public boolean barrel = true;
    @Op(name = "Hopper")
    public boolean hopper = true;
    @Op(name = "Spawner")
    public boolean spawner = true;

    @OpChild(name = "Chest Color", isColor = true, parent = "Chest")
    public int chestColor = new Color(255, 255, 0).getRGB();
    @OpChild(name = "Ender Chest Color", isColor = true, parent = "Ender Chest")
    public int enderchestColor = new Color(147, 0, 255).getRGB();
    @OpChild(name = "Shulker Color", isColor = true, parent = "Shulker")
    public int shulkerColor = new Color(255, 0, 239).getRGB();
    @OpChild(name = "Spawner Color", isColor = true, parent = "Spawner")
    public int spawnerColor = new Color(161, 255, 0).getRGB();
    @OpChild(name = "Barrel Color", isColor = true, parent = "Barrel")
    public int barrelColor = new Color(215, 82, 0).getRGB();
    @OpChild(name = "Hopper Color", isColor = true, parent = "Hopper")
    public int hopperColor = new Color(79, 76, 78).getRGB();
    private String lastMode;

    public StorageESP() {
        new OutlineStorageESP();
        new BoxStorageESP();
    }

    @EventListener(events = {EventRender3D.class, EventRender2D.class, EventRender2DNoScale.class}, priority = 1)
    public void run(Event event) {
        if (lastMode != null && !mode.equalsIgnoreCase(lastMode)) {
            FeatureExtension.get(lastMode, this).disable();
            FeatureExtension.get(mode, this).enable();
        }
        FeatureExtension.get(mode, this).pass(event);
        this.setSuffix(mode);
        lastMode = mode;
    }

    @Override
    public void onEnable() {
        FeatureExtension.get(mode, this).enable();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        FeatureExtension.get(mode, this).disable();
        super.onDisable();
    }

    public boolean isValid(BlockEntity blockEntity) {
        Chunk chunk = Wrapper.INSTANCE.getWorld().getChunk(blockEntity.getPos());
        if (!Wrapper.INSTANCE.getWorld().getChunkManager().isChunkLoaded(chunk.getPos().x, chunk.getPos().z))
            return false;
        if (blockEntity instanceof ChestBlockEntity)
            return chest;
        if (blockEntity instanceof EnderChestBlockEntity)
            return echest;
        if (blockEntity instanceof ShulkerBoxBlockEntity)
            return shulker;
        if (blockEntity instanceof BarrelBlockEntity)
            return barrel;
        if (blockEntity instanceof MobSpawnerBlockEntity)
            return spawner;
        if (blockEntity instanceof HopperBlockEntity)
            return hopper;
        return false;
    }

    public int getColor(BlockEntity blockEntity) {
        if (blockEntity instanceof ChestBlockEntity)
            return chestColor;
        if (blockEntity instanceof EnderChestBlockEntity)
            return enderchestColor;
        if (blockEntity instanceof MobSpawnerBlockEntity)
            return spawnerColor;
        if (blockEntity instanceof ShulkerBoxBlockEntity)
            return shulkerColor;
        if (blockEntity instanceof BarrelBlockEntity)
            return barrelColor;
        if (blockEntity instanceof HopperBlockEntity)
            return hopperColor;

        return 0xffffffff;
    }
}
