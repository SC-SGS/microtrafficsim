/*
 * A simple shader to copy/project tiles form a texture to the screen.
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

