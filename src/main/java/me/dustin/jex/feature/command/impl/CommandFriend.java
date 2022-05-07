package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.FriendArgumentType;
import me.dustin.jex.feature.command.core.arguments.PlayerNameArgumentType;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FriendFile;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.player.FriendHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

@Cmd(name = "friend", syntax = {".friend add <name> (optional)<alias>", ".friend del <name>", ".friend list"}, description = "Add or remove friends.")
public class CommandFriend extends Command {

    @Override
    public void registerCommand() {
        dispatcher.register(literal(this.name).then(literal("del").then(argument("name", FriendArgumentType.friend()).executes(context -> {
            String friendName = FriendArgumentType.getPlayerName(context, "name");
            FriendHelper.Friend friend = FriendHelper.INSTANCE.getFriendViaName(friendName);
            if (friend == null) {
                ChatHelper.INSTANCE.addClientMessage("\247c" + friendName + " is not your friend.");
                return 0;
            }
            FriendHelper.INSTANCE.removeFriend(friendName);
            ChatHelper.INSTANCE.addClientMessage("Removed \247c" + friendName);
            ConfigManager.INSTANCE.get(FriendFile.class).write();
            return 1;
        }))).then(literal("add").then(argument("name", PlayerNameArgumentType.playerName()).executes(context -> {
            String name = PlayerNameArgumentType.getPlayerName(context, "name");
            String alias = name;
            if (FriendHelper.INSTANCE.isFriend(name)) {
                ChatHelper.INSTANCE.addClientMessage(name + " is already a friend!");
                return 0;
            } else {
                FriendHelper.INSTANCE.addFriend(name, alias);
                ChatHelper.INSTANCE.addClientMessage("Added \247b" + name + " \2477as \247b" + alias);
                ConfigManager.INSTANCE.get(FriendFile.class).write();
            }
            return 1;
        }).then(argument("alias", PlayerNameArgumentType.playerName()).executes(context -> {
            String name = PlayerNameArgumentType.getPlayerName(context, "name");
            String alias = PlayerNameArgumentType.getPlayerName(context, "alias");
            if (FriendHelper.INSTANCE.isFriend(name)) {
                ChatHelper.INSTANCE.addClientMessage(name + " is already a friend!");
                return 0;
            } else {
                FriendHelper.INSTANCE.addFriend(name, alias);
                ChatHelper.INSTANCE.addClientMessage("Added \247b" + name + " \2477as \247b" + alias);
                ConfigManager.INSTANCE.get(FriendFile.class).write();
            }
            return 1;
        })))).then(literal("list").executes(context -> {
            ChatHelper.INSTANCE.addClientMessage("Displaying friends list");
            FriendHelper.INSTANCE.getFriendsList().forEach(friend -> {
                ChatHelper.INSTANCE.addClientMessage("\247b" + friend.name() + " \247f-> \247b" + friend.alias());
            });
            return 1;
        })));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        return 0;
    }
}
