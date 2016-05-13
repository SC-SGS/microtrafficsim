#version 150

in vec3 a_position;
in vec2 a_texcoord;

out vec2 v_texcoord;

uniform mat4 u_tile;
uniform mat4 u_view;
uniform mat4 u_projection;

void main() {
    v_texcoord = a_texcoord;
	gl_Position = u_projection * u_view * u_tile * vec4(a_position.xyz, 1.0);
}

