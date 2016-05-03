#version 150

in vec4 vertex_color;

out vec4 out_accumulation;
out vec3 out_revealage;

uniform vec2 u_zplane = vec2(0.1, 100);

float linear_depth() {
	float n = u_zplane.x;
	float f = u_zplane.y;
	return abs((n * f) / gl_FragCoord.z * (n - f) - f);
}

void main() {
	vec4 color = vertex_color;
	float z = linear_depth();

	float weight = max(min(1.0, max(max(color.r, color.g), color.b) * color.a), color.a)
		* clamp(0.03 / (1e-5 + pow(z / 200, 4.0)), 1e-2, 3e3);

	out_accumulation = vec4(color.rgb * color.a, color.a) * weight;
	out_revealage = color.aaa;
}

