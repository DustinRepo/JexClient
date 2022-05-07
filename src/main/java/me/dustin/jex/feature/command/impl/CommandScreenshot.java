package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.WebHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.Screenshot;
import java.io.File;

@Cmd(name = "screenshot", description = "Take a screenshot and upload to imgur", alias = "ss")
public class CommandScreenshot extends Command {
    @Override
    public void registerCommand() {
        dispatcher.register(literal(this.name).executes(this));
        dispatcher.register(literal("ss").executes(this));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        File screenshotFolder = new File(Wrapper.INSTANCE.getMinecraft().gameDirectory, "screenshots");
        Screenshot.grab(Wrapper.INSTANCE.getMinecraft().gameDirectory, "jex_screenshot.png", Wrapper.INSTANCE.getMinecraft().getMainRenderTarget(), text -> {
            WebHelper.INSTANCE.uploadToImgur(new File(screenshotFolder, "jex_screenshot.png"));
        });
        return 1;
    }
}
