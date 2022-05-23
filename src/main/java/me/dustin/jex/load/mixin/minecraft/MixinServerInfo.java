package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.chat.EventShouldPreviewChat;
import net.minecraft.client.network.ServerInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerInfo.class)
public class MixinServerInfo {

    @Shadow @Nullable private ServerInfo.@Nullable ChatPreview chatPreview;

    @Shadow private boolean temporaryChatPreviewState;

    @Inject(method = "shouldPreviewChat", at = @At("HEAD"), cancellable = true)
    public void shouldPreviewChat(CallbackInfoReturnable<Boolean> cir) {
        EventShouldPreviewChat eventShouldPreviewChat = new EventShouldPreviewChat(this.temporaryChatPreviewState && this.chatPreview != null).run();
        if (eventShouldPreviewChat.isCancelled())
            cir.setReturnValue(eventShouldPreviewChat.isEnabled());
    }

}
