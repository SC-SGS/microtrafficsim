/*
 * Simple shader to copy a color and depth texture to a frame-buffer.
 */

#version 150

in vec2 v_texcoord;

uniform sampler2D u_color_sampler;
uniform sampler2D u_depth_sampler;

out vec4 out_color;


void main() {
    out_color    = texture(u_color_sampler, v_texcoord).rgba;
    gl_FragDepth = texture(u_depth_sampler, v_texcoord).r;
}
