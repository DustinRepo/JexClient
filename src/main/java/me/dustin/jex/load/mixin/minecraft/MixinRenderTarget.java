package me.dustin.jex.load.mixin.minecraft;

import com.mojang.blaze3d.pipeline.RenderTarget;
import me.dustin.jex.load.impl.IFrameBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderTarget.class)
public class MixinRenderTarget implements IFrameBuffer {

    @Shadow protected int depthBufferId;

    @Override
    public void setDepthAttachment(int depth) {
        this.depthBufferId = depth;
    }
}
