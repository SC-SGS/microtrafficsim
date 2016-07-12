#version 150

in vec3 a_position;
in vec4 a_color;

out vec4 vertex_color;

uniform mat4 u_model;
uniform mat4 u_viewprojection;


void main() {
    vertex_color = a_color;
    gl_Position = u_viewprojection * u_model * vec4(a_position, 1.0);
}
