package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.awt.*;

public class Nuker extends Feature {
    public Nuker() {
        super(Category.WORLD, "Nuke the blocks around you. (Creative mode only)");
    }

    private BlockPos survivalPos;
    private final StopWatch stopWatch = new StopWatch();

    public final Property<Integer> distanceProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Distance")
            .description("The distance at which to break blocks")
            .value(4)
            .min(3)
            .max(7)
            .build();
    public final Property<Long> delayProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Break Delay")
            .description("Delay between breaking blocks in milliseconds")
            .value(0L)
            .max(500)
            .inc(10)
            .depends(ignored -> {
                PlayerListEntry playerListEntry =  Wrapper.INSTANCE.getLocalPlayer().networkHandler.getPlayerListEntry(Wrapper.INSTANCE.getLocalPlayer().getUuid());
                if (playerListEntry != null)
                    return playerListEntry.getGameMode() != null && playerListEntry.getGameMode() == GameMode.CREATIVE;
                return false;
            })
            .build();
    public final Property<Boolean> keepFloorProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Keep Floor")
            .description("Don't break blocks below you, only above.")
            .value(true)
            .depends(ignored -> {
                PlayerListEntry playerListEntry =  Wrapper.INSTANCE.getLocalPlayer().networkHandler.getPlayerListEntry(Wrapper.INSTANCE.getLocalPlayer().getUuid());
                if (playerListEntry != null)
                    return playerListEntry.getGameMode() != null && playerListEntry.getGameMode() == GameMode.SURVIVAL;
                return false;
            })
            .build();
    public final Property<Color> breakColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Break Color")
            .description("Color for the visualizing of what block you are breaking in survival.")
            .value(Color.MAGENTA)
            .build();

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (!Wrapper.INSTANCE.getLocalPlayer().getAbilities().creativeMode) {
            survivalPos = getClosest();
            if (survivalPos != null) {
                new EventClickBlock(survivalPos, Direction.UP, EventClickBlock.Mode.PRE).run();
                NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, survivalPos, Direction.UP));
                NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, survivalPos, Direction.UP));
                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                return;
            }
            return;
        }
        if (!stopWatch.hasPassed(delayProperty.value()))
            return;
        for (int x = -distanceProperty.value(); x < distanceProperty.value(); x++)
            for (int y = -distanceProperty.value(); y < distanceProperty.value(); y++)
                for (int z = -distanceProperty.value(); z < distanceProperty.value(); z++) {
                    BlockPos pos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, y, z);
                    Block block = WorldHelper.INSTANCE.getBlock(pos);
                    if (!(block instanceof AirBlock || block instanceof FluidBlock)) {
                        NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP));
                        NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP));
                        stopWatch.reset();
                        if (delayProperty.value() > 0)
                            return;
                    }
                }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    public BlockPos getClosest() {
        int minX = -distanceProperty.value();
        int maxX = distanceProperty.value();
        int minY = keepFloorProperty.value() ? 0 : -distanceProperty.value();
        int maxY = distanceProperty.value();
        int minZ = -distanceProperty.value();
        int maxZ = distanceProperty.value();
        double dist = 8;
        BlockPos closest = null;
        for (int x = minX; x < maxX; x++)
            for (int y = minY; y < maxY; y++)
                for (int z = minZ; z < maxZ; z++) {
                    BlockPos pos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, y, z);
                    double distance = ClientMathHelper.INSTANCE.getDistance(Vec3d.of(pos), Wrapper.INSTANCE.getLocalPlayer().getPos().add(0, 1, 0));
                    if (distance < dist) {
                        Block block = WorldHelper.INSTANCE.getBlock(pos);
                        if (!(block instanceof AirBlock || block instanceof FluidBlock) && WorldHelper.INSTANCE.isBreakable(block)) {
                            closest = pos;
                            dist = distance;
                        }
                    }
                }
        return closest;
    }

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        if (survivalPos != null) {
            Vec3d vec = Render3DHelper.INSTANCE.getRenderPosition(survivalPos);
            Box box = WorldHelper.SINGLE_BOX.offset(vec);
            Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), box, breakColorProperty.value().getRGB());
        }
    });
}
