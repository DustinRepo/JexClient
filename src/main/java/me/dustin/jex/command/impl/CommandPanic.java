package me.dustin.jex.command.impl;


import me.dustin.jex.command.core.Command;
import me.dustin.jex.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.enums.FeatureCategory;

@Cmd(name = "Panic", syntax = ".panic", description = "Disables all mods that are not visual incase an admin is nearby.")
public class CommandPanic extends Command {

    @Override
    public void runCommand(String command, String[] args) {
        for (FeatureCategory category : FeatureCategory.values()) {
            if (category != FeatureCategory.VISUAL) {
                for (Feature feature : Feature.getModules(category)) {
                    if (feature.getState())
                        feature.setState(false);
                }
            }
        }
        ChatHelper.INSTANCE.addClientMessage("All non-visual mods disabled.");
    }
}
