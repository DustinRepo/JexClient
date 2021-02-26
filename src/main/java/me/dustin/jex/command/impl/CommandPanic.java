package me.dustin.jex.command.impl;


import me.dustin.jex.command.core.Command;
import me.dustin.jex.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.enums.ModCategory;

@Cmd(name = "Panic", syntax = ".panic", description = "Disables all mods that are not visual incase an admin is nearby.")
public class CommandPanic extends Command {

    @Override
    public void runCommand(String command, String[] args) {
        for (ModCategory category : ModCategory.values()) {
            if (category != ModCategory.VISUAL) {
                for (Module module : Module.getModules(category)) {
                    if (module.getState())
                        module.setState(false);
                }
            }
        }
        ChatHelper.INSTANCE.addClientMessage("All non-visual mods disabled.");
    }
}
