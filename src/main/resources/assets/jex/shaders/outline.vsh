#version 330 core

layout (location = 0) in vec3 Position;

uniform mat4 Projection;
uniform vec2 Size;

out vec2 TexCoord;
out vec2 Texel;

void main() {
    vec4 outPos = Projection * vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.2, 1.0);
    Texel = 1.0 / Size;
    TexCoord = Position.xy / Size;
}