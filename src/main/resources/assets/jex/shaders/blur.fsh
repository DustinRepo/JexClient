#version 150

in vec4 frag_color;

out vec4 fragColor;

void main() {
    fragColor = vec4(frag_color.xyz, 1.0);
}
