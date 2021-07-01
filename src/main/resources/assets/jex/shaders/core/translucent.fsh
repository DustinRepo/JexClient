#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float Alpha;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 normal;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }
    vec4 final = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
    //set back to normal render if Alpha uniform is more than 1 (off) or Alpha uniform is more than current alpha (fixing things like water going more sold than it should be in the fade)
    if (Alpha > 1.0 || Alpha > final.a) {
        fragColor = final;
    } else {
        fragColor = vec4(final.rgb, Alpha);
    }
}
