/*
 * Fragment-shader for anti-aliased thick line rendering with various joins and caps.
 */

#version 150

uniform float u_lineblur = 25.0000;
uniform float u_viewscale;

in vec4 vertex_color;
in vec3 vertex_line;
out vec4 frag_color;


void main() {
    float len = length(vertex_line.xy);
    float lineblur = u_lineblur / u_viewscale;
    float alpha = 1.0 - smoothstep(vertex_line.z - lineblur, vertex_line.z, len);
    if (alpha == 0.0) discard;

    frag_color = vec4(vertex_color.rgb, alpha);
    // frag_color = vec4((vertex_line.xy / vertex_line.z + 1.0) / 2.0, 1.0, alpha);
}
