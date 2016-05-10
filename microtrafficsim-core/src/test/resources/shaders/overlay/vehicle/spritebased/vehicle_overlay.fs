#version 150

in vec2 vehicle_normal;
in vec4 vehicle_vert_color;

uniform sampler2D u_sprite_sampler;

out vec4 out_color;


// NOTE: assuming quad-sprite
void main() {
	float c = dot(vehicle_normal, vec2(0, 1));
	float s = sqrt(1.0 - c * c) * sign(vehicle_normal.x);
	mat2 rotation = mat2(c, s, -s, c);

	vec2 texcoord = (rotation * (gl_PointCoord.st - vec2(0.5, 0.5))) + vec2(0.5, 0.5);

    out_color = texture(u_sprite_sampler, texcoord) * vehicle_vert_color;
}
