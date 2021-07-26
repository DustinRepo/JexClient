#version 330 core

uniform sampler2D Sampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler, texCoord);
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color;
}
