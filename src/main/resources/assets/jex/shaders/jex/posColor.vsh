#version 330 core

layout (location = 0) in vec3 Position;
layout (location = 1) in  vec4 Color;

uniform mat4 Projection;
uniform mat4 ModelView;

out vec4 frag_color;

void main(){
    gl_Position = Projection * ModelView * vec4(Position, 1.0);
    frag_color = Color;
}
