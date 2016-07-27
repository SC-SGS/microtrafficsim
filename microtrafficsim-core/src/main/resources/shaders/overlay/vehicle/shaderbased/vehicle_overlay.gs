/*
 * Geometry-shader for the geometry-shader-based vehicle overlay.
 */

#version 150

layout(points) in;
layout(triangle_strip, max_vertices = 3) out;

in vec2 vehicle_normal[];
in vec4 vehicle_vert_color[];

out vec4 vehicle_frag_color;

uniform vec4 u_viewport;

uniform vec2 u_vehicle_size;
uniform float u_vehicle_scale;


void main() {
    // calculate axis in pixel-space
    vec2 dx = vehicle_normal[0].xy * u_vehicle_size.x * u_vehicle_scale;
    vec2 dy = vehicle_normal[0].yx * vec2(-1, 1) * u_vehicle_size.y * u_vehicle_scale;

    // transform to clip-space
    dx *= u_viewport.zw;
    dy *= u_viewport.zw;

    // calculate triangle points
    vec2 top = gl_in[0].gl_Position.xy + dx;
    vec2 bottom = gl_in[0].gl_Position.xy - dx;

    // draw triangle
    gl_Position = vec4(top, 0, 1);
    vehicle_frag_color = vehicle_vert_color[0];
    EmitVertex();

    gl_Position = vec4(bottom + dy, 0, 1);
    vehicle_frag_color = vehicle_vert_color[0];
    EmitVertex();

    gl_Position = vec4(bottom - dy, 0, 1);
    vehicle_frag_color = vehicle_vert_color[0];
    EmitVertex();
}
