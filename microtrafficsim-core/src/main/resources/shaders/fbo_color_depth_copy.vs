/*
 * Simple shader to copy a color and depth texture to a frame-buffer.
 */

#version 150

out vec2 v_texcoord;

const vec4[] obj1 = vec4[](
    vec4(-1,  1, 0, 1),
    vec4(-1, -3, 0, 1),
    vec4( 3,  1, 0, 1)
);

const vec2[] t = vec2[](
    vec2(0,  1),
    vec2(0, -1),
    vec2(2,  1)
);

void main() {
    v_texcoord = t[gl_VertexID];
    gl_Position = obj1[gl_VertexID];
}
