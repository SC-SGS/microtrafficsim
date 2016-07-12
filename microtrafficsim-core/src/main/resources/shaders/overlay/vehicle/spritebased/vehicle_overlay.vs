#version 150

in highp vec3 a_position;
in vec2 a_normal2;
in vec4 a_color;

uniform highp mat4 u_view;
uniform highp mat4 u_projection;

out vec2 vehicle_normal;
out vec4 vehicle_vert_color;
out float vehicle_layer;


void main() {
    vehicle_normal = a_normal2;
    vehicle_vert_color = a_color;
    vehicle_layer = a_position.z;
    gl_Position = u_projection * u_view * vec4(a_position.xy, 0.0, 1.0);
}
