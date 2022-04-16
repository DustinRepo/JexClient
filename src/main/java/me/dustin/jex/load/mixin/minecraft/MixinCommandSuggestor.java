package me.dustin.jex.load.mixin.minecraft;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import me.dustin.jex.feature.command.CommandManagerJex;
import me.dustin.jex.load.impl.ICommandSuggestor;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(CommandSuggestor.class)
public abstract class MixinCommandSuggestor implements ICommandSuggestor {

    @Shadow @Final private TextFieldWidget textField;

    @Shadow @Nullable private CommandSuggestor.SuggestionWindow window;

    @Shadow @Final private static Pattern WHITESPACE_PATTERN;

    @Redirect(method = "refresh", at = @At(value = "INVOKE", target = "com/mojang/brigadier/StringReader.peek()C"))
    public char refresh(StringReader stringReader) {
        if (getThis() == CommandManagerJex.INSTANCE.jexCommandSuggestor) {
            String string = this.textField.getText();
            boolean bl = string.startsWith(CommandManagerJex.INSTANCE.getPrefix());
            if (bl) {
                return '/';
            }
        }
        return stringReader.peek();
    }

    @Inject(method = "sortSuggestions", at = @At("HEAD"), cancellable = true)
    public void sort(Suggestions suggestions, CallbackInfoReturnable<List<Suggestion>> cir) {
        if (getThis() == CommandManagerJex.INSTANCE.jexCommandSuggestor) {
            String string = this.textField.getText().substring(0, this.textField.getCursor());
            int i = getLastPlayerNameStart(string);
            String string2 = string.substring(i).toLowerCase(Locale.ROOT);
            List<Suggestion> list = Lists.newArrayList();
            List<Suggestion> list2 = Lists.newArrayList();
            Iterator var7 = suggestions.getList().iterator();

            while(true) {
                while(var7.hasNext()) {
                    Suggestion suggestion = (Suggestion)var7.next();
                    if (!CommandManagerJex.INSTANCE.isJexCommand(suggestion.getText()) && !this.textField.getText().contains(" ")){
                        continue;
                    }

                    if (!suggestion.getText().startsWith(string2) && !suggestion.getText().startsWith("minecraft:" + string2)) {
                    list2.add(suggestion);
                    } else {
                        list.add(suggestion);
                    }
                }

                list.addAll(list2);
                cir.setReturnValue(list);
                break;
            }
        } else {
            String string = this.textField.getText().substring(0, this.textField.getCursor());
            int i = getLastPlayerNameStart(string);
            String string2 = string.substring(i).toLowerCase(Locale.ROOT);
            List<Suggestion> list = Lists.newArrayList();
            List<Suggestion> list2 = Lists.newArrayList();
            Iterator var7 = suggestions.getList().iterator();

            while(true) {
                while (var7.hasNext()) {
                    Suggestion suggestion = (Suggestion) var7.next();
                    if (CommandManagerJex.INSTANCE.isJexCommand(suggestion.getText()) && !this.textField.getText().contains(" ")) {
                        continue;
                    }

                    if (!suggestion.getText().startsWith(string2) && !suggestion.getText().startsWith("minecraft:" + string2)) {
                        list2.add(suggestion);
                    } else {
                        list.add(suggestion);
                    }
                }

                list.addAll(list2);
                cir.setReturnValue(list);
                break;
            }
        }
    }

    private static int getLastPlayerNameStart(String input) {
        if (Strings.isNullOrEmpty(input)) {
            return 0;
        } else {
            int i = 0;

            for(Matcher matcher = WHITESPACE_PATTERN.matcher(input); matcher.find(); i = matcher.end()) {
            }

            return i;
        }
    }

    @Override
    public boolean isWindowActive() {
        return this.window != null;
    }

    public CommandSuggestor getThis() {
        return (CommandSuggestor)(Object)this;
    }
}
