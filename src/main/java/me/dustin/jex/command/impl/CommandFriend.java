package me.dustin.jex.command.impl;

import me.dustin.jex.command.core.Command;
import me.dustin.jex.command.core.annotate.Cmd;
import me.dustin.jex.file.FriendFile;
import me.dustin.jex.friend.Friend;
import me.dustin.jex.helper.misc.ChatHelper;

@Cmd(name = "Friend", syntax = ".friend <add/del> <name> (optional)<alias>", description = "Add or remove friends.")
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
                if (Friend.isFriend(name)) {
                    ChatHelper.INSTANCE.addClientMessage(name + " is already a friend!");
                } else {
                    Friend.addFriend(name, alias);
                    ChatHelper.INSTANCE.addClientMessage("Added \247b" + name + " \2477as \247b" + alias);
                    FriendFile.write();
                }
            } else if (isDeleteString(args[1])) {
                String name = args[2];
                if (!Friend.isFriend(name)) {
                    ChatHelper.INSTANCE.addClientMessage(name + " is not your friend!");
                } else {
                    Friend.removeFriend(name);
                    ChatHelper.INSTANCE.addClientMessage("Removed \247c" + name);
                    FriendFile.write();
                }
            } else {
                giveSyntaxMessage();
            }
        } catch (Exception e) {
            giveSyntaxMessage();
        }
    }
}
