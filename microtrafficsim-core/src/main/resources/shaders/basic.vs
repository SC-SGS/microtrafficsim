#version 150

in vec3 a_position;
out vec4 vertex_color;

uniform vec4 u_color = vec4(0, 0, 0, 1);
uniform mat4 u_view;
uniform mat4 u_projection;

void main() {
    vertex_color = u_color;
	gl_Position = u_projection * u_view * vec4(a_position.xyz, 1.0);
}
