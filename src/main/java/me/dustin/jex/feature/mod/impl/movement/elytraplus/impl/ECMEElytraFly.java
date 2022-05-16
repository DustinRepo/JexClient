package me.dustin.jex.feature.mod.impl.movement.elytraplus.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.elytraplus.ElytraPlus;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.util.Mth;

public class ECMEElytraFly extends FeatureExtension {
    private ElytraPlus elytraPlus;
    private boolean wasFlying;
    public ECMEElytraFly() {
        super("ECME", ElytraPlus.class);
    }

    @Override
    public void pass(Event event) {
        if (elytraPlus == null)
            elytraPlus = Feature.get(ElytraPlus.class);
        if (event instanceof EventMove eventMove) {
            if (Wrapper.INSTANCE.getLocalPlayer().isFallFlying()) {
                if (!wasFlying) {
                    for (int i = 0; i < 10; i++) {
                        NetworkHelper.INSTANCE.sendPacket(new ServerboundMovePlayerPacket.Pos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
                        NetworkHelper.INSTANCE.sendPacket(new ServerboundMovePlayerPacket.Pos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() - 0.05, Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
                    }
                }
                float radianYaw = (float) Math.toRadians(Wrapper.INSTANCE.getLocalPlayer().getYRot());
                double currentVel = Math.abs(Wrapper.INSTANCE.getLocalPlayer().getDeltaMovement().x) + Math.abs(Wrapper.INSTANCE.getLocalPlayer().getDeltaMovement().y) + Math.abs(Wrapper.INSTANCE.getLocalPlayer().getDeltaMovement().z);
                if (Wrapper.INSTANCE.getOptions().keyUp.isDown()) {
                    if (currentVel <= 2f)
                        Wrapper.INSTANCE.getLocalPlayer().push(Mth.sin(radianYaw) * -0.05, 0, Mth.cos(radianYaw) * 0.05);
                } else {
                    eventMove.setX(0);
                    eventMove.setZ(0);
                }
                if (eventMove.getY() <= 0)
                    eventMove.setY(Wrapper.INSTANCE.getOptions().keyShift.isDown() ? -0.75 : -0.0001);
            }
            if (Wrapper.INSTANCE.getOptions().keyJump.isDown()) {
                if (wasFlying) {
                    if (Wrapper.INSTANCE.getLocalPlayer().isFallFlying()) {
                        if (Wrapper.INSTANCE.getLocalPlayer().tickCount % 5 == 0) {
                            Wrapper.INSTANCE.getLocalPlayer().setPos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() + 5, Wrapper.INSTANCE.getLocalPlayer().getZ());
                            NetworkHelper.INSTANCE.sendPacket(new ServerboundMovePlayerPacket.Pos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
                        }
                    } else {
                        NetworkHelper.INSTANCE.sendPacket(new ServerboundPlayerCommandPacket(Wrapper.INSTANCE.getLocalPlayer(), ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
                    }
                }
            }
            wasFlying = Wrapper.INSTANCE.getLocalPlayer().isFallFlying();
        }
    }
}