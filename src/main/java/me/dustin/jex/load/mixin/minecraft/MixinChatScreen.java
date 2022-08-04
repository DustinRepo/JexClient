package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.misc.IRC;
import me.dustin.jex.load.impl.IChatScreen;
import me.dustin.jex.load.impl.IChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class MixinChatScreen implements IChatScreen {

    @Shadow protected TextFieldWidget chatField;
    @Shadow private ChatInputSuggestor chatInputSuggestor;
    private ButtonWidget normalChatButton;
    private ButtonWidget ircChatButton;
    private IRC ircMod;

    @Inject(method = "init", at = @At("RETURN"))
    public void init(CallbackInfo ci) {
        ircMod = Feature.get(IRC.class);
        normalChatButton = new ButtonWidget(chatField.x - 2, chatField.y - 22, 40, 18, Text.of(ircMod.ircChatOverride ? "\2477Chat": "\247bChat"), button -> {
            ircChatButton.setMessage(Text.of("\2477IRC"));
            normalChatButton.setMessage(Text.of("\247bChat"));
            ircMod.ircChatOverride = false;
        });
        ircChatButton = new ButtonWidget(chatField.x - 2 + 42, chatField.y - 22, 40, 18, Text.of(ircMod.ircChatOverride ? "\247cIRC" : "\2477IRC"), button -> {
            normalChatButton.setMessage(Text.of("\2477Chat"));
            ircChatButton.setMessage(Text.of("\247cIRC"));
            ircMod.ircChatOverride = true;
        });
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "net/minecraft/client/gui/screen/ChatInputSuggestor.render(Lnet/minecraft/client/util/math/MatrixStack;II)V"))
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        IChatInputSuggestor mc = (IChatInputSuggestor) this.chatInputSuggestor;
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
