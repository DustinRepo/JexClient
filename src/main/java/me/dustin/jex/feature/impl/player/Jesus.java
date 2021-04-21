package me.dustin.jex.feature.impl.player;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.events.core.enums.EventPriority;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.world.EventFluidCollisionShape;
import me.dustin.jex.helper.misc.BaritoneHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.load.impl.IPlayerMoveC2SPacket;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.block.Block;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import org.lwjgl.glfw.GLFW;

@Feat(name = "Jesus", category = FeatureCategory.PLAYER, description = "Walk on water like Jesus.")
public class Jesus extends Feature {

    @Op(name = "Mode", all = {"Solid", "Dolphin"})
    public String mode = "Solid";
    @OpChild(name = "Jump", parent = "Mode", dependency = "Dolphin")
    public boolean allowJump = true;
    private int ticks;

    public Jesus() {
        this.setKey(GLFW.GLFW_KEY_J);
    }

    @EventListener(events = {EventPlayerPackets.class, EventFluidCollisionShape.class, EventMove.class, EventPacketSent.class}, priority = EventPriority.LOW)
    public void run(Event event) {
        BaritoneHelper.INSTANCE.setAssumeJesus(true);
        if (event instanceof EventMove) {
            if ((WorldHelper.INSTANCE.isOnLiquid(Wrapper.INSTANCE.getLocalPlayer()) || WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getLocalPlayer())) && mode.equalsIgnoreCase("Dolphin")) {
                if (PlayerHelper.INSTANCE.isMoving())
                    PlayerHelper.INSTANCE.setMoveSpeed((EventMove) event, 2.5 / 20);
                else
                    PlayerHelper.INSTANCE.setMoveSpeed((EventMove) event, 0);
            }
            this.setSuffix(mode);
        }
        if (event instanceof EventPlayerPackets) {
            if (Wrapper.INSTANCE.getLocalPlayer() == null || Wrapper.INSTANCE.getWorld() == null)
                return;
            if (((EventPlayerPackets) event).getMode() == EventPlayerPackets.Mode.PRE) {
                if (mode.equalsIgnoreCase("Solid") && (WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getLocalPlayer())) && !Wrapper.INSTANCE.getLocalPlayer().isSneaking()) {
                    Vec3d orig = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
                    Wrapper.INSTANCE.getLocalPlayer().setVelocity(orig.getX(), 0.1, orig.getZ());
                }
                if ((Wrapper.INSTANCE.getLocalPlayer().isRiding() && WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getLocalPlayer().getVehicle()))) {
                    Vec3d orig = Wrapper.INSTANCE.getLocalPlayer().getVehicle().getVelocity();
                    Wrapper.INSTANCE.getLocalPlayer().getVehicle().setVelocity(orig.getX(), 0.3, orig.getZ());
                }
                if (WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getLocalPlayer()) && Wrapper.INSTANCE.getLocalPlayer().isOnGround() && !Wrapper.INSTANCE.getLocalPlayer().isSneaking()) {
                    Vec3d orig = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
                    Wrapper.INSTANCE.getLocalPlayer().setVelocity(orig.getX(), 0.1, orig.getZ());
                }
                if (WorldHelper.INSTANCE.isOnLiquid(Wrapper.INSTANCE.getLocalPlayer())) {
                    if (!Wrapper.INSTANCE.getLocalPlayer().isSneaking()) {
                        if (Wrapper.INSTANCE.getOptions().keyJump.isPressed() && allowJump) {
                            if (ticks != 4) {
                                Wrapper.INSTANCE.getLocalPlayer().jump();
                                Vec3d orig = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
                                Wrapper.INSTANCE.getLocalPlayer().setVelocity(orig.getX() * 0.5f, orig.getY(), orig.getZ() * 0.5f);
                            } else {
                                KeyBinding.setKeyPressed(Wrapper.INSTANCE.getOptions().keyJump.getDefaultKey(), false);
                            }
                        } else if (mode.equalsIgnoreCase("Dolphin") && WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getLocalPlayer()) && !Wrapper.INSTANCE.getLocalPlayer().isSneaking()) {
                            Vec3d orig = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
                            Wrapper.INSTANCE.getLocalPlayer().setVelocity(orig.getX(), 0.1, orig.getZ());
                        }
                    }
                }

            }
        }
        if (event instanceof EventFluidCollisionShape) {
            if (Wrapper.INSTANCE.getLocalPlayer() == null || Wrapper.INSTANCE.getWorld() == null || mode.equalsIgnoreCase("Dolphin"))
                return;
            EventFluidCollisionShape eventBox = (EventFluidCollisionShape) event;
            Block block = eventBox.getBlock();
            if (block instanceof FluidBlock) {
                Box waterBox = new Box(0.1f, 0, 0.1f, 0.9f, Wrapper.INSTANCE.getLocalPlayer().isRiding() ? 0.92f : 1, 0.9f);
                if (WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getLocalPlayer()) || Wrapper.INSTANCE.getLocalPlayer().isSneaking() || Wrapper.INSTANCE.getLocalPlayer().fallDistance > 3)
                    eventBox.setVoxelShape(VoxelShapes.empty());
                else
                    eventBox.setVoxelShape(VoxelShapes.cuboid(waterBox));

                eventBox.cancel();
            }
        }
        if (event instanceof EventPacketSent) {
            EventPacketSent sent = (EventPacketSent) event;
            if (sent.getPacket() instanceof PlayerMoveC2SPacket) {
                if (WorldHelper.INSTANCE.isOnLiquid(Wrapper.INSTANCE.getLocalPlayer())) {
                    if (ticks >= 4) {
                        ((IPlayerMoveC2SPacket) ((PlayerMoveC2SPacket) sent.getPacket())).setY(((IPlayerMoveC2SPacket) ((PlayerMoveC2SPacket) sent.getPacket())).getY() - 0.02);
                        ticks = 0;
                    } else
                        ticks++;
                } else {
                    ticks = 0;
                }
            }
        }
    }

    @Override
    public void onDisable() {
        BaritoneHelper.INSTANCE.setAssumeJesus(false);
        super.onDisable();
    }
}
