package me.dustin.jex.feature.command.impl;

import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.file.files.FriendFile;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.helper.misc.ChatHelper;

@Cmd(name = "Friend", syntax = {".friend add <name> (optional)<alias>", ".friend del <name>", ".friend list"}, description = "Add or remove friends.")
public class CommandFriend extends Command {

    @Override
    public void runCommand(String command, String[] args) {
        try {
            if (isAddString(args[1])) {
                String name = args[2];
                String alias = args[2];
                if (args.length > 3) {
                    alias = args[3];
                }
                if (FriendHelper.INSTANCE.isFriend(name)) {
                    ChatHelper.INSTANCE.addClientMessage(name + " is already a friend!");
                } else {
                    FriendHelper.INSTANCE.addFriend(name, alias);
                    ChatHelper.INSTANCE.addClientMessage("Added \247b" + name + " \2477as \247b" + alias);
                    FriendFile.write();
                }
            } else if (isDeleteString(args[1])) {
                String name = args[2];
                if (!FriendHelper.INSTANCE.isFriend(name)) {
                    ChatHelper.INSTANCE.addClientMessage(name + " is not your friend!");
                } else {
                    FriendHelper.INSTANCE.removeFriend(name);
                    ChatHelper.INSTANCE.addClientMessage("Removed \247c" + name);
                    FriendFile.write();
                }
            } else if (args[1].equalsIgnoreCase("list")) {
                ChatHelper.INSTANCE.addClientMessage("Displaying friends list");
                FriendHelper.INSTANCE.getFriendsList().forEach(friend -> {
                    ChatHelper.INSTANCE.addClientMessage("\247b" + friend.name() + " \247f-> \247b" + friend.alias());
                });
            }else {
                giveSyntaxMessage();
            }
        } catch (Exception e) {
            giveSyntaxMessage();
        }
    }
}
