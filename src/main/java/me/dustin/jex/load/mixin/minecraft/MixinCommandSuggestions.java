package me.dustin.jex.load.mixin.minecraft;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import me.dustin.jex.feature.command.CommandManagerJex;
import me.dustin.jex.load.impl.ICommandSuggestions;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(CommandSuggestions.class)
public abstract class MixinCommandSuggestions implements ICommandSuggestions {

    @Shadow @Final private static Pattern WHITESPACE_PATTERN;

    @Shadow @Final private EditBox input;

    @Shadow @Nullable private CommandSuggestions.SuggestionsList suggestions;

    @Redirect(method = "updateCommandInfo", at = @At(value = "INVOKE", target = "com/mojang/brigadier/StringReader.peek()C"))
    public char refresh(StringReader stringReader) {
        String string = this.input.getValue();
        boolean bl = string.startsWith(CommandManagerJex.INSTANCE.getPrefix());
        if (bl) {
            return '/';
        }
        return stringReader.peek();
    }

    @Inject(method = "sortSuggestions", at = @At("HEAD"), cancellable = true)
    public void sort(Suggestions suggestions, CallbackInfoReturnable<List<Suggestion>> cir) {
        String s = this.input.getValue();
        boolean bl = s.startsWith(CommandManagerJex.INSTANCE.getPrefix());
        String string = this.input.getValue().substring(0, this.input.getCursorPosition());
        int i = getLastPlayerNameStart(string);
        String string2 = string.substring(i).toLowerCase(Locale.ROOT);
        ArrayList<Suggestion> list = Lists.newArrayList();
        ArrayList<Suggestion> list2 = Lists.newArrayList();
        for (Suggestion suggestion : suggestions.getList()) {
            if (bl) {
                if (!CommandManagerJex.INSTANCE.isJexCommand(suggestion.getText()) && !this.input.getValue().contains(" "))
                    continue;
            } else if (CommandManagerJex.INSTANCE.isJexCommand(suggestion.getText()))
                continue;
            if (suggestion.getText().startsWith(string2) || (!bl && suggestion.getText().startsWith("minecraft:" + string2))) {
                list.add(suggestion);
                continue;
            }
            list2.add(suggestion);
        }
        list.addAll(list2);
        cir.setReturnValue(list);
    }

    private static int getLastPlayerNameStart(String input) {
        if (Strings.isNullOrEmpty(input)) {
            return 0;
        }
        int i = 0;
        Matcher matcher = WHITESPACE_PATTERN.matcher(input);
        while (matcher.find()) {
            i = matcher.end();
        }
        return i;
    }

    @Override
    public boolean isWindowActive() {
        return this.suggestions != null;
    }

    public CommandSuggestions getThis() {
        return (CommandSuggestions)(Object)this;
    }
}
