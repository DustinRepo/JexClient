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
        CommandManagerJex.INSTANCE.jexCommandSuggestor = new CommandSuggestions(Wrapper.INSTANCE.getMinecraft(), (ChatScreen)(Object)this, this.input, Wrapper.INSTANCE.getTextRenderer(), false, true, 1, 10, true, -805306368);
        CommandManagerJex.INSTANCE.jexCommandSuggestor.updateCommandInfo();
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

    @Inject(method = "onEdited", at = @At("RETURN"))
    public void onChatFieldUpdate(String chatText, CallbackInfo ci) {
        if (this.input == null || CommandManagerJex.INSTANCE.jexCommandSuggestor == null) return;
        String string = this.input.getValue();
        CommandManagerJex.INSTANCE.jexCommandSuggestor.setAllowSuggestions(!string.equals(this.initial));
        CommandManagerJex.INSTANCE.jexCommandSuggestor.updateCommandInfo();
    }

    @Inject(method = "moveInHistory", at = @At(value = "INVOKE", target = "net/minecraft/client/gui/components/CommandSuggestions.setAllowSuggestions (Z)V"))
    public void setChat(int offset, CallbackInfo ci) {
        if (CommandManagerJex.INSTANCE.jexCommandSuggestor != null)
            CommandManagerJex.INSTANCE.jexCommandSuggestor.setAllowSuggestions(false);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "net/minecraft/client/gui/components/CommandSuggestions.render (Lcom/mojang/blaze3d/vertex/PoseStack;II)V"))
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ICommandSuggestions jex = (ICommandSuggestions) CommandManagerJex.INSTANCE.jexCommandSuggestor;
        ICommandSuggestions mc = (ICommandSuggestions) this.commandSuggestions;
        ircMod.renderAboveChat = !(jex.isWindowActive() || mc.isWindowActive());

        if (ircMod.renderAboveChat && ircMod.ircClient != null && ircMod.ircClient.isConnected()) {
            normalChatButton.render(matrices, mouseX, mouseY, delta);
            ircChatButton.render(matrices, mouseX, mouseY, delta);
        }
        if (this.input.getValue().startsWith(CommandManagerJex.INSTANCE.getPrefix()) && CommandManagerJex.INSTANCE.jexCommandSuggestor != null)
            CommandManagerJex.INSTANCE.jexCommandSuggestor.render(matrices, mouseX, mouseY);
    }

    @Inject(method = "resize", at = @At("RETURN"))
    public void resize(Minecraft client, int width, int height, CallbackInfo ci) {
        if (CommandManagerJex.INSTANCE.jexCommandSuggestor != null)
            CommandManagerJex.INSTANCE.jexCommandSuggestor.updateCommandInfo();
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.input.getValue().startsWith(CommandManagerJex.INSTANCE.getPrefix()) && CommandManagerJex.INSTANCE.jexCommandSuggestor != null)
            if (CommandManagerJex.INSTANCE.jexCommandSuggestor.keyPressed(keyCode, scanCode, modifiers)) {
                cir.setReturnValue(true);
            }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    public void mouseScrolled(double mouseX, double mouseY, double amount, CallbackInfoReturnable<Boolean> cir) {
        if (amount > 1.0D) {
            amount = 1.0D;
        }

        if (amount < -1.0D) {
            amount = -1.0D;
        }

        if (this.input.getValue().startsWith(CommandManagerJex.INSTANCE.getPrefix()) && CommandManagerJex.INSTANCE.jexCommandSuggestor != null)
            if (CommandManagerJex.INSTANCE.jexCommandSuggestor.mouseScrolled(amount)) {
                cir.setReturnValue(true);
            }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (ircMod.getState() && ircMod.renderAboveChat && ircMod.ircClient != null && ircMod.ircClient.isConnected()) {
            normalChatButton.mouseClicked(mouseX, mouseY, button);
            ircChatButton.mouseClicked(mouseX, mouseY, button);
        }
        if (this.input.getValue().startsWith(CommandManagerJex.INSTANCE.getPrefix()) && CommandManagerJex.INSTANCE.jexCommandSuggestor != null)
            if (CommandManagerJex.INSTANCE.jexCommandSuggestor.mouseClicked((double)((int)mouseX), (double)((int)mouseY), button)) {
                cir.setReturnValue(true);
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
