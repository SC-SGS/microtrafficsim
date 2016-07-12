#version 150

in vec2 v_texcoord;
out vec4 out_color;

void main() {
    out_color = vec4(v_texcoord.xy, 0, 0.5);
}
