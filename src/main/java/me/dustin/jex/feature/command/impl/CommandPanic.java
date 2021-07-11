package me.dustin.jex.feature.command.impl;


import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.feature.mod.core.Feature;

@Cmd(name = "Panic", description = "Disables all mods that are not visual incase an admin is nearby.")
public class CommandPanic extends Command {

    @Override
    public void runCommand(String command, String[] args) {
        for (Feature.Category category : Feature.Category.values()) {
            if (category != Feature.Category.VISUAL) {
                for (Feature feature : Feature.getModules(category)) {
                    if (feature.getState())
                        feature.setState(false);
                }
            }
        }
        ChatHelper.INSTANCE.addClientMessage("All non-visual mods disabled.");
    }
}
