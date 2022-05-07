package me.dustin.jex.feature.command.core.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class MessageArgumentType implements ArgumentType<MessageArgumentType.MessageFormat> {
   private static final Collection<String> EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");

   public static MessageArgumentType message() {
      return new MessageArgumentType();
   }

   public static Component getMessage(CommandContext<FabricClientCommandSource> command, String name) throws CommandSyntaxException {
      return ((MessageFormat)command.getArgument(name, MessageFormat.class)).format((FabricClientCommandSource)command.getSource(), ((FabricClientCommandSource)command.getSource()).hasPermission(2));
   }

   public MessageFormat parse(StringReader stringReader) throws CommandSyntaxException {
      return MessageFormat.parse(stringReader, true);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static class MessageFormat {
      private final String contents;
      private final MessageSelector[] selectors;

      public MessageFormat(String contents, MessageSelector[] selectors) {
         this.contents = contents;
         this.selectors = selectors;
      }

      public String getContents() {
         return this.contents;
      }

      public Component format(FabricClientCommandSource source, boolean bl) throws CommandSyntaxException {
         return Component.nullToEmpty(this.contents);
      }

      public static MessageFormat parse(StringReader reader, boolean bl) throws CommandSyntaxException {
         String string = reader.getString().substring(reader.getCursor(), reader.getTotalLength());
         if (!bl) {
            reader.setCursor(reader.getTotalLength());
            return new MessageFormat(string, new MessageSelector[0]);
         } else {
            List<MessageSelector> list = Lists.newArrayList();
            int i = reader.getCursor();

            while(true) {
               int j;
               label38:
               while(true) {
                  while(reader.canRead()) {
                        reader.skip();
                  }

                  return new MessageFormat(string, (MessageSelector[])list.toArray(new MessageSelector[list.size()]));
               }
            }
         }
      }
   }

   public static class MessageSelector {
      private final int start;
      private final int end;
      private final EntitySelector selector;

      public MessageSelector(int start, int end, EntitySelector selector) {
         this.start = start;
         this.end = end;
         this.selector = selector;
      }

      public int getStart() {
         return this.start;
      }

      public int getEnd() {
         return this.end;
      }

      public EntitySelector getSelector() {
         return this.selector;
      }

      @Nullable
      public Component format(FabricClientCommandSource source) throws CommandSyntaxException {
         return Component.nullToEmpty("");//EntitySelector.getNames(this.selector.getEntities(source));
      }
   }
}
