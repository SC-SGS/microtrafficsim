#version 150

in vec3 a_position;
out vec4 vertex_color_gs;

uniform vec4 u_color;
uniform mat4 u_viewprojection;
uniform mat4 u_model;

void main() {
    vertex_color_gs = u_color;
    gl_Position = u_viewprojection * u_model * vec4(a_position.xyz, 1.0);
}
