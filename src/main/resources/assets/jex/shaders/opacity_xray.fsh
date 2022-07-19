#version 330 core

uniform sampler2D Sampler;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float Alpha;

in float vertexDistance;
in vec4 vertexColor;
in vec2 TexCoord;
in vec4 normal;

out vec4 fragColor;

vec4 linear_fog(vec4 inColor, float vertexDistance, float fogStart, float fogEnd, vec4 fogColor) {
    if (vertexDistance <= fogStart) {
        return inColor;
    }

    float fogValue = vertexDistance < fogEnd ? smoothstep(fogStart, fogEnd, vertexDistance) : 1.0;
    return vec4(mix(inColor.rgb, fogColor.rgb, fogValue * fogColor.a), inColor.a);
}

void main() {
    vec4 color = texture(Sampler, TexCoord) * vertexColor * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }
    vec4 final = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
    if (Alpha > 1.0 || Alpha > final.a) {
        fragColor = final;
    } else {
        fragColor = vec4(final.rgb, Alpha);
    }
}
