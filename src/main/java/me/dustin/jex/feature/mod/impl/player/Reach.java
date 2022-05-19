package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventGetReachDistance;
import me.dustin.jex.event.player.EventHasExtendedReach;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.event.world.EventInteractBlock;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Reach extends Feature {
    @Op(name = "Distance", min = 5, max = 24, inc = 0.05f)
    public float distance = 5.5f;

    private Vec3d storedPos;

    public Reach() {
        super(Category.PLAYER, "Stretch Armstrong, but nerfed.");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        this.setSuffix(String.format("%.1f", distance));
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventClickBlock> eventClickBlockEventListener = new EventListener<>(event -> {
        if (distance <= 6)
            return;
        if (event.getMode() == EventClickBlock.Mode.PRE) {
            storedPos = Wrapper.INSTANCE.getPlayer().getPos();
            BlockPos blockPos = event.getBlockPos();
            if (ClientMathHelper.INSTANCE.getDistance(storedPos, Vec3d.ofCenter(blockPos)) > 6) {
                Wrapper.INSTANCE.getPlayer().setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            } else
                storedPos = null;
        } else if (event.getMode() == EventClickBlock.Mode.POST) {
            if (storedPos != null) {
                Wrapper.INSTANCE.getPlayer().setPos(storedPos.getX(), storedPos.getY(), storedPos.getZ());
                storedPos = null;
            }
        }
    });

    @EventPointer
    private final EventListener<EventInteractBlock> eventInteractBlockEventListener = new EventListener<>(event -> {
        if (distance <= 6)
            return;
        if (event.getMode() == EventInteractBlock.Mode.PRE) {
            storedPos = Wrapper.INSTANCE.getPlayer().getPos();
            BlockPos blockPos = event.getPos().offset(event.getBlockHitResult().getSide()).offset(event.getBlockHitResult().getSide());
            if (ClientMathHelper.INSTANCE.getDistance(storedPos, Vec3d.ofCenter(blockPos)) > 6) {
                Wrapper.INSTANCE.getPlayer().setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                NetworkHelper.INSTANCE.sendPacketDirect(new PlayerMoveC2SPacket.PositionAndOnGround(blockPos.getX(), blockPos.getY(), blockPos.getZ(), false));
            } else
                storedPos = null;
        } else if (event.getMode() == EventInteractBlock.Mode.POST) {
            if (storedPos != null) {
                Wrapper.INSTANCE.getPlayer().setPos(storedPos.getX(), storedPos.getY(), storedPos.getZ());
                storedPos = null;
            }
        }
    });

    @EventPointer
    private final EventListener<EventGetReachDistance> eventGetReachDistanceEventListener = new EventListener<>(event -> {
        event.setReachDistance(distance);
    });

    @EventPointer
    private final EventListener<EventHasExtendedReach> eventHasExtendedReachEventListener = new EventListener<>(event -> {
        event.setExtendedReach(true);
    });

    @Override
    public void onDisable() {
        storedPos = null;
        super.onDisable();
    }
}
