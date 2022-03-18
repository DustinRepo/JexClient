#version 330 core

uniform sampler2D Sampler;

in vec4 frag_color;
in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler, texCoord) * frag_color;
    if (color.a < 0.1) {
        discard;
    }
    fragColor = color;
}
