package me.dustin.jex.feature.command;


import java.util.HashMap;
import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandException;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

@Environment(EnvType.CLIENT)
public final class ClientCommandInternals {
    private static final Logger LOGGER = LogManager.getLogger();

    public static boolean executeCommand(String message) {
        if (message.isEmpty()) {
            LOGGER.info("empty");
            return false; // Nothing to process
        }

        if (!message.startsWith(CommandManager.INSTANCE.getPrefix())) {
            LOGGER.info("prefix wrong");
            return false; // Incorrect prefix, won't execute anything.
        }

        MinecraftClient client = MinecraftClient.getInstance();

        // noinspection ConstantConditions
        FabricClientCommandSource commandSource = (FabricClientCommandSource) client.getNetworkHandler().getCommandSource();

        client.getProfiler().push(message);

        try {
            CommandManager.DISPATCHER.execute(message.substring(CommandManager.INSTANCE.getPrefix().length()), commandSource);
            return true;
        } catch (CommandSyntaxException e) {
            LOGGER.log(Level.WARN, "Syntax exception for client-sided command '{}'", message, e);

            commandSource.sendError(getErrorMessage(e));
            return true;
        } catch (CommandException e) {
            LOGGER.warn("Error while executing client-sided command '{}'", message, e);
            commandSource.sendError(e.getTextMessage());
            return true;
        } catch (RuntimeException e) {
            LOGGER.warn("Error while executing client-sided command '{}'", message, e);
            commandSource.sendError(Text.of(e.getMessage()));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        } finally {
            client.getProfiler().pop();
        }
    }

    // See CommandSuggestor.method_30505. That cannot be used directly as it returns an OrderedText instead of a Text.
    private static Text getErrorMessage(CommandSyntaxException e) {
        Text message = Texts.toText(e.getRawMessage());
        String context = e.getContext();

        return context != null ? Text.translatable("command.context.parse_error", message, context) : message;
    }

    public static void addCommands(CommandDispatcher<FabricClientCommandSource> target, FabricClientCommandSource source) {
        Map<CommandNode<FabricClientCommandSource>, CommandNode<FabricClientCommandSource>> originalToCopy = new HashMap<>();
        originalToCopy.put(CommandManager.DISPATCHER.getRoot(), target.getRoot());
        copyChildren(CommandManager.DISPATCHER.getRoot(), target.getRoot(), source, originalToCopy);
    }

    private static void copyChildren(CommandNode<FabricClientCommandSource> origin, CommandNode<FabricClientCommandSource> target, FabricClientCommandSource source, Map<CommandNode<FabricClientCommandSource>, CommandNode<FabricClientCommandSource>> originalToCopy) {
        for (CommandNode<FabricClientCommandSource> child : origin.getChildren()) {
            if (!child.canUse(source)) continue;

            ArgumentBuilder<FabricClientCommandSource, ?> builder = child.createBuilder();

            // Reset the unnecessary non-completion stuff from the builder
            builder.requires(s -> true); // This is checked with the if check above.

            if (builder.getCommand() != null) {
                builder.executes(context -> 0);
            }

            // Set up redirects
            if (builder.getRedirect() != null) {
                builder.redirect(originalToCopy.get(builder.getRedirect()));
            }

            CommandNode<FabricClientCommandSource> result = builder.build();
            originalToCopy.put(child, result);
            target.addChild(result);

            if (!child.getChildren().isEmpty()) {
                copyChildren(child, result, source, originalToCopy);
            }
        }
    }
}
