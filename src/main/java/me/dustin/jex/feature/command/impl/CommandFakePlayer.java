package me.dustin.jex.feature.command.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import me.dustin.jex.JexClient;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.FakePlayerArgumentType;
import me.dustin.jex.feature.command.core.arguments.PlayerNameArgumentType;
import me.dustin.jex.helper.entity.FakePlayerEntity;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.MCAPIHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.entity.Entity;

import java.util.UUID;

@Cmd(name = "fakeplayer", description = "Create a fake player at your current position", syntax = {".fakeplayer add <name>", ".fakeplayer del <name>"}, alias = {"fp", "player"})
public class CommandFakePlayer extends Command {

    @Override
    public void registerCommand() {
        CommandNode<FabricClientCommandSource> node = dispatcher.register(literal(this.name).then(literal("add").then(argument("name", PlayerNameArgumentType.playerName()).executes(context -> {
            String name = PlayerNameArgumentType.getPlayerName(context, "name");
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
            return 1;
        }))).then(literal("del").then(argument("fake player", FakePlayerArgumentType.fakePlayer()).executes(context -> {
            String fakePlayerName = FakePlayerArgumentType.getPlayerName(context, "fake player");
            FakePlayerEntity player = null;
            for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
                if (entity instanceof FakePlayerEntity) {
                    if (entity.getName().getString().equalsIgnoreCase(fakePlayerName)) {
                        player = (FakePlayerEntity)entity;
                    }
                }
            }
            if (player == null) {
                ChatHelper.INSTANCE.addClientMessage("Could not find fake player with that name.");
                return 0;
            }
            player.setPos(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
            Wrapper.INSTANCE.getWorld().removeEntity(player.getId(), Entity.RemovalReason.DISCARDED);
            ChatHelper.INSTANCE.addClientMessage("Removed fake player " + name);
            return 1;
        }))));
        dispatcher.register(literal("fp").redirect(node));
        dispatcher.register(literal("player").redirect(node));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        return 0;
    }
}
