#version 150

#define EPSILON 0.00001

in vec2 vehicle_normal;
in vec4 vehicle_vert_color;
in float vehicle_layer;

uniform vec4 u_viewport;

uniform sampler2D u_sprite_sampler;
uniform sampler2D u_map_depth;

out vec4 out_color;


// NOTE: assuming quad-sprite
void main() {
	vec4 base_color = vehicle_vert_color;

	float normalized_layer = (vehicle_layer + 10) / 20;
	float map_depth = texture(u_map_depth, gl_FragCoord.xy * u_viewport.zw).r;
	if (normalized_layer + EPSILON < map_depth)
	    base_color *= vec4(1, 1, 1, 0.5);

	float c = dot(vehicle_normal, vec2(0, 1));
	float s = sqrt(1.0 - c * c) * sign(vehicle_normal.x);
	mat2 rotation = mat2(c, s, -s, c);
	vec2 tc_sprite = (rotation * (gl_PointCoord.st - vec2(0.5, 0.5))) + vec2(0.5, 0.5);

    out_color = texture(u_sprite_sampler, tc_sprite) * base_color;
}
