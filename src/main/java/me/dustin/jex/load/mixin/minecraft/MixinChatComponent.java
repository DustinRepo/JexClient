package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventRenderChatHud;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.misc.IRC;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.load.impl.IChatComponent;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;

@Mixin(ChatComponent.class)
public class MixinChatComponent implements IChatComponent {

    @Shadow @Final private List<GuiMessage<Component>> allMessages;

    @Inject(method = "isChatHidden", at = @At("HEAD"), cancellable = true)
    public void isChatHidden1(CallbackInfoReturnable<Boolean> cir) {
        if (getThis() == Wrapper.INSTANCE.getMinecraft().gui.getChat()) {
            IRC irc = Feature.get(IRC.class);
            if (irc.getState() && irc.ircChatOverride) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render1(PoseStack matrices, int tickDelta, CallbackInfo ci) {
        EventRenderChatHud eventRenderChatHud = new EventRenderChatHud(getThis(), matrices, tickDelta).run();
        if (eventRenderChatHud.isCancelled())
            ci.cancel();
    }

    @Inject(method = "scrollChat", at = @At("HEAD"), cancellable = true)
    public void scroll1(int amount, CallbackInfo ci) {
        if (getThis() == Wrapper.INSTANCE.getMinecraft().gui.getChat()) {
            IRC irc = Feature.get(IRC.class);
            if (irc.getState() && irc.ircChatOverride) {
                IRC.ircChatHud.scrollChat(amount);
                ci.cancel();
            }
        }
    }

    @Inject(method = "handleChatQueueClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked1(double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (getThis() == Wrapper.INSTANCE.getMinecraft().gui.getChat()) {
            IRC irc = Feature.get(IRC.class);
            if (irc.getState() && irc.ircChatOverride) {
                IRC.ircChatHud.handleChatQueueClicked(mouseX, mouseY);
                cir.cancel();
            }
        }
    }

    private ChatComponent getThis() {
        return (ChatComponent)(Object)this;
    }

    @Override
    public boolean containsMessage(String message) {
        for (GuiMessage<Component> textChatHudLine : this.allMessages) {
            if (textChatHudLine.getMessage().getString().equalsIgnoreCase(message))
                return true;
        }
        return false;
    }

    @Override
    public List<GuiMessage<Component>> getMessages() {
        return allMessages;
    }
}
