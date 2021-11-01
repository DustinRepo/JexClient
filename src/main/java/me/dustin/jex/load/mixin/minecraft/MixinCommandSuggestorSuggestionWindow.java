package me.dustin.jex.load.mixin.minecraft;

import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.client.gui.screen.CommandSuggestor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CommandSuggestor.SuggestionWindow.class)
public class MixinCommandSuggestorSuggestionWindow {

    @Shadow @Final private List<Suggestion> suggestions;

    @Shadow private int selection;

    @Inject(method = "select", at = @At("HEAD"), cancellable = true)
    public void crashFix(int index, CallbackInfo ci) {
        try {
            if (index < 0) {
                index += this.suggestions.size();
            }

            if (index >= this.suggestions.size()) {
                index -= this.suggestions.size();
            }
            this.suggestions.get(index);
        } catch (IndexOutOfBoundsException e) {
            ci.cancel();
        }
    }

    @Inject(method = "complete", at = @At("HEAD"), cancellable = true)
    public void crashFix1(CallbackInfo ci) {
        try {
            this.suggestions.get(this.selection);
        } catch (IndexOutOfBoundsException e) {
            ci.cancel();
        }
    }

}
