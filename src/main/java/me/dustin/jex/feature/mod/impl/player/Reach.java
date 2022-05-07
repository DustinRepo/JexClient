package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClickBlockFilter;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventGetReachDistance;
import me.dustin.jex.event.player.EventHasExtendedReach;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.world.EventBreakBlock;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.event.world.EventInteractBlock;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Stretch Armstrong, but nerfed.")
public class Reach extends Feature {
    @Op(name = "Distance", min = 5, max = 24, inc = 0.05f)
    public float distance = 5.5f;

    private Vec3 storedPos;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        this.setSuffix(String.format("%.1f", distance));
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventClickBlock> eventClickBlockEventListener = new EventListener<>(event -> {
        if (distance <= 6)
            return;
        if (event.getMode() == EventClickBlock.Mode.PRE) {
            storedPos = Wrapper.INSTANCE.getPlayer().position();
            BlockPos blockPos = event.getBlockPos();
            if (ClientMathHelper.INSTANCE.getDistance(storedPos, Vec3.atCenterOf(blockPos)) > 6) {
                Wrapper.INSTANCE.getPlayer().setPosRaw(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            } else
                storedPos = null;
        } else if (event.getMode() == EventClickBlock.Mode.POST) {
            if (storedPos != null) {
                Wrapper.INSTANCE.getPlayer().setPosRaw(storedPos.x(), storedPos.y(), storedPos.z());
                storedPos = null;
            }
        }
    });

    @EventPointer
    private final EventListener<EventInteractBlock> eventInteractBlockEventListener = new EventListener<>(event -> {
        if (distance <= 6)
            return;
        if (event.getMode() == EventInteractBlock.Mode.PRE) {
            storedPos = Wrapper.INSTANCE.getPlayer().position();
            BlockPos blockPos = event.getPos().relative(event.getBlockHitResult().getDirection()).relative(event.getBlockHitResult().getDirection());
            if (ClientMathHelper.INSTANCE.getDistance(storedPos, Vec3.atCenterOf(blockPos)) > 6) {
                Wrapper.INSTANCE.getPlayer().setPosRaw(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                NetworkHelper.INSTANCE.sendPacketDirect(new ServerboundMovePlayerPacket.Pos(blockPos.getX(), blockPos.getY(), blockPos.getZ(), false));
            } else
                storedPos = null;
        } else if (event.getMode() == EventInteractBlock.Mode.POST) {
            if (storedPos != null) {
                Wrapper.INSTANCE.getPlayer().setPosRaw(storedPos.x(), storedPos.y(), storedPos.z());
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
