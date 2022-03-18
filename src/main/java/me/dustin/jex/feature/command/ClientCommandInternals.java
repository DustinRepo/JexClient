package me.dustin.jex.feature.command;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.AmbiguityConsumer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.fabric.mixin.command.HelpCommandAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandException;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public final class ClientCommandInternals {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String API_COMMAND_NAME = "jex_command";
    private static final String SHORT_API_COMMAND_NAME = "assets/jex";

    /**
     * Executes a client-sided command from a message.
     *
     * @param message the command message
     * @return true if the message should not be sent to the server, false otherwise
     */
    public static boolean executeCommand(String message) {
        if (message.isEmpty()) {
            LOGGER.info("empty");
            return false; // Nothing to process
        }

        if (!message.startsWith(CommandManagerJex.INSTANCE.getPrefix())) {
            LOGGER.info("prefix wrong");
            return false; // Incorrect prefix, won't execute anything.
        }

        MinecraftClient client = MinecraftClient.getInstance();

        // noinspection ConstantConditions
        FabricClientCommandSource commandSource = (FabricClientCommandSource) client.getNetworkHandler().getCommandSource();

        client.getProfiler().push(message);

        try {
            CommandManagerJex.DISPATCHER.execute(message.substring(CommandManagerJex.INSTANCE.getPrefix().length()), commandSource);
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

        return context != null ? new TranslatableText("command.context.parse_error", message, context) : message;
    }

    /**
     * Runs final initialization tasks such as {@link CommandDispatcher#findAmbiguities(AmbiguityConsumer)}
     * on the command dispatcher. Also registers a {@code /fcc help} command if there are other commands present.
     */
    public static void finalizeInit() {
        if (!CommandManagerJex.DISPATCHER.getRoot().getChildren().isEmpty()) {
            // Register an API command if there are other commands;
            // these helpers are not needed if there are no client commands
            LiteralArgumentBuilder<FabricClientCommandSource> help = literal("help");
            help.executes(ClientCommandInternals::executeRootHelp);
            help.then(argument("command", StringArgumentType.greedyString()).executes(ClientCommandInternals::executeArgumentHelp));

            CommandNode<FabricClientCommandSource> mainNode = CommandManagerJex.DISPATCHER.register(literal(API_COMMAND_NAME).then(help));
            CommandManagerJex.DISPATCHER.register(literal(SHORT_API_COMMAND_NAME).redirect(mainNode));
        }

        // noinspection CodeBlock2Expr
        CommandManagerJex.DISPATCHER.findAmbiguities((parent, child, sibling, inputs) -> {
            LOGGER.warn("Ambiguity between arguments {} and {} with inputs: {}", CommandManagerJex.DISPATCHER.getPath(child), CommandManagerJex.DISPATCHER.getPath(sibling), inputs);
        });
    }

    private static int executeRootHelp(CommandContext<FabricClientCommandSource> context) {
        return executeHelp(CommandManagerJex.DISPATCHER.getRoot(), context);
    }

    private static int executeArgumentHelp(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        ParseResults<FabricClientCommandSource> parseResults = CommandManagerJex.DISPATCHER.parse(StringArgumentType.getString(context, "command"), context.getSource());
        List<ParsedCommandNode<FabricClientCommandSource>> nodes = parseResults.getContext().getNodes();

        if (nodes.isEmpty()) {
            throw HelpCommandAccessor.getFailedException().create();
        }

        return executeHelp(Iterables.getLast(nodes).getNode(), context);
    }

    private static int executeHelp(CommandNode<FabricClientCommandSource> startNode, CommandContext<FabricClientCommandSource> context) {
        Map<CommandNode<FabricClientCommandSource>, String> commands = CommandManagerJex.DISPATCHER.getSmartUsage(startNode, context.getSource());

        for (String command : commands.values()) {
            context.getSource().sendFeedback(new LiteralText("/" + command));
        }

        return commands.size();
    }

    public static void addCommands(CommandDispatcher<FabricClientCommandSource> target, FabricClientCommandSource source) {
        Map<CommandNode<FabricClientCommandSource>, CommandNode<FabricClientCommandSource>> originalToCopy = new HashMap<>();
        originalToCopy.put(CommandManagerJex.DISPATCHER.getRoot(), target.getRoot());
        copyChildren(CommandManagerJex.DISPATCHER.getRoot(), target.getRoot(), source, originalToCopy);
    }

    /**
     * Copies the child commands from origin to target, filtered by {@code child.canUse(source)}.
     * Mimics vanilla's CommandManager.makeTreeForSource.
     *
     * @param origin         the source command node
     * @param target         the target command node
     * @param source         the command source
     * @param originalToCopy a mutable map from original command nodes to their copies, used for redirects;
     *                       should contain a mapping from origin to target
     */
    private static void copyChildren(
            CommandNode<FabricClientCommandSource> origin,
            CommandNode<FabricClientCommandSource> target,
            FabricClientCommandSource source,
            Map<CommandNode<FabricClientCommandSource>, CommandNode<FabricClientCommandSource>> originalToCopy
    ) {
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
