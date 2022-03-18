#version 330 core

layout (location = 0) in vec3 Position;
layout (location = 1) in  vec4 Color;
layout (location = 2) in  vec2 Tex;

uniform mat4 Projection;
uniform mat4 ModelView;

out vec4 frag_color;
out vec2 texCoord;

void main() {
    gl_Position = Projection * ModelView * vec4(Position, 1.0);
    frag_color = Color;
    texCoord = Tex;
}
