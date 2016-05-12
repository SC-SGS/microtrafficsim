#version 150

in vec3 a_position;

uniform mat4 u_tile;
uniform mat4 u_view;
uniform mat4 u_projection;

void main() {
	gl_Position = u_projection * u_view * u_tile * vec4(a_position.xyz, 1.0);
}

