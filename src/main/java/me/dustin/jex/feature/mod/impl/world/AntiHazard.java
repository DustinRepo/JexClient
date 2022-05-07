package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.world.EventBlockCollisionShape;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.Shapes;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Prevent yourself from walking into hazards like cactus")
public class AntiHazard extends Feature {

    @Op(name = "Cactus")
    public boolean cactus = true;
    @Op(name = "Fire")
    public boolean fire = true;
    @Op(name = "Wither Rose")
    public boolean witherRose = true;
    @Op(name = "Berry Bush")
    public boolean berryBush = true;
    @Op(name = "Lava")
    public boolean lava = true;
    @Op(name = "Powdered Snow")
    public boolean powderedSnow = true;

    @EventPointer
    private final EventListener<EventBlockCollisionShape> eventBlockCollisionShapeEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getWorld() == null || Wrapper.INSTANCE.getLocalPlayer() == null || Wrapper.INSTANCE.getLocalPlayer().tickCount < 20)
            return;
       if (event.getBlock() == Blocks.CACTUS && cactus) {
           event.setVoxelShape(Shapes.create(WorldHelper.SINGLE_BOX));
           event.cancel();
       } else if (event.getBlock() == Blocks.FIRE || event.getBlock() == Blocks.SOUL_FIRE) {
           event.setVoxelShape(Shapes.create(WorldHelper.SINGLE_BOX));
           event.cancel();
       } else if (event.getBlock() == Blocks.WITHER_ROSE && witherRose) {
           event.setVoxelShape(Shapes.create(WorldHelper.SINGLE_BOX));
           event.cancel();
       } else if (event.getBlock() == Blocks.SWEET_BERRY_BUSH && berryBush) {
           event.setVoxelShape(Shapes.create(WorldHelper.SINGLE_BOX));
           event.cancel();
       } else if (event.getBlock() == Blocks.LAVA && lava) {
           event.setVoxelShape(Shapes.create(WorldHelper.SINGLE_BOX));
           event.cancel();
       } else if (event.getBlock() == Blocks.POWDER_SNOW && powderedSnow) {
           event.setVoxelShape(Shapes.create(WorldHelper.SINGLE_BOX));
           event.cancel();
       }
    });
}
