package me.dustin.jex.feature.mod.impl.movement.speed.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.impl.movement.speed.Speed;
import net.minecraft.util.math.Vec3d;

public class NCPSpeed extends FeatureExtension {

    public static double stage;
    public NCPSpeed() {
        super("Floaty", Speed.class);
    }

    @Override
    public void pass(Event event) {
        if (event instanceof EventPlayerPackets) {
            if (((EventPlayerPackets) event).getMode() == EventPlayerPackets.Mode.PRE) {
            if ((Wrapper.INSTANCE.getLocalPlayer().forwardSpeed != 0.0F || Wrapper.INSTANCE.getLocalPlayer().sidewaysSpeed != 0.0F) && !Wrapper.INSTANCE.getLocalPlayer().horizontalCollision) {
                if (Wrapper.INSTANCE.getLocalPlayer().fallDistance > 3.994D)
                    return;
                //Wrapper.INSTANCE.getLocalPlayer().horizontalSpeed *= 1.0485F;
                if (Wrapper.INSTANCE.getLocalPlayer().isSubmergedInWater())
                    return;
                //Wrapper.INSTANCE.getLocalPlayer().setPos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() - 0.3993000090122223D, Wrapper.INSTANCE.getLocalPlayer().getZ());
                Vec3d velo = Wrapper.INSTANCE.getLocalPlayer().getVelocity();

                Wrapper.INSTANCE.getLocalPlayer().setVelocity(velo.x, -1000, velo.z);
                Wrapper.INSTANCE.getIRenderTickCounter().setTimeScale(1000 / 20);
            }
            } else {
                if (Wrapper.INSTANCE.getLocalPlayer().isSubmergedInWater())
                    return;
                if (Wrapper.INSTANCE.getLocalPlayer().isOnGround() && (Wrapper.INSTANCE.getLocalPlayer().forwardSpeed != 0.0F || Wrapper.INSTANCE.getLocalPlayer().sidewaysSpeed != 0.0F) && !Wrapper.INSTANCE.getLocalPlayer().horizontalCollision) {
                    Vec3d velo = Wrapper.INSTANCE.getLocalPlayer().getVelocity();

                    Wrapper.INSTANCE.getLocalPlayer().setPos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() + 0.3993000090122223D, Wrapper.INSTANCE.getLocalPlayer().getZ());
                    Wrapper.INSTANCE.getLocalPlayer().setVelocity(ClientMathHelper.INSTANCE.clamp((float)velo.x * 1.590000033378601f, -0.399f, 0.399f), 0.3993000090122223D, ClientMathHelper.INSTANCE.clamp((float)velo.z * 1.590000033378601f, -0.399f, 0.399f));
                    Wrapper.INSTANCE.getIRenderTickCounter().setTimeScale(1000 / (20 * 1.199f));
                }
            }
        }
        /*if (event instanceof EventPacketSent) {
            if (((EventPacketSent) event).getPacket() instanceof PlayerMoveC2SPacket) {
                PlayerMoveC2SPacket packet = (PlayerMoveC2SPacket) ((EventPacketSent) event).getPacket();
                if (packet.isOnGround()) {
                    Wrapper.INSTANCE.getLocalPlayer().stepHeight = 0;
                    if (stage == 1 || stage == 2) {
                        BlockPos pos = new BlockPos(packet.getX(Wrapper.INSTANCE.getLocalPlayer().getX()), packet.getY(Wrapper.INSTANCE.getLocalPlayer().getY()) + 2, packet.getZ(Wrapper.INSTANCE.getLocalPlayer().getZ()));
                        if (Wrapper.INSTANCE.getWorld().getBlockState(pos).getBlock() instanceof AirBlock) {
                            ((EventPacketSent) event).setPacket(new PlayerMoveC2SPacket.PositionOnly(packet.getX(Wrapper.INSTANCE.getLocalPlayer().getX()), packet.getY(Wrapper.INSTANCE.getLocalPlayer().getY()) + 0.4994, packet.getZ(Wrapper.INSTANCE.getLocalPlayer().getZ()), packet.isOnGround()));
                        } else if (Wrapper.INSTANCE.getWorld().getBlockState(new BlockPos(packet.getX(Wrapper.INSTANCE.getLocalPlayer().getX()), packet.getY(Wrapper.INSTANCE.getLocalPlayer().getY()) - 0.05, packet.getZ(Wrapper.INSTANCE.getLocalPlayer().getZ()))).getBlock() instanceof TrapdoorBlock) {
                            ((EventPacketSent) event).setPacket(new PlayerMoveC2SPacket.PositionOnly(packet.getX(Wrapper.INSTANCE.getLocalPlayer().getX()), packet.getY(Wrapper.INSTANCE.getLocalPlayer().getY()) + 0.2047683716f - 0.1875, packet.getZ(Wrapper.INSTANCE.getLocalPlayer().getZ()), packet.isOnGround()));
                        } else {
                            ((EventPacketSent) event).setPacket(new PlayerMoveC2SPacket.PositionOnly(packet.getX(Wrapper.INSTANCE.getLocalPlayer().getX()), packet.getY(Wrapper.INSTANCE.getLocalPlayer().getY()) + 0.2047683716f, packet.getZ(Wrapper.INSTANCE.getLocalPlayer().getZ()), packet.isOnGround()));
                        }
                    } else {
                        Wrapper.INSTANCE.getLocalPlayer().stepHeight = 0.6f;
                    }
                }
            }
        }*/
    }

    @Override
    public void enable() {
    }

    @Override
    public void disable() {
        Wrapper.INSTANCE.getIRenderTickCounter().setTimeScale(1000 / 20);
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            Wrapper.INSTANCE.getLocalPlayer().stepHeight = 0.6f;
            Vec3d velo = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
            if (velo.y > 0)
                Wrapper.INSTANCE.getLocalPlayer().setVelocity(velo.x, -1000, velo.z);
        }
    }
}
