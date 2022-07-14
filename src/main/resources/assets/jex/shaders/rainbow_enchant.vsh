#version 330 core

layout (location = 0) in vec3 Position;
layout (location = 1) in vec2 Tex;

uniform mat4 ModelView;
uniform mat4 Projection;
uniform mat4 TextureMatrix;

out float VertexDistance;
out vec2 TexCoord;
out vec3 Pos;

void main() {
    gl_Position = Projection * ModelView * vec4(Position, 1.0);

    VertexDistance = length((ModelView * vec4(Position, 1.0)).xyz);
    TexCoord = (TextureMatrix * vec4(Tex, 0.0, 1.0)).xy;
    Pos = Position;
}
