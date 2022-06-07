package me.dustin.jex.feature.mod.impl.movement.elytraplus.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.elytraplus.ElytraPlus;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;

public class ECMEElytraFly extends FeatureExtension {
    private ElytraPlus elytraPlus;
    private boolean wasFlying;
    public ECMEElytraFly() {
        super(ElytraPlus.Mode.ECME, ElytraPlus.class);
    }

    @Override
    public void pass(Event event) {
        if (elytraPlus == null)
            elytraPlus = Feature.get(ElytraPlus.class);
        if (event instanceof EventMove eventMove) {
            if (Wrapper.INSTANCE.getLocalPlayer().isFallFlying()) {
                if (!wasFlying) {
                    for (int i = 0; i < 10; i++) {
                        NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
                        NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() - 0.05, Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
                    }
                }
                float radianYaw = (float) Math.toRadians(Wrapper.INSTANCE.getLocalPlayer().getYaw());
                double currentVel = Math.abs(Wrapper.INSTANCE.getLocalPlayer().getVelocity().x) + Math.abs(Wrapper.INSTANCE.getLocalPlayer().getVelocity().y) + Math.abs(Wrapper.INSTANCE.getLocalPlayer().getVelocity().z);
                if (Wrapper.INSTANCE.getOptions().forwardKey.isPressed()) {
                    if (currentVel <= 2f)
                        Wrapper.INSTANCE.getLocalPlayer().addVelocity(MathHelper.sin(radianYaw) * -0.05, 0, MathHelper.cos(radianYaw) * 0.05);
                } else {
                    eventMove.setX(0);
                    eventMove.setZ(0);
                }
                if (eventMove.getY() <= 0)
                    eventMove.setY(Wrapper.INSTANCE.getOptions().sneakKey.isPressed() ? -0.75 : -0.0001);
            }
            if (Wrapper.INSTANCE.getOptions().jumpKey.isPressed()) {
                if (wasFlying) {
                    if (Wrapper.INSTANCE.getLocalPlayer().isFallFlying()) {
                        if (Wrapper.INSTANCE.getLocalPlayer().age % 5 == 0) {
                            Wrapper.INSTANCE.getLocalPlayer().setPosition(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() + 5, Wrapper.INSTANCE.getLocalPlayer().getZ());
                            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
                        }
                    } else {
                        NetworkHelper.INSTANCE.sendPacket(new ClientCommandC2SPacket(Wrapper.INSTANCE.getLocalPlayer(), ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                    }
                }
            }
            wasFlying = Wrapper.INSTANCE.getLocalPlayer().isFallFlying();
        }
    }
}