#version 150

in vec3 a_position;
in vec2 a_texcoord;

out vec2 v_texcoord;

uniform mat4 u_tilecopy;
uniform mat4 u_view;
uniform mat4 u_projection;

#define FP_FLICKER_OFFSET   0.00001

void main() {
    v_texcoord = a_texcoord;
	vec4 pos = u_projection * u_view * u_tilecopy * vec4(a_position.xyz, 1.0);

	// fix border flickering issues due to fp-precision with slight offsets
	pos.xy += FP_FLICKER_OFFSET * (2*a_texcoord - vec2(1, 1));
	gl_Position = pos;
}
