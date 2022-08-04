package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventRenderChatHud;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.misc.IRC;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.load.impl.IChatHud;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

@Mixin(ChatHud.class)
public class MixinChatHud implements IChatHud {

    @Shadow @Final private List<ChatHudLine> messages;

    @Inject(method = "isChatHidden", at = @At("HEAD"), cancellable = true)
    public void isChatHidden1(CallbackInfoReturnable<Boolean> cir) {
        if (getThis() == Wrapper.INSTANCE.getMinecraft().inGameHud.getChatHud()) {
            IRC irc = Feature.get(IRC.class);
            if (irc.getState() && irc.ircChatOverride) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render1(MatrixStack matrices, int tickDelta, CallbackInfo ci) {
        EventRenderChatHud eventRenderChatHud = new EventRenderChatHud(getThis(), matrices, tickDelta).run();
        if (eventRenderChatHud.isCancelled())
            ci.cancel();
    }

    @Inject(method = "scroll", at = @At("HEAD"), cancellable = true)
    public void scroll1(int amount, CallbackInfo ci) {
        if (getThis() == Wrapper.INSTANCE.getMinecraft().inGameHud.getChatHud()) {
            IRC irc = Feature.get(IRC.class);
            if (irc.getState() && irc.ircChatOverride) {
                IRC.ircChatHud.scroll(amount);
                ci.cancel();
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked1(double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (getThis() == Wrapper.INSTANCE.getMinecraft().inGameHud.getChatHud()) {
            IRC irc = Feature.get(IRC.class);
            if (irc.getState() && irc.ircChatOverride) {
                IRC.ircChatHud.mouseClicked(mouseX, mouseY);
                cir.cancel();
            }
        }
    }

    private ChatHud getThis() {
        return (ChatHud)(Object)this;
    }

    @Override
    public boolean containsMessage(String message) {
        for (ChatHudLine textChatHudLine : this.messages) {
            if (textChatHudLine.content().getString().equalsIgnoreCase(message))
                return true;
        }
        return false;
    }

    @Override
    public List<ChatHudLine> getMessages() {
        return messages;
    }
}
