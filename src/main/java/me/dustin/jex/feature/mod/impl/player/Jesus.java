package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.world.EventBlockCollisionShape;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.PathingHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import org.lwjgl.glfw.GLFW;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Walk on water like Jesus.", key = GLFW.GLFW_KEY_J)
public class Jesus extends Feature {

    @Op(name = "Mode", all = {"Solid", "Dolphin"})
    public String mode = "Solid";
    @OpChild(name = "Jump", parent = "Mode", dependency = "Dolphin")
    public boolean allowJump = true;
    private int ticks;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getPlayer() == null || Wrapper.INSTANCE.getWorld() == null)
            return;
        if (mode.equalsIgnoreCase("Solid") && (WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getPlayer())) && !Wrapper.INSTANCE.getPlayer().isSneaking()) {
            Vec3d orig = Wrapper.INSTANCE.getPlayer().getVelocity();
            Wrapper.INSTANCE.getPlayer().setVelocity(orig.getX(), 0.1, orig.getZ());
        }
        if ((Wrapper.INSTANCE.getPlayer().hasVehicle() && WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getPlayer().getVehicle()))) {
            Vec3d orig = Wrapper.INSTANCE.getPlayer().getVehicle().getVelocity();
            Wrapper.INSTANCE.getPlayer().getVehicle().setVelocity(orig.getX(), 0.3, orig.getZ());
        }
        if (WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getPlayer()) && !Wrapper.INSTANCE.getPlayer().isOnGround() && !Wrapper.INSTANCE.getPlayer().isSneaking()) {
            Vec3d orig = Wrapper.INSTANCE.getPlayer().getVelocity();
            Wrapper.INSTANCE.getPlayer().setVelocity(orig.getX(), 0.1, orig.getZ());
        }
        if (WorldHelper.INSTANCE.isOnLiquid(Wrapper.INSTANCE.getPlayer())) {
            if (!Wrapper.INSTANCE.getPlayer().isSneaking()) {
                if (Wrapper.INSTANCE.getOptions().jumpKey.isPressed() && allowJump && mode.equalsIgnoreCase("Dolphin")) {
                    if (ticks != 4) {
                        Wrapper.INSTANCE.getPlayer().jump();
                        Vec3d orig = Wrapper.INSTANCE.getPlayer().getVelocity();
                        Wrapper.INSTANCE.getPlayer().setVelocity(orig.getX() * 0.5f, orig.getY(), orig.getZ() * 0.5f);
                    } else {
                        KeyBinding.setKeyPressed(Wrapper.INSTANCE.getOptions().jumpKey.getDefaultKey(), false);
                    }
                } else if (mode.equalsIgnoreCase("Dolphin") && WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getPlayer()) && !Wrapper.INSTANCE.getPlayer().isSneaking()) {
                    Vec3d orig = Wrapper.INSTANCE.getPlayer().getVelocity();
                    Wrapper.INSTANCE.getPlayer().setVelocity(orig.getX(), 0.1, orig.getZ());
                }
            }
        }
    }, Priority.SECOND_LAST, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventBlockCollisionShape> eventBlockCollisionShapeEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getPlayer() == null || Wrapper.INSTANCE.getWorld() == null || mode.equalsIgnoreCase("Dolphin") || event.getBlockPos() == null)
            return;
        if (Wrapper.INSTANCE.getPlayer().isSubmergedInWater() || Wrapper.INSTANCE.getPlayer().isInLava() || (event.getBlockPos().getY() < Wrapper.INSTANCE.getPlayer().getY() + 0.5f && WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getPlayer())) || Wrapper.INSTANCE.getPlayer().isSneaking() || Wrapper.INSTANCE.getPlayer().fallDistance > 3)
            return;
        if (WorldHelper.INSTANCE.isWaterlogged(event.getBlockPos()) && event.getVoxelShape().isEmpty()) {
            FluidState fluidState = WorldHelper.INSTANCE.getFluidState(event.getBlockPos());
            if (fluidState.getLevel() == 8) {
                Box waterBox = new Box(0.1f, 0, 0.1f, 0.9f, Wrapper.INSTANCE.getPlayer().hasVehicle() ? 0.92f : 1, 0.9f);
                event.setVoxelShape(VoxelShapes.cuboid(waterBox));
            } else
                event.setVoxelShape(fluidState.getShape(Wrapper.INSTANCE.getWorld(), event.getBlockPos()));
            event.cancel();
        }
    });

    @EventPointer
    private final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> {
        BaritoneHelper.INSTANCE.setAssumeJesus(true);
        if ((WorldHelper.INSTANCE.isOnLiquid(Wrapper.INSTANCE.getPlayer()) || WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getPlayer())) && mode.equalsIgnoreCase("Dolphin")) {
            if (PlayerHelper.INSTANCE.isMoving())
                PlayerHelper.INSTANCE.setMoveSpeed((EventMove) event, 2.5 / 20);
            else
                PlayerHelper.INSTANCE.setMoveSpeed((EventMove) event, 0);
        }
        this.setSuffix(mode);
    });

    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        if (WorldHelper.INSTANCE.isOnLiquid(Wrapper.INSTANCE.getPlayer()) || WorldHelper.INSTANCE.isTouchingLiquidBlockSpace(Wrapper.INSTANCE.getPlayer())) {
            if (ticks >= 4) {
                PlayerMoveC2SPacket origPacket = (PlayerMoveC2SPacket) event.getPacket();
                PlayerMoveC2SPacket playerMoveC2SPacket = new PlayerMoveC2SPacket.Full(origPacket.getX(Wrapper.INSTANCE.getPlayer().getX()), origPacket.getY(Wrapper.INSTANCE.getPlayer().getY()) - 0.02, origPacket.getZ(Wrapper.INSTANCE.getPlayer().getZ()), origPacket.getYaw(PlayerHelper.INSTANCE.getYaw()), origPacket.getPitch(PlayerHelper.INSTANCE.getPitch()), origPacket.isOnGround());
                event.setPacket(playerMoveC2SPacket);
                ticks = 0;
            } else
                ticks++;
        } else {
            ticks = 0;
        }
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE, PlayerMoveC2SPacket.class));

    @Override
    public void onDisable() {
        BaritoneHelper.INSTANCE.setAssumeJesus(false);
        super.onDisable();
    }
}
