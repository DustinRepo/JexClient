package me.dustin.jex.load.mixin;

import me.dustin.jex.load.impl.IFrameBuffer;
import net.minecraft.client.gl.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Framebuffer.class)
public class MixinFrameBuffer implements IFrameBuffer {
    @Shadow
    private int depthAttachment;

    @Override
    public void setDepthAttachment(int depth) {
        this.depthAttachment = depth;
    }
}
