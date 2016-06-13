#version 150

in highp vec3 a_position;
out lowp vec4 vertex_color_gs;

uniform lowp vec4 u_color;
uniform highp mat4 u_view;
uniform highp mat4 u_projection;

void main() {
    vertex_color_gs = u_color;
    gl_Position = u_projection * u_view * vec4(a_position.xyz, 1.0);
}