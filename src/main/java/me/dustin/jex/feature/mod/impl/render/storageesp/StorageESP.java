package me.dustin.jex.feature.mod.impl.render.storageesp;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.event.render.*;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.impl.render.storageesp.impl.OutlineStorageESP;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.SmokerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.world.chunk.Chunk;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.storageesp.impl.BoxStorageESP;
import java.awt.*;

public class StorageESP extends Feature {

    public final Property<Mode> modeProperty = new Property.PropertyBuilder<Mode>(this.getClass())
            .name("Mode")
            .value(Mode.SHADER)
            .build();
    public final Property<Integer> lineWidthProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Line Width")
            .description("Line width for shaders (in pixels)")
            .value(2)
            .min(1)
            .max(10)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.SHADER)
            .build();
    public final Property<Boolean> glowProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Glow")
            .description("Whether or not to add a glow effect to the outline")
            .value(false)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.SHADER)
            .build();
    public final Property<Float> glowIntensityProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Glow Intensity")
            .description("Intensity for the glow effect")
            .value(0.5f)
            .min(0.1f)
            .max(1)
            .inc(0.1f)
            .parent(glowProperty)
            .depends(parent -> (boolean)parent.value())
            .build();
    public final Property<Boolean> fadeBoxesWhenCloseProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Fade When Close")
            .description("Fade the boxes to make the storage block easier to see.")
            .value(true)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.BOX)
            .build();
    public final Property<Integer> fadeDistanceProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Fade Distance")
            .value(10)
            .min(1)
            .max(50)
            .parent(fadeBoxesWhenCloseProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> chestProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Chest")
            .value(true)
            .build();
    public final Property<Boolean> echestProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Ender Chest")
            .value(true)
            .build();
    public final Property<Boolean> shulkerProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Shulker")
            .value(true)
            .build();
    public final Property<Boolean> barrelProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Barrel")
            .value(true)
            .build();
    public final Property<Boolean> hopperProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Hopper")
            .value(true)
            .build();
    public final Property<Boolean> furnaceProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Furnace")
            .value(true)
            .build();
    public final Property<Boolean> dispensersProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Dispenser/Dropper")
            .value(true)
            .build();
    public final Property<Boolean> hopperMinecartProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Hopper Minecart")
            .value(true)
            .build();
    public final Property<Boolean> chestMinecartProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Chest Minecart")
            .value(true)
            .build();
    public final Property<Boolean> furnaceMinecartProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Furnace Minecart")
            .value(true)
            .build();
    public final Property<Boolean> spawnerProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Spawner")
            .value(true)
            .build();
    public final Property<Color> chestColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Chest Color")
            .value(Color.YELLOW)
            .parent(chestProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Color> enderchestColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Ender Chest Color")
            .value(new Color(147, 0, 255))
            .parent(echestProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Color> shulkerColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Shulker Color")
            .value(new Color(255, 0, 239))
            .parent(shulkerProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Color> spawnerColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Spawner Color")
            .value(new Color(161, 255, 0))
            .parent(spawnerProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Color> barrelColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Barrel Color")
            .value(new Color(215, 82, 0))
            .parent(barrelProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Color> hopperColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Hopper Color")
            .value(new Color(42, 42, 42))
            .parent(hopperProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Color> furnaceColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Furnace Color")
            .value(new Color(201, 201, 201))
            .parent(furnaceProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Color> dispenserColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Dispenser Color")
            .value(new Color(0, 208, 255))
            .parent(dispensersProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Color> dropperColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Dropper Color")
            .value(new Color(59, 147, 0))
            .parent(dispensersProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Color> hopperMinecartColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Hopper Minecart Color")
            .value(new Color(0, 128, 255))
            .parent(hopperMinecartProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Color> chestMinecartColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Chest Minecart Color")
            .value(new Color(255, 0, 0))
            .parent(chestMinecartProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Color> furnaceMinecartColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Furnace Minecart Color")
            .value(new Color(73, 50, 103))
            .parent(furnaceMinecartProperty)
            .depends(parent -> (boolean) parent.value())
            .build();

    private Mode lastMode;
    public StorageESP() {
        super(Category.VISUAL, "Show storage blocks through walls");
        new OutlineStorageESP();
        new BoxStorageESP();
    }

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> sendEvent(event), Priority.FIRST);
    @EventPointer
    private final EventListener<EventWorldRender> eventWorldRenderEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventRender2D> eventRender2DEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventRender2DNoScale> eventRender2DNoScaleEventListener = new EventListener<>(event -> sendEvent(event));

    private void sendEvent(Event event) {
        if (lastMode != null && modeProperty.value() != lastMode) {
            FeatureExtension.get(lastMode, this).disable();
            FeatureExtension.get(modeProperty.value(), this).enable();
        }
        FeatureExtension.get(modeProperty.value(), this).pass(event);
        this.setSuffix(modeProperty.value());
        lastMode = modeProperty.value();
    }

    @Override
    public void onEnable() {
        FeatureExtension.get(modeProperty.value(), this).enable();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        FeatureExtension.get(modeProperty.value(), this).disable();
        super.onDisable();
    }

    public boolean isValid(BlockEntity blockEntity) {
        Chunk chunk = Wrapper.INSTANCE.getWorld().getChunk(blockEntity.getPos());
        if (!Wrapper.INSTANCE.getWorld().getChunkManager().isChunkLoaded(chunk.getPos().x, chunk.getPos().z))
            return false;
        if (blockEntity instanceof ChestBlockEntity)
            return chestProperty.value();
        if (blockEntity instanceof EnderChestBlockEntity)
            return echestProperty.value();
        if (blockEntity instanceof ShulkerBoxBlockEntity)
            return shulkerProperty.value();
        if (blockEntity instanceof BarrelBlockEntity)
            return barrelProperty.value();
        if (blockEntity instanceof MobSpawnerBlockEntity)
            return spawnerProperty.value();
        if (blockEntity instanceof HopperBlockEntity)
            return hopperProperty.value();
        if (blockEntity instanceof FurnaceBlockEntity || blockEntity instanceof SmokerBlockEntity || blockEntity instanceof BlastFurnaceBlockEntity)
            return furnaceProperty.value();
        if (blockEntity instanceof DispenserBlockEntity)
            return dispensersProperty.value();
        return false;
    }

    public boolean isValid(Entity entity) {
        if (entity instanceof HopperMinecartEntity)
            return hopperMinecartProperty.value();
        if (entity instanceof ChestMinecartEntity)
            return chestMinecartProperty.value();
        if (entity instanceof FurnaceMinecartEntity)
            return furnaceMinecartProperty.value();
        return false;
    }

    public int getColor(Entity entity) {
        if (entity instanceof HopperMinecartEntity)
            return hopperMinecartColorProperty.value().getRGB();
        if (entity instanceof ChestMinecartEntity)
            return chestMinecartColorProperty.value().getRGB();
        if (entity instanceof FurnaceMinecartEntity)
            return furnaceMinecartColorProperty.value().getRGB();
        return -1;
    }

    public int getColor(BlockEntity blockEntity) {
        if (blockEntity instanceof ChestBlockEntity)
            return chestColorProperty.value().getRGB();
        if (blockEntity instanceof EnderChestBlockEntity)
            return enderchestColorProperty.value().getRGB();
        if (blockEntity instanceof MobSpawnerBlockEntity)
            return spawnerColorProperty.value().getRGB();
        if (blockEntity instanceof ShulkerBoxBlockEntity)
            return shulkerColorProperty.value().getRGB();
        if (blockEntity instanceof BarrelBlockEntity)
            return barrelColorProperty.value().getRGB();
        if (blockEntity instanceof HopperBlockEntity)
            return hopperColorProperty.value().getRGB();
        if (blockEntity instanceof FurnaceBlockEntity || blockEntity instanceof SmokerBlockEntity || blockEntity instanceof BlastFurnaceBlockEntity)
            return furnaceColorProperty.value().getRGB();
        if (blockEntity instanceof DropperBlockEntity)
            return dropperColorProperty.value().getRGB();
        if (blockEntity instanceof DispenserBlockEntity)
            return dispenserColorProperty.value().getRGB();
        return 0xffffffff;
    }

    public enum Mode {
        BOX, SHADER
    }
}
