#version 150

#define EPSILON 0.00001

in mediump vec2 vehicle_normal;
in lowp vec4 vehicle_vert_color;
in float vehicle_layer;

uniform vec4 u_viewport;

uniform sampler2D u_sprite_sampler;
uniform sampler2D u_map_depth;

out vec4 out_color;


// NOTE: assuming quad-sprite
vec4 color_sprite() {
	float c = dot(vehicle_normal, vec2(0, 1));
	float s = sqrt(1.0 - c * c) * sign(vehicle_normal.x);

	vec2 tc = (mat2(c, s, -s, c) * (gl_PointCoord.xy - vec2(0.5, 0.5))) + vec2(0.5, 0.5);

    return texture(u_sprite_sampler, tc);
}

vec4 color_depth() {
    vec2 tc = gl_FragCoord.xy * u_viewport.zw;   // TODO: use gl_PointCoord.xy to determine depth?

	float normalized_layer = (vehicle_layer + 10) / 20;
	float map_depth = texture(u_map_depth, tc).r;

	if (normalized_layer + EPSILON < map_depth) {
	    return vec4(1, 1, 1, 0.5);
	} else {
	    return vec4(1, 1, 1, 1);
	}
}

void main() {
    out_color = vehicle_vert_color * color_sprite() * color_depth();
}
