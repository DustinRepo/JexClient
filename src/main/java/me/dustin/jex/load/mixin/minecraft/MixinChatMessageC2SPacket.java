package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.load.impl.IChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChatMessageC2SPacket.class)
public class MixinChatMessageC2SPacket implements IChatMessageC2SPacket {
    @Mutable
    @Shadow @Final private String chatMessage;

    @Override
    public void setMessage(String message) {
        this.chatMessage = message;
    }
}
