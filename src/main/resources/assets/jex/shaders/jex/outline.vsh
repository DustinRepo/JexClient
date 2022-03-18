#version 330 core

layout (location = 0) in vec3 Position;
layout (location = 1) in vec4 Color;
layout (location = 2) in vec2 Tex;

uniform mat4 ModelView;

out vec2 TexCoord;
out vec4 frag_color;

void main() {
    TexCoord = Tex;
    gl_Position = ModelView * vec4(Position, 1.0);
}
