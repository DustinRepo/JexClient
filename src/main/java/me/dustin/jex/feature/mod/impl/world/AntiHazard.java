package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.world.EventBlockCollisionShape;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.util.shape.VoxelShapes;

public class AntiHazard extends Feature {

    public final Property<Boolean> cactusProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Cactus")
            .value(true)
            .build();
    public final Property<Boolean> fireProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Fire")
            .value(true)
            .build();
    public final Property<Boolean> witherRoseProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Wither Rose")
            .value(true)
            .build();
    public final Property<Boolean> berryBushProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Berry Bush")
            .value(true)
            .build();
    public final Property<Boolean> lavaProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Lava")
            .value(true)
            .build();
    public final Property<Boolean> powderedSnowProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Powdered Snow")
            .value(true)
            .build();

    public AntiHazard() {
        super(Category.WORLD, "Prevent yourself from walking into hazards like cactus");
    }

    @EventPointer
    private final EventListener<EventBlockCollisionShape> eventBlockCollisionShapeEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getWorld() == null || Wrapper.INSTANCE.getLocalPlayer() == null || Wrapper.INSTANCE.getLocalPlayer().age < 20)
            return;
       if (event.getBlock() == Blocks.CACTUS && cactusProperty.value()) {
           event.setVoxelShape(VoxelShapes.cuboid(WorldHelper.SINGLE_BOX));
           event.cancel();
       } else if (event.getBlock() == Blocks.FIRE || event.getBlock() == Blocks.SOUL_FIRE) {
           event.setVoxelShape(VoxelShapes.cuboid(WorldHelper.SINGLE_BOX));
           event.cancel();
       } else if (event.getBlock() == Blocks.WITHER_ROSE && witherRoseProperty.value()) {
           event.setVoxelShape(VoxelShapes.cuboid(WorldHelper.SINGLE_BOX));
           event.cancel();
       } else if (event.getBlock() == Blocks.SWEET_BERRY_BUSH && berryBushProperty.value()) {
           event.setVoxelShape(VoxelShapes.cuboid(WorldHelper.SINGLE_BOX));
           event.cancel();
       } else if (event.getBlock() == Blocks.LAVA && lavaProperty.value()) {
           event.setVoxelShape(VoxelShapes.cuboid(WorldHelper.SINGLE_BOX));
           event.cancel();
       } else if (event.getBlock() == Blocks.POWDER_SNOW && powderedSnowProperty.value()) {
           event.setVoxelShape(VoxelShapes.cuboid(WorldHelper.SINGLE_BOX));
           event.cancel();
       }
    });
}
