#version 330 core

uniform sampler2D Sampler;

uniform vec4 ColorModulator;
uniform vec4 GlintColor;
uniform int CrazyRainbow;
uniform float Saturation;
uniform float Alpha;
uniform int MathMode;

in float VertexDistance;
in vec2 TexCoord;
in vec3 Pos;

out vec4 fragColor;

vec3 hsb2rgb(in vec3 c){
    vec3 rgb = clamp(abs(mod(c.x*6.0+vec3(0.0,4.0,2.0),6.0)-3.0)-1.0,
    0.0,
    1.0 );
    rgb = rgb*rgb*(3.0-2.0*rgb);
    return c.z * mix(vec3(1.0), rgb, c.y);
}

vec4 transRights(vec3 Pos) {
    float y = fract(Pos.y * 5);
    if (y > 0.30) {
        return vec4( 96, 205, 248, 255) / 255;
    } else if (y > 0.15) {
        return vec4(243, 168, 183, 255) / 255;
    } else {
        return vec4(255, 255, 255, 255) / 255;
    }
}

void main() {
    vec4 color = texture(Sampler, TexCoord) * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }
    if (CrazyRainbow == 1) {
        switch (MathMode) {
            case 0:
            fragColor = vec4(hsb2rgb(vec3(fract(color.r + color.g + color.b + Pos.x + Pos.y), Saturation, Alpha)), 1);
            return;
            case 1:
            fragColor = transRights(color.xyz);
            return;
            case 2:
            fragColor = vec4(hsb2rgb(vec3(fract(cos(color.r + color.g + color.b + Pos.x + Pos.y) * 180), Saturation, Alpha)), 1);
            return;
            case 3:
            fragColor = vec4(hsb2rgb(vec3(fract(cos(color.r + color.g + color.b + Pos.x * Pos.y) * 45), Saturation, Alpha)), 1);
            return;
        }
        fragColor = vec4(hsb2rgb(vec3(fract(color.r + color.g + color.b + Pos.x + Pos.y), Saturation, Alpha)), 1);
    } else {
        fragColor = vec4(GlintColor.r, GlintColor.g, GlintColor.b, 1.0);
    }
}
