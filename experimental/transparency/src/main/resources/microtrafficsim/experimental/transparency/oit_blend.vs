#version 150

const vec4[] v = vec4[](
	vec4(-1,  1, 0, 1),
	vec4(-1, -3, 0, 1),
	vec4( 3,  1, 0, 1)
);

void main() {
	gl_Position = v[gl_VertexID];
}
