#version 150

#define OUTPUT_BLENDED				0
#define OUTPUT_BUFFER_ACCUMULATION  1
#define OUTPUT_BUFFER_REVEALAGE		2


out vec4 frag_color;

uniform int u_mode;
uniform sampler2D u_accumulation;
uniform sampler2D u_revealage;


void output_blended() {
	vec4 accum = texelFetch(u_accumulation, ivec2(gl_FragCoord.xy), 0);
	float reveal = texelFetch(u_revealage, ivec2(gl_FragCoord.xy), 0).r;

	frag_color = vec4(accum.rgb / max(accum.a, 1e-5), reveal);
}

void output_buffer_accumulation() {
	vec4 accum = texelFetch(u_accumulation, ivec2(gl_FragCoord.xy), 0);
	frag_color = vec4(accum.rgb / max(accum.a, 1e-5), 0.0);
}

void output_buffer_revealage() {
	frag_color = vec4(texelFetch(u_revealage, ivec2(gl_FragCoord.xy), 0).rrr, 0.0);
}

void main() {
	switch (u_mode) {
	case OUTPUT_BUFFER_ACCUMULATION:
		output_buffer_accumulation();
		break;

	case OUTPUT_BUFFER_REVEALAGE:
		output_buffer_revealage();
		break;
	
	default:
	case OUTPUT_BLENDED:
		output_blended();
		break;
	}
}
