package me.dustin.jex.feature.command.impl;

import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

@Cmd(name = "Damage", description = "Cause yourself to take damage", syntax = ".damage <amount> (max 7)", alias = {"dmg"})
public class CommandDamage extends Command {
    @Override
    public void runCommand(String command, String[] args) {
        try {
            int damage = Integer.parseInt(args[1]);
            if (damage > 7) {
                ChatHelper.INSTANCE.addClientMessage("Maximum damage is 7");
                return;
            } else if (damage < 1) {
                ChatHelper.INSTANCE.addClientMessage("Minimum damage is 1");
                return;
            }
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() + 2.1 + damage, Wrapper.INSTANCE.getLocalPlayer().getZ(), true));
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), true));
            ChatHelper.INSTANCE.addClientMessage("Dealing \247b" + damage + " \2477damage.");
        } catch (Exception e) {
            giveSyntaxMessage();
        }
    }
}
