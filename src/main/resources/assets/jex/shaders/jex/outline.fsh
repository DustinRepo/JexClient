#version 330 core

in vec2 TexCoord;
in vec4 frag_color;

uniform sampler2D Sampler;
uniform vec2 TexelSize;
uniform int Width;

out vec4 fragColor;

void main(){
    vec4 centerCol = texture2D(Sampler, TexCoord.st);

    if(centerCol.a != 0.0F) {
        gl_FragColor = vec4(0, 0, 0, 0);
        return;
    }

    vec4 color = vec4(0, 0, 0, 0);

    float closest = Width;
    for(int x = -Width; x <= Width; x++) {
        for(int y = -Width; y <= Width; y++) {
            vec4 currCol = texture2D(Sampler, TexCoord.st + vec2(x * TexelSize.x, y * TexelSize.y));
            if(currCol.a != 0.0F) {
                float currentDist = sqrt(x*x*1.0f + y*y*1.0f);
                if(currentDist < closest) {
                    closest = currentDist;
                    color = currCol;
                }

            }
        }
    }
    color.a = max(0, ((Width*1.0F) - (closest - 1)) / (Width*1.0F));
    fragColor = color;
}

