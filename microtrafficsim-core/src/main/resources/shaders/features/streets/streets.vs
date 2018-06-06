/*
 * Vertex-shader for anti-aliased thick line rendering with various joins and caps.
 */

#version 150

in vec3 a_position;
in vec3 a_line;

out vec4 vertex_color;
out vec3 vertex_line;

uniform vec4 u_color;
uniform mat4 u_view;
uniform mat4 u_projection;


void main() {
    vertex_color = u_color;
    vertex_line = a_line;
    gl_Position = u_projection * u_view * vec4(a_position.xyz, 1.0);
}
