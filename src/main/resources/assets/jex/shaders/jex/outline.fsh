#version 330 core

in vec2 TexCoord;
in vec2 Texel;

uniform sampler2D Sampler;
uniform int Width;
uniform int Glow;
uniform float GlowIntensity;

out vec4 fragColor;

void main(){
    vec4 centerCol = texture(Sampler, TexCoord.st);

    if (centerCol.a != 0.0F) {
        fragColor = vec4(0, 0, 0, 0);
        return;
    }

    vec4 color = vec4(0, 0, 0, 0);

    float dist = Width * Width * 4.0;
    for (int x = -Width; x <= Width; x++) {
        for (int y = -Width; y <= Width; y++) {
            vec4 offset = texture(Sampler, TexCoord + Texel * vec2(x, y));

            if (offset.a != 0) {
                float ndist = x * x + y * y - 1.0;
                dist = min(ndist, dist);
                color = offset;
            }
        }
    }

    float minDist = Width * Width;

    if (dist > minDist) {
        discard;
    } else {
        if (Glow == 1) color.a = max(0, ((Width*1.0F) - dist) / (Width*(1.0F + (1.0F - GlowIntensity))));
        else color.a = min((1.0 - (dist / minDist)) * 3.5, 1.0);
    }
    fragColor = color;
}

