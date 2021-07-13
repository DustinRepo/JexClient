#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform vec4 GlintColor;
uniform int CrazyRainbow;
uniform float Saturation;
uniform float Alpha;
uniform int MathMode;

in float vertexDistance;
in vec2 texCoord0;
in vec3 pos;

out vec4 fragColor;

vec3 hsb2rgb( in vec3 c ){
    vec3 rgb = clamp(abs(mod(c.x*6.0+vec3(0.0,4.0,2.0),
    6.0)-3.0)-1.0,
    0.0,
    1.0 );
    rgb = rgb*rgb*(3.0-2.0*rgb);
    return c.z * mix(vec3(1.0), rgb, c.y);
}

void main() {
    vec4 color = texture(Sampler0, texCoord0) * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }
    if (CrazyRainbow == 1) {
        switch (MathMode) {
            case 0:
            fragColor = vec4(hsb2rgb(vec3(fract(color.r + color.g + color.b + pos.x + pos.y), Saturation, Alpha)), 1);
            return;
            case 1:
            fragColor = vec4(hsb2rgb(vec3(fract(cos(color.r + color.g + color.b + pos.x + pos.y) * 180), Saturation, Alpha)), 1);
            return;
            case 2:
            fragColor = vec4(hsb2rgb(vec3(fract(cos(color.r + color.g + color.b + pos.x * pos.y) * 45), Saturation, Alpha)), 1);
            return;
        }
        fragColor = vec4(hsb2rgb(vec3(fract(color.r + color.g + color.b + pos.x + pos.y), Saturation, Alpha)), 1);
    } else {
        fragColor = vec4(GlintColor.r, GlintColor.g, GlintColor.b, 1.0);
    }
}
