#version 150

uniform float u_lineblur = 5.0;

in lowp vec4 vertex_color;
in mediump vec3 vertex_line;
out vec4 frag_color;


void main() {
	float len = length(vertex_line.xy);
    float alpha = 1.0 - smoothstep(vertex_line.z - u_lineblur, vertex_line.z, len);
    if (alpha == 0.0) discard;

	frag_color = vec4(vertex_color.rgb, alpha);
}
