package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.filters.SoundFilter;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventGetPose;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.world.EventBlockCollisionShape;
import me.dustin.jex.event.world.EventPlaySound;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.EntityPose;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import org.lwjgl.glfw.GLFW;

public class Jesus extends Feature {

    public final Property<Mode> modeProperty = new Property.PropertyBuilder<Mode>(this.getClass())
            .name("Mode")
            .value(Mode.SOLID)
            .build();
    public final Property<Boolean> allowJumpProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Jump")
            .value(true)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.DOLPHIN)
            .build();
    private int ticks;

    public Jesus() {
        super(Category.PLAYER, "", GLFW.GLFW_KEY_J);
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getPlayer() == null || Wrapper.INSTANCE.getWorld() == null)
            return;
        if (modeProperty.value() == Mode.SWIM) {
            if (Wrapper.INSTANCE.getLocalPlayer().isSneaking())
                return;
            if (WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getPlayer())) {
                Block insideBlock = WorldHelper.INSTANCE.getBlockBelowEntity(Wrapper.INSTANCE.getPlayer(), 0);
                Block belowBlock = WorldHelper.INSTANCE.getBlockBelowEntity(Wrapper.INSTANCE.getPlayer(), 0.5f);
                Vec3d orig = Wrapper.INSTANCE.getPlayer().getVelocity();
                if (insideBlock instanceof FluidBlock && belowBlock instanceof FluidBlock && !Wrapper.INSTANCE.getPlayer().horizontalCollision) {
                    FluidState fluidState = WorldHelper.INSTANCE.getFluidState(Wrapper.INSTANCE.getPlayer().getBlockPos());
                    double swimHeight = Wrapper.INSTANCE.getLocalPlayer().getSwimHeight();
                    if (fluidState.getHeight() < swimHeight || !(fluidState.getFluid() instanceof WaterFluid))
                        return;
                    if (!Wrapper.INSTANCE.getPlayer().isSwimming() && PlayerHelper.INSTANCE.isMoving()) {
                        Wrapper.INSTANCE.getPlayer().setSprinting(true);
                        Wrapper.INSTANCE.getPlayer().setVelocity(orig.getX(), -0.05, orig.getZ());
                    } else {
                        if (Wrapper.INSTANCE.getPlayer().isSubmergedInWater())
                            Wrapper.INSTANCE.getPlayer().setVelocity(orig.getX(), 0.05, orig.getZ());
                        else
                            Wrapper.INSTANCE.getPlayer().setVelocity(orig.getX(), 0, orig.getZ());
                    }
                }
            }
            return;
        }
        if (modeProperty.value() == Mode.SOLID && (WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getPlayer())) && !Wrapper.INSTANCE.getPlayer().isSneaking()) {
            Vec3d orig = Wrapper.INSTANCE.getPlayer().getVelocity();
            Wrapper.INSTANCE.getPlayer().setVelocity(orig.getX(), 0.30, orig.getZ());
        }
        if (modeProperty.value() == Mode.SOLIDOLD && (WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getPlayer())) && !Wrapper.INSTANCE.getPlayer().isSneaking()) {
            Vec3d orig = Wrapper.INSTANCE.getPlayer().getVelocity();
            Wrapper.INSTANCE.getPlayer().setVelocity(orig.getX(), 0.11, orig.getZ());
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
                if (Wrapper.INSTANCE.getOptions().jumpKey.isPressed() && allowJumpProperty.value() && modeProperty.value() == Mode.DOLPHIN) {
                    if (ticks != 4) {
                        Wrapper.INSTANCE.getPlayer().jump();
                        Vec3d orig = Wrapper.INSTANCE.getPlayer().getVelocity();
                        Wrapper.INSTANCE.getPlayer().setVelocity(orig.getX() * 0.5f, orig.getY(), orig.getZ() * 0.5f);
                    } else {
                        KeyBinding.setKeyPressed(Wrapper.INSTANCE.getOptions().jumpKey.getDefaultKey(), false);
                    }
                } else if (modeProperty.value() == Mode.DOLPHIN && WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getPlayer()) && !Wrapper.INSTANCE.getPlayer().isSneaking()) {
                    Vec3d orig = Wrapper.INSTANCE.getPlayer().getVelocity();
                    Wrapper.INSTANCE.getPlayer().setVelocity(orig.getX(), 0.1, orig.getZ());
                }
            }
        }
    }, Priority.SECOND_LAST, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventPlaySound> eventPlaySoundEventListener = new EventListener<>(event -> {
        if (modeProperty.value() == Mode.SWIM)
            event.cancel();
    }, new SoundFilter(EventPlaySound.Mode.PRE, new Identifier("ambient.underwater.enter"), new Identifier("ambient.underwater.exit"), new Identifier("ambient.underwater.loop"), new Identifier("entity.player.swim"), new Identifier("ambient.underwater.loop"), new Identifier("ambient.underwater.loop.additions")));

    @EventPointer
    private final EventListener<EventBlockCollisionShape> eventBlockCollisionShape = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getPlayer() == null || Wrapper.INSTANCE.getWorld() == null || modeProperty.value() != Mode.SOLID || event.getBlockPos() == null)
            return;
        if (Wrapper.INSTANCE.getPlayer().isSubmergedInWater() || Wrapper.INSTANCE.getPlayer().isInLava() || (event.getBlockPos().getY() < Wrapper.INSTANCE.getPlayer().getY() + 0.5f && WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getPlayer())) || Wrapper.INSTANCE.getPlayer().isSneaking() || Wrapper.INSTANCE.getPlayer().fallDistance > 3)
            return;
        if (WorldHelper.INSTANCE.isWaterlogged(event.getBlockPos()) && event.getVoxelShape().isEmpty()) {
            FluidState fluidState = WorldHelper.INSTANCE.getFluidState(event.getBlockPos());
            if (fluidState.getLevel() == 8) {      
                event.setVoxelShape(VoxelShapes.fullCube());
            } else
                event.setVoxelShape(fluidState.getShape(Wrapper.INSTANCE.getWorld(), event.getBlockPos()));
            event.cancel();
        }
    });
    
    @EventPointer
    private final EventListener<EventBlockCollisionShape> eventBlockCollisionShapeEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getPlayer() == null || Wrapper.INSTANCE.getWorld() == null || modeProperty.value() != Mode.SOLIDOLD || event.getBlockPos() == null)
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
        if ((WorldHelper.INSTANCE.isOnLiquid(Wrapper.INSTANCE.getPlayer()) || WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getPlayer())) && modeProperty.value() == Mode.DOLPHIN) {
            if (PlayerHelper.INSTANCE.isMoving())
                PlayerHelper.INSTANCE.setMoveSpeed((EventMove) event, 2.5 / 20);
            else
                PlayerHelper.INSTANCE.setMoveSpeed((EventMove) event, 0);
        }
        this.setSuffix(modeProperty.value());
    });

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        if (WorldHelper.INSTANCE.isOnLiquid(Wrapper.INSTANCE.getPlayer()) || WorldHelper.INSTANCE.isTouchingLiquidBlockSpace(Wrapper.INSTANCE.getPlayer())) {
            if (ticks >= 4) {
                PlayerMoveC2SPacket origPacket = (PlayerMoveC2SPacket) event.getPacket();
                PlayerMoveC2SPacket playerMoveC2SPacket = new PlayerMoveC2SPacket.Full(origPacket.getX(Wrapper.INSTANCE.getPlayer().getX()), origPacket.getY(Wrapper.INSTANCE.getPlayer().getY()) - 0.05, origPacket.getZ(Wrapper.INSTANCE.getPlayer().getZ()), origPacket.getYaw(PlayerHelper.INSTANCE.getYaw()), origPacket.getPitch(PlayerHelper.INSTANCE.getPitch()), origPacket.isOnGround());
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

    public enum Mode {
        SOLID, DOLPHIN, SWIM, SOLIDOLD
    }
}
