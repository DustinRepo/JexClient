package me.dustin.jex.command.impl;

import me.dustin.jex.command.core.Command;
import me.dustin.jex.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;

@Cmd(name = "VClip", syntax = ".vclip <amount>", description = "Instantly teleport vertically.", alias = {"up", "clip"})
public class CommandVClip extends Command {

    @Override
    public void runCommand(String command, String[] args) {
        try {
            if (args[1].equalsIgnoreCase("ncp")) {
                NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionOnly(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() - 0.0624, Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
                NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionOnly(Wrapper.INSTANCE.getLocalPlayer().getX(), -1337.0, Wrapper.INSTANCE.getLocalPlayer().getZ(), true));
            } else {
                float num = Float.parseFloat(args[1]);
                NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionOnly(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() + num, Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
                NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionOnly(Wrapper.INSTANCE.getLocalPlayer().getX(), -1337.0, Wrapper.INSTANCE.getLocalPlayer().getZ(), true));
                if (Wrapper.INSTANCE.getLocalPlayer().isRiding()) {
                    Wrapper.INSTANCE.getLocalPlayer().getVehicle().setPos(Wrapper.INSTANCE.getLocalPlayer().getVehicle().getX(), Wrapper.INSTANCE.getLocalPlayer().getVehicle().getY() + num, Wrapper.INSTANCE.getLocalPlayer().getVehicle().getZ());
                    NetworkHelper.INSTANCE.sendPacket(new VehicleMoveC2SPacket(Wrapper.INSTANCE.getLocalPlayer().getVehicle()));
                }
            }
            ChatHelper.INSTANCE.addClientMessage("VClip done");
        } catch (Exception e) {
            giveSyntaxMessage();
        }
    }
}
