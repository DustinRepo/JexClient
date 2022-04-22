package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.feature.command.CommandManagerJex;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.misc.IRC;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.load.impl.IChatScreen;
import me.dustin.jex.load.impl.ICommandSuggestor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class MixinChatScreen implements IChatScreen {

    @Shadow protected TextFieldWidget chatField;

    @Shadow private CommandSuggestor commandSuggestor;

    @Shadow @Final private String originalChatText;
    private ButtonWidget normalChatButton;
    private ButtonWidget ircChatButton;
    private IRC ircMod;

    @Inject(method = "init", at = @At("RETURN"))
    public void init(CallbackInfo ci) {
        CommandManagerJex.INSTANCE.jexCommandSuggestor = new CommandSuggestor(Wrapper.INSTANCE.getMinecraft(), (ChatScreen)(Object)this, this.chatField, Wrapper.INSTANCE.getTextRenderer(), false, true, 1, 10, true, -805306368);
        CommandManagerJex.INSTANCE.jexCommandSuggestor.refresh();
        ircMod = Feature.get(IRC.class);
        normalChatButton = new ButtonWidget(chatField.x - 2, chatField.y - 22, 40, 18, new LiteralText(ircMod.ircChatOverride ? "\2477Chat": "\247bChat"), button -> {
            ircChatButton.setMessage(new LiteralText("\2477IRC"));
            normalChatButton.setMessage(new LiteralText("\247bChat"));
            ircMod.ircChatOverride = false;
        });
        ircChatButton = new ButtonWidget(chatField.x - 2 + 42, chatField.y - 22, 40, 18, new LiteralText(ircMod.ircChatOverride ? "\247cIRC" : "\2477IRC"), button -> {
            normalChatButton.setMessage(new LiteralText("\2477Chat"));
            ircChatButton.setMessage(new LiteralText("\247cIRC"));
            ircMod.ircChatOverride = true;
        });
    }

    @Inject(method = "onChatFieldUpdate", at = @At("RETURN"))
    public void onChatFieldUpdate(String chatText, CallbackInfo ci) {
        if (this.chatField == null || CommandManagerJex.INSTANCE.jexCommandSuggestor == null) return;
        String string = this.chatField.getText();
        CommandManagerJex.INSTANCE.jexCommandSuggestor.setWindowActive(!string.equals(this.originalChatText));
        CommandManagerJex.INSTANCE.jexCommandSuggestor.refresh();
    }

    @Inject(method = "setChatFromHistory", at = @At(value = "INVOKE", target = "net/minecraft/client/gui/screen/CommandSuggestor.setWindowActive(Z)V"))
    public void setChat(int offset, CallbackInfo ci) {
        if (CommandManagerJex.INSTANCE.jexCommandSuggestor != null)
            CommandManagerJex.INSTANCE.jexCommandSuggestor.setWindowActive(false);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "net/minecraft/client/gui/screen/CommandSuggestor.render(Lnet/minecraft/client/util/math/MatrixStack;II)V"))
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ICommandSuggestor jex = (ICommandSuggestor) CommandManagerJex.INSTANCE.jexCommandSuggestor;
        ICommandSuggestor mc = (ICommandSuggestor) this.commandSuggestor;
        ircMod.renderAboveChat = !(jex.isWindowActive() || mc.isWindowActive());

        if (ircMod.renderAboveChat && ircMod.ircClient != null && ircMod.ircClient.isConnected()) {
            normalChatButton.render(matrices, mouseX, mouseY, delta);
            ircChatButton.render(matrices, mouseX, mouseY, delta);
        }
        if (this.chatField.getText().startsWith(CommandManagerJex.INSTANCE.getPrefix()) && CommandManagerJex.INSTANCE.jexCommandSuggestor != null)
            CommandManagerJex.INSTANCE.jexCommandSuggestor.render(matrices, mouseX, mouseY);
    }

    @Inject(method = "resize", at = @At("RETURN"))
    public void resize(MinecraftClient client, int width, int height, CallbackInfo ci) {
        if (CommandManagerJex.INSTANCE.jexCommandSuggestor != null)
            CommandManagerJex.INSTANCE.jexCommandSuggestor.refresh();
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.chatField.getText().startsWith(CommandManagerJex.INSTANCE.getPrefix()) && CommandManagerJex.INSTANCE.jexCommandSuggestor != null)
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

        if (this.chatField.getText().startsWith(CommandManagerJex.INSTANCE.getPrefix()) && CommandManagerJex.INSTANCE.jexCommandSuggestor != null)
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
        if (this.chatField.getText().startsWith(CommandManagerJex.INSTANCE.getPrefix()) && CommandManagerJex.INSTANCE.jexCommandSuggestor != null)
            if (CommandManagerJex.INSTANCE.jexCommandSuggestor.mouseClicked((double)((int)mouseX), (double)((int)mouseY), button)) {
                cir.setReturnValue(true);
            }
    }

    @Override
    public String getText() {
        return this.chatField.getText();
    }

    @Override
    public void setText(String text) {
        this.chatField.setText(text);
    }

    @Override
    public TextFieldWidget getWidget() {
        return this.chatField;
    }

}
