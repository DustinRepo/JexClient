package me.dustin.jex.load.mixin.minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.feature.command.CommandManagerJex;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.misc.IRC;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.load.impl.IChatScreen;
import me.dustin.jex.load.impl.ICommandSuggestions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class MixinChatScreen implements IChatScreen {

    @Shadow protected EditBox input;
    @Shadow @Final private String initial;
    @Shadow private CommandSuggestions commandSuggestions;
    private Button normalChatButton;
    private Button ircChatButton;
    private IRC ircMod;

    @Inject(method = "init", at = @At("RETURN"))
    public void init(CallbackInfo ci) {
        ircMod = Feature.get(IRC.class);
        normalChatButton = new Button(input.x - 2, input.y - 22, 40, 18, Component.nullToEmpty(ircMod.ircChatOverride ? "\2477Chat": "\247bChat"), button -> {
            ircChatButton.setMessage(Component.nullToEmpty("\2477IRC"));
            normalChatButton.setMessage(Component.nullToEmpty("\247bChat"));
            ircMod.ircChatOverride = false;
        });
        ircChatButton = new Button(input.x - 2 + 42, input.y - 22, 40, 18, Component.nullToEmpty(ircMod.ircChatOverride ? "\247cIRC" : "\2477IRC"), button -> {
            normalChatButton.setMessage(Component.nullToEmpty("\2477Chat"));
            ircChatButton.setMessage(Component.nullToEmpty("\247cIRC"));
            ircMod.ircChatOverride = true;
        });
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "net/minecraft/client/gui/components/CommandSuggestions.render (Lcom/mojang/blaze3d/vertex/PoseStack;II)V"))
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ICommandSuggestions mc = (ICommandSuggestions) this.commandSuggestions;
        ircMod.renderAboveChat = !mc.isWindowActive();

        if (ircMod.renderAboveChat && ircMod.ircClient != null && ircMod.ircClient.isConnected()) {
            normalChatButton.render(matrices, mouseX, mouseY, delta);
            ircChatButton.render(matrices, mouseX, mouseY, delta);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (ircMod.getState() && ircMod.renderAboveChat && ircMod.ircClient != null && ircMod.ircClient.isConnected()) {
            normalChatButton.mouseClicked(mouseX, mouseY, button);
            ircChatButton.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public String getText() {
        return this.input.getValue();
    }

    @Override
    public void setText(String text) {
        this.input.setValue(text);
    }

    @Override
    public EditBox getWidget() {
        return this.input;
    }

}
