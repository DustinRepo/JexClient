package me.dustin.jex.load.mixin.minecraft;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import me.dustin.jex.event.chat.EventSortSuggestions;
import me.dustin.jex.feature.command.CommandManager;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CommandSuggestor.class)
public abstract class MixinCommandSuggestor implements ICommandSuggestor {
    @Shadow @Final private TextFieldWidget textField;
    @Shadow @Nullable private CommandSuggestor.SuggestionWindow window;

    @Redirect(method = "refresh", at = @At(value = "INVOKE", target = "com/mojang/brigadier/StringReader.peek()C"))
    public char refresh(StringReader stringReader) {
        String string = this.textField.getText();
        boolean isJex = string.startsWith(CommandManager.INSTANCE.getPrefix());
        if (isJex) {
            //tell the suggestor it actually starts with a '/' so it activates
            return '/';
        }
        return stringReader.peek();
    }

    @Inject(method = "sortSuggestions", at = @At("RETURN"), cancellable = true)
    public void sort(Suggestions suggestions, CallbackInfoReturnable<List<Suggestion>> cir) {
        EventSortSuggestions eventSortSuggestions = new EventSortSuggestions(suggestions, this.textField.getText(), cir.getReturnValue()).run();
        cir.setReturnValue(eventSortSuggestions.getOutput());
    }

    @Override
    public boolean isWindowActive() {
        return this.window != null;
    }
}
