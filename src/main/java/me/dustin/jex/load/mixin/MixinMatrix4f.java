package me.dustin.jex.load.mixin;

import me.dustin.jex.JexClient;
import me.dustin.jex.helper.math.Matrix4x4;
import me.dustin.jex.load.impl.IMatrix4f;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Matrix4f.class)
public class MixinMatrix4f implements IMatrix4f {
    @Shadow protected float a00;

    @Shadow protected float a01;

    @Shadow protected float a02;

    @Shadow protected float a03;

    @Shadow protected float a10;

    @Shadow protected float a11;

    @Shadow protected float a12;

    @Shadow protected float a13;

    @Shadow protected float a20;

    @Shadow protected float a21;

    @Shadow protected float a22;

    @Shadow protected float a23;

    @Shadow protected float a30;

    @Shadow protected float a31;

    @Shadow protected float a32;

    @Shadow protected float a33;

    @Override
    public float[] toFloatArray() {
        float[] floats = new float[4*4];
        floats[0] = this.a00;
        floats[1] = this.a01;
        floats[2] = this.a02;
        floats[3] = this.a03;
        floats[4] = this.a10;
        floats[5] = this.a11;
        floats[6] = this.a12;
        floats[7] = this.a13;
        floats[8] = this.a20;
        floats[9] = this.a21;
        floats[10] = this.a22;
        floats[11] = this.a23;
        floats[12] = this.a30;
        floats[13] = this.a31;
        floats[14] = this.a32;
        floats[15] = this.a33;
        return floats;
    }
}
