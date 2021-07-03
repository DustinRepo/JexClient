#version 330

in vec3 Position;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 frag_color;

void main(){
    //gl_Position = vec4(Position.xyz, 1.0);
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    frag_color = Color;
}
