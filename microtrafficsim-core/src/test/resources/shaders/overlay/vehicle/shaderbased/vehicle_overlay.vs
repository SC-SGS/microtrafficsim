#version 150

in vec2 a_position2;
in vec2 a_normal2;
in vec4 a_color;

uniform mat4 u_view;
uniform mat4 u_projection;

out vec2 vehicle_normal;
out vec4 vehicle_vert_color;


void main() {
    vehicle_normal = a_normal2.xy;
    vehicle_vert_color = a_color.rgba;
    gl_Position = u_projection * u_view * vec4(a_position2.xy, 0.0, 1.0);
}
