package me.dustin.jex.feature.mod.impl.render.storageesp;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.event.render.*;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.impl.render.storageesp.impl.OutlineStorageESP;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.storageesp.impl.BoxStorageESP;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.entity.vehicle.MinecartHopper;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import java.awt.*;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Show storage blocks through walls")
public class StorageESP extends Feature {

    @Op(name = "Mode", all = {"Box", "Shader"})
    public String mode = "Shader";
    @OpChild(name = "Fade When Close", parent = "Mode", dependency = "Box")
    public boolean fadeBoxesWhenClose = true;
    @OpChild(name = "Fade Distance", min = 1, max = 50, parent = "Mode", dependency = "Box")
    public int fadeDistance = 10;
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
    @Op(name = "Furnace")
    public boolean furnace = true;
    @Op(name = "Dispenser/Dropper")
    public boolean dispensers = true;
    @Op(name = "Hopper Minecart")
    public boolean hopperMinecart = true;
    @Op(name = "Chest Minecart")
    public boolean chestMinecart = true;
    @Op(name = "Furnace Minecart")
    public boolean furnaceMinecart = true;
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
    public int hopperColor = new Color(42, 42, 42).getRGB();
    @OpChild(name = "Furnace Color", isColor = true, parent = "Furnace")
    public int furnaceColor = new Color(201, 201, 201).getRGB();
    @OpChild(name = "Dispenser Color", isColor = true, parent = "Dispenser/Dropper")
    public int dispenserColor = new Color(0, 208, 255).getRGB();
    @OpChild(name = "Dropper Color", isColor = true, parent = "Dispenser/Dropper")
    public int dropperColor = new Color(59, 147, 0).getRGB();
    @OpChild(name = "Hopper Minecart Color", isColor = true, parent = "Hopper Minecart")
    public int hopperMinecartColor = new Color(0, 128, 255).getRGB();
    @OpChild(name = "Chest Minecart Color", isColor = true, parent = "Chest Minecart")
    public int chestMinecartColor = new Color(255, 0, 0).getRGB();
    @OpChild(name = "Furnace Minecart Color", isColor = true, parent = "Furnace Minecart")
    public int furnaceMinecartColor = new Color(73, 50, 103).getRGB();

    private String lastMode;
    public StorageESP() {
        new OutlineStorageESP();
        new BoxStorageESP();
    }

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> sendEvent(event), Priority.FIRST);
    @EventPointer
    private final EventListener<EventRender2D> eventRender2DEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventRender2DNoScale> eventRender2DNoScaleEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventHasOutline> eventHasOutlineEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventTeamColor> eventOutlineColorEventListener = new EventListener<>(event -> sendEvent(event));


    private void sendEvent(Event event) {
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
        ChunkAccess chunk = Wrapper.INSTANCE.getWorld().getChunk(blockEntity.getBlockPos());
        if (!Wrapper.INSTANCE.getWorld().getChunkSource().hasChunk(chunk.getPos().x, chunk.getPos().z))
            return false;
        if (blockEntity instanceof ChestBlockEntity)
            return chest;
        if (blockEntity instanceof EnderChestBlockEntity)
            return echest;
        if (blockEntity instanceof ShulkerBoxBlockEntity)
            return shulker;
        if (blockEntity instanceof BarrelBlockEntity)
            return barrel;
        if (blockEntity instanceof SpawnerBlockEntity)
            return spawner;
        if (blockEntity instanceof HopperBlockEntity)
            return hopper;
        if (blockEntity instanceof FurnaceBlockEntity || blockEntity instanceof SmokerBlockEntity || blockEntity instanceof BlastFurnaceBlockEntity)
            return furnace;
        if (blockEntity instanceof DispenserBlockEntity)
            return dispensers;
        return false;
    }

    public boolean isValid(Entity entity) {
        if (entity instanceof MinecartHopper)
            return hopperMinecart;
        if (entity instanceof MinecartChest)
            return chestMinecart;
        if (entity instanceof MinecartFurnace)
            return furnaceMinecart;
        return false;
    }

    public int getColor(Entity entity) {
        if (entity instanceof MinecartHopper)
            return hopperMinecartColor;
        if (entity instanceof MinecartChest)
            return chestMinecartColor;
        if (entity instanceof MinecartFurnace)
            return furnaceMinecartColor;
        return -1;
    }

    public int getColor(BlockEntity blockEntity) {
        if (blockEntity instanceof ChestBlockEntity)
            return chestColor;
        if (blockEntity instanceof EnderChestBlockEntity)
            return enderchestColor;
        if (blockEntity instanceof SpawnerBlockEntity)
            return spawnerColor;
        if (blockEntity instanceof ShulkerBoxBlockEntity)
            return shulkerColor;
        if (blockEntity instanceof BarrelBlockEntity)
            return barrelColor;
        if (blockEntity instanceof HopperBlockEntity)
            return hopperColor;
        if (blockEntity instanceof FurnaceBlockEntity || blockEntity instanceof SmokerBlockEntity || blockEntity instanceof BlastFurnaceBlockEntity)
            return furnaceColor;
        if (blockEntity instanceof DropperBlockEntity)
            return dropperColor;
        if (blockEntity instanceof DispenserBlockEntity)
            return dispenserColor;
        return 0xffffffff;
    }
}
