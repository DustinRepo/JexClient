package me.dustin.jex.load.impl;

import com.mojang.blaze3d.shaders.Uniform;

public interface IShader {

    Uniform getCustomUniform(String name);

}
