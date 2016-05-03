#version 150

uniform float u_lineblur = 5.0;

in vec4 vertex_color;
in vec3 vertex_line;
out vec4 frag_color;

vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main()
{
    /* generic cap: round */
    float len = length(vertex_line.xy);

    /* generic cap: square */
    // float len = max(abs(vertex_line.x), abs(vertex_line.y));

    float alpha = 1.0 - smoothstep(vertex_line.z - u_lineblur, vertex_line.z, len);
	//frag_color = vec4(vertex_color.rgb, alpha);

    vec4 color_a = vec4(0.56, 0.76, 0.44, 1);
    vec4 color_b = vec4(0.00, 0.55, 0.80, 1);
    float gradient = (vertex_line.y + vertex_line.z) / (vertex_line.z * 2);
	frag_color = vec4(mix(color_a, color_b, gradient).rgb, alpha);
}
