#version 330 core

layout (location = 0) in vec3 Position;
layout (location = 1) in vec4 Color;
layout (location = 2) in vec2 Tex;
layout (location = 3) in ivec2 Tex2;
layout (location = 4) in vec3 Normal;

uniform sampler2D Sampler2;

uniform mat4 ModelView;
uniform mat4 Projection;
uniform vec3 ChunkOffset;
uniform int FogShape;

out float vertexDistance;
out vec4 vertexColor;
out vec2 TexCoord;
out vec4 normal;

float fog_distance(mat4 modelViewMat, vec3 pos, int shape) {
    if (shape == 0) {
        return length((modelViewMat * vec4(pos, 1.0)).xyz);
    } else {
        float distXZ = length((modelViewMat * vec4(pos.x, 0.0, pos.z, 1.0)).xyz);
        float distY = length((modelViewMat * vec4(0.0, pos.y, 0.0, 1.0)).xyz);
        return max(distXZ, distY);
    }
}

vec4 minecraft_sample_lightmap(sampler2D lightMap, ivec2 uv) {
    return texture(lightMap, clamp(uv / 256.0, vec2(0.5 / 16.0), vec2(15.5 / 16.0)));
}

void main() {
    vec3 pos = Position + ChunkOffset;
    gl_Position = Projection * ModelView * vec4(pos, 1.0);

    vertexDistance = fog_distance(ModelView, pos, FogShape);
    vertexColor = Color * minecraft_sample_lightmap(Sampler2, Tex2);
    TexCoord = Tex;
    normal = Projection * ModelView * vec4(Normal, 0.0);
}
