package me.dustin.jex.load.impl;

import net.minecraft.client.gl.GlUniform;

public interface IShader {

    GlUniform getCustomUniform(String name);

}
