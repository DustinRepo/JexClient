package me.dustin.jex.command.impl;

import me.dustin.jex.command.core.Command;
import me.dustin.jex.command.core.annotate.Cmd;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.impl.misc.SpeedCrafter;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.item.Items;

@Cmd(name = "SpeedCraft", alias = {"sc", "craft"}, syntax = ".speedcraft set/clear", description = "Change the item used for SpeedCrafter mod (hold output item in hand)")
public class CommandSpeedCraft extends Command {
    @Override
    public void runCommand(String command, String[] args) {
        try {
            if (args[1].equalsIgnoreCase("set")) {
                SpeedCrafter speedCrafter = (SpeedCrafter) Feature.get(SpeedCrafter.class);
                if (Wrapper.INSTANCE.getLocalPlayer().getMainHandStack().getItem() == Items.AIR) {
                    ChatHelper.INSTANCE.addClientMessage("No item in hand");
                } else {
                    speedCrafter.craftingItem = Wrapper.INSTANCE.getLocalPlayer().getMainHandStack().getItem();
                }
                ChatHelper.INSTANCE.addClientMessage("SpeedCrafter item set to \247b" + speedCrafter.craftingItem.getName().getString());
            } else if (args[1].equalsIgnoreCase("clear")) {
                SpeedCrafter speedCrafter = (SpeedCrafter) Feature.get(SpeedCrafter.class);
                speedCrafter.craftingItem = null;
                ChatHelper.INSTANCE.addClientMessage("SpeedCrafter item set to \247bnull");
            }
        } catch (Exception e) {
            giveSyntaxMessage();
        }
    }
}
