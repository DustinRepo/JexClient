package me.dustin.jex.load.mixin.minecraft;

import com.mojang.brigadier.suggestion.Suggestion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.client.gui.components.CommandSuggestions;

@Mixin(CommandSuggestions.SuggestionsList.class)
public class MixinCommandSuggestionsSuggestionsList {

    @Shadow @Final private List<Suggestion> suggestionList;

    @Shadow private int current;

    @Inject(method = "select", at = @At("HEAD"), cancellable = true)
    public void crashFix(int index, CallbackInfo ci) {
        try {
            if (index < 0) {
                index += this.suggestionList.size();
            }

            if (index >= this.suggestionList.size()) {
                index -= this.suggestionList.size();
            }
            this.suggestionList.get(index);
        } catch (IndexOutOfBoundsException e) {
            ci.cancel();
        }
    }

    @Inject(method = "useSuggestion", at = @At("HEAD"), cancellable = true)
    public void crashFix1(CallbackInfo ci) {
        try {
            this.suggestionList.get(this.current);
        } catch (IndexOutOfBoundsException e) {
            ci.cancel();
        }
    }

}
