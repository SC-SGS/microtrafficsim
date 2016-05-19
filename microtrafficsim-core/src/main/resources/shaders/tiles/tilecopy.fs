#version 150

in vec2 v_texcoord;

uniform sampler2D u_tile_sampler;

out vec4 out_color;


void main() {
    out_color = texture(u_tile_sampler, v_texcoord).rgba;
}

