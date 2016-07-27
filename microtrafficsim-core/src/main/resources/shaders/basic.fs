/*
 * Basic fragment shader, outputting the vertex color.
 */

#version 150

in vec4 vertex_color;
out vec4 out_color;

void main() {
    out_color = vertex_color;
}
