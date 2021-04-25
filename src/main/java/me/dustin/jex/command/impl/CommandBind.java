package me.dustin.jex.command.impl;

import me.dustin.jex.command.core.Command;
import me.dustin.jex.command.core.annotate.Cmd;
import me.dustin.jex.file.FeatureFile;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.FeatureManager;

@Cmd(name = "Bind", syntax = {".bind add <module> <key>", ".bind remove <module>", ".bind list"}, description = "Modify keybinds with a command. List with bind list")
public class CommandBind extends Command {

    @Override
    public void runCommand(String command, String[] args) {
        try {
            String action = args[1];
            if (action.equalsIgnoreCase("list")) {
                ChatHelper.INSTANCE.addClientMessage("Listing keybinds.");
                for (Feature feature : FeatureManager.INSTANCE.getFeatures()) {
                    if (feature.getKey() != 0) {
                        ChatHelper.INSTANCE.addClientMessage("\247b" + feature.getName() + "\247f: \2477" + KeyboardHelper.INSTANCE.getKeyName(feature.getKey()));
                    }
                }
                return;
            } else {
                String moduleName = args[2];
                Feature feature = Feature.get(moduleName);
                if (feature == null) {
                    ChatHelper.INSTANCE.addClientMessage("Module not found.");
                    return;
                }
                if (isAddString(action)) {
                    String keyName = args[3];
                    int key = KeyboardHelper.INSTANCE.getKeyFromName(keyName);
                    if (key == -1) {
                        ChatHelper.INSTANCE.addClientMessage("Key not found.");
                        return;
                    }
                    feature.setKey(key);
                    ChatHelper.INSTANCE.addClientMessage("\247b" + feature.getName() + " \2477has been bound to \247b" + keyName);
                    FeatureFile.write();
                } else if (isDeleteString(action)) {
                    feature.setKey(0);
                    ChatHelper.INSTANCE.addClientMessage("\247b" + feature.getName() + " \2477has been unbound");
                    FeatureFile.write();
                } else {
                    giveSyntaxMessage();
                }
            }
        }catch (Exception e) {
            giveSyntaxMessage();
        }
    }
}
