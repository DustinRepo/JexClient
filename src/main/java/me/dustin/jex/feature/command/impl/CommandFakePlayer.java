package me.dustin.jex.feature.command.impl;

import com.mojang.authlib.GameProfile;
import me.dustin.jex.JexClient;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.entity.FakePlayerEntity;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.MCAPIHelper;
import net.minecraft.entity.Entity;

import java.util.UUID;

@Cmd(name = "FakePlayer", description = "Create a fake player at your current position", syntax = {".fakeplayer add <name>", ".fakeplayer del <name>"}, alias = {"fp", "player"})
public class CommandFakePlayer extends Command {
    @Override
    public void runCommand(String command, String[] args) {
        try {
            String syn = args[1];
            String name = args[2];
            if (isAddString(syn)) {
                int id = 69420 + ClientMathHelper.INSTANCE.getRandom(200);
                UUID uuid = UUID.randomUUID();
                UUID realUUID = MCAPIHelper.INSTANCE.getUUIDFromName(name);
                if (realUUID != null) {
                    JexClient.INSTANCE.getLogger().info("Grabbed UUID from Minecraft");
                    uuid = realUUID;
                }
                FakePlayerEntity otherClientPlayerEntity = new FakePlayerEntity(Wrapper.INSTANCE.getWorld(), new GameProfile(uuid, name));
                Wrapper.INSTANCE.getWorld().addEntity(id, otherClientPlayerEntity);
                otherClientPlayerEntity.copyPositionAndRotation(Wrapper.INSTANCE.getLocalPlayer());
                ChatHelper.INSTANCE.addClientMessage("Added fake player " + name);
            } else if (isDeleteString(syn)) {
                FakePlayerEntity player = null;
                for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
                    if (entity instanceof FakePlayerEntity) {
                        JexClient.INSTANCE.getLogger().info("Found fake player with name " + entity.getName().getString());
                        if (entity.getName().getString().equalsIgnoreCase(name)) {
                            player = (FakePlayerEntity)entity;
                        }
                    }
                }
                if (player == null) {
                    ChatHelper.INSTANCE.addClientMessage("Could not find fake player with that name.");
                    return;
                }
                player.setPos(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
                Wrapper.INSTANCE.getWorld().removeEntity(player.getId(), Entity.RemovalReason.DISCARDED);
                ChatHelper.INSTANCE.addClientMessage("Removed fake player " + name);
            } else {
                giveSyntaxMessage();
            }
        }catch (Exception e) {
            giveSyntaxMessage();
        }
    }
}
