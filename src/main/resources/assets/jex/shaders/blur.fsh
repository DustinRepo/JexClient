#version 330 core

uniform sampler2D Sampler;

in vec2 TexCoord;
in vec2 Texel;

uniform vec2 Size;

uniform vec2 BlurDir;
uniform float Radius;

out vec4 fragColor;

void main() {
    vec4 blurred = vec4(0.0);
    float totalStrength = 0.0;
    float totalAlpha = 0.0;
    float totalSamples = 0.0;
    for(float r = -Radius; r <= Radius; r += 1.0) {
        vec4 sampleValue = texture(Sampler, TexCoord + Texel * r * BlurDir);

		// Accumulate average alpha
        float a = max(0.1F, sampleValue.a);
        totalAlpha = totalAlpha + a;
        totalSamples = totalSamples + 1.0;

		// Accumulate smoothed blur
        float strength = 1.0 - abs(r / Radius);
        totalStrength = totalStrength + strength;
        blurred = blurred + sampleValue;
    }
    fragColor = vec4(blurred.rgb / (Radius * 2.0 + 1.0), totalAlpha);
}
