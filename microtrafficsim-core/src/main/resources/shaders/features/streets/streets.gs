#version 150

/* -- cap and join types ------------------------------------------------------------------------ */
#define CAP_TYPE_BUTT           0
#define CAP_TYPE_SQUARE         1
#define CAP_TYPE_ROUND          2

#define JOIN_TYPE_NONE          0
#define JOIN_TYPE_MITER         1
#define JOIN_TYPE_BEVEL         2
#define JOIN_TYPE_ROUND         3

/* -- style limits ------------------------------------------------------------------------------ */
#define LINE_WIDTH_LIMIT_CAP    1.5
#define LINE_WIDTH_LIMIT_JOIN   1.5

#define MITER_ANGLE_LIMIT       0.5


/* -- shader inputs ----------------------------------------------------------------------------- */
layout(lines_adjacency) in;
layout(triangle_strip, max_vertices = 22) out;

in lowp vec4[] vertex_color_gs;
out lowp vec4  vertex_color;
out mediump vec3  vertex_line;

uniform vec4  u_viewport;
uniform float u_viewscale;
uniform float u_viewscale_norm;
uniform float u_zoom_correction =       0.03;

uniform float u_linewidth;
uniform float u_square_cap_extrusion    = 1.0;
uniform int   u_cap_type                = CAP_TYPE_ROUND;
uniform int   u_join_type               = JOIN_TYPE_ROUND;


/* -- misc -------------------------------------------------------------------------------------- */

/* vec2 cross function */
float cross(vec2 a, vec2 b) { return a.x * b.y - a.y * b.x; }

/* screen-space pixel-space tranformations */
vec4 s_to_p(vec4 v) { return vec4(v.xy * u_viewport.xy, v.zw); }
vec4 p_to_s(vec4 v) { return vec4(v.xy * u_viewport.zw, v.zw); }

/* use none-cap on thin lines */
int get_sane_cap_type(float linewidth) {
    return linewidth < LINE_WIDTH_LIMIT_CAP ? CAP_TYPE_BUTT : u_cap_type;
}

/* use none-join on thin lines */
int get_sane_join_type(float linewidth) {
    return linewidth < LINE_WIDTH_LIMIT_JOIN ? JOIN_TYPE_NONE : u_join_type;
}

/* EmitVertex() wrapper */
void emit_vertex(vec4 position, vec3 line, vec4 color) {
    gl_Position = position;
    vertex_line = line;
    vertex_color = color;
    EmitVertex();
}

/* none-join / butt-cap */
void emit_none(vec4 position, vec2 normal, float linewidth, vec4 color) {
    vec4 p1 = p_to_s(vec4(position.xy + normal.xy, position.zw));
    vec4 p2 = p_to_s(vec4(position.xy - normal.xy, position.zw));

    emit_vertex(p1, vec3(0,  linewidth, linewidth), color);
    emit_vertex(p2, vec3(0, -linewidth, linewidth), color);
}


/* -- caps -------------------------------------------------------------------------------------- */

void emit_square_cap_a(vec4 position, vec2 normal, float linewidth, vec4 color) {
    float ext_len = u_square_cap_extrusion * linewidth;

    vec2 line = vec2(-normal.y, normal.x);
    vec2 extrusion = line * u_square_cap_extrusion;

    vec4 v0 = p_to_s(vec4(position.xy - (line / linewidth) * 0.01, position.zw));
    vec4 v1 = p_to_s(vec4(position.xy + normal.xy,             position.zw));
    vec4 v2 = p_to_s(vec4(position.xy + normal.xy + extrusion, position.zw));
    vec4 v3 = p_to_s(vec4(position.xy - normal.xy + extrusion, position.zw));
    vec4 v4 = p_to_s(vec4(position.xy - normal.xy,             position.zw));

    vec3 p0 = vec3(       0,          0, linewidth);
    vec3 p1 = vec3(       0,  linewidth, linewidth);
    vec3 p2 = vec3(       0, -linewidth, linewidth);
    vec3 p3 = vec3(       0,          0,   ext_len);
    vec3 p4 = vec3(-ext_len,          0,   ext_len);

    /* top */
    emit_vertex(v0, p0, color);
    emit_vertex(v1, p1, color);
    emit_vertex(v2, p1, color);
    EndPrimitive();

    /* center */
    emit_vertex(v0, p3, color);
    emit_vertex(v2, p4, color);
    emit_vertex(v3, p4, color);
    EndPrimitive();

    /* bottom */
    emit_vertex(v0, p0, color);
    emit_vertex(v3, p2, color);
    emit_vertex(v4, p2, color);
    EndPrimitive();

    /* connector */
    emit_vertex(v1, p1, color);
    emit_vertex(v4, p2, color);
}

void emit_square_cap_b(vec4 position, vec2 normal, float linewidth, vec4 color) {
    float ext_len = u_square_cap_extrusion * linewidth;

    vec2 line = -vec2(-normal.y, normal.x);
    vec2 extrusion = line * u_square_cap_extrusion;

    vec4 v0 = p_to_s(vec4(position.xy - (line / linewidth) * 0.01, position.zw));
    vec4 v1 = p_to_s(vec4(position.xy + normal.xy,             position.zw));
    vec4 v2 = p_to_s(vec4(position.xy + normal.xy + extrusion, position.zw));
    vec4 v3 = p_to_s(vec4(position.xy - normal.xy + extrusion, position.zw));
    vec4 v4 = p_to_s(vec4(position.xy - normal.xy,             position.zw));

    vec3 p0 = vec3(      0,          0, linewidth);
    vec3 p1 = vec3(      0,  linewidth, linewidth);
    vec3 p2 = vec3(      0, -linewidth, linewidth);
    vec3 p3 = vec3(      0,          0,   ext_len);
    vec3 p4 = vec3(ext_len,          0,   ext_len);

    /* connector */
    emit_vertex(v1, p1, color);
    emit_vertex(v4, p2, color);
    EndPrimitive();

    /* top */
    emit_vertex(v0, p0, color);
    emit_vertex(v2, p1, color);
    emit_vertex(v1, p1, color);
    EndPrimitive();

    /* center */
    emit_vertex(v0, p3, color);
    emit_vertex(v3, p4, color);
    emit_vertex(v2, p4, color);
    EndPrimitive();

    /* bottom */
    emit_vertex(v0, p0, color);
    emit_vertex(v4, p2, color);
    emit_vertex(v3, p2, color);
}

void emit_round_cap_a(vec4 position, vec2 normal, float linewidth, vec4 color) {
    vec2 extrusion = vec2(-normal.y, normal.x);

    vec4 v1 = p_to_s(vec4(position.xy + normal.xy,             position.zw));
    vec4 v2 = p_to_s(vec4(position.xy + normal.xy + extrusion, position.zw));
    vec4 v3 = p_to_s(vec4(position.xy - normal.xy + extrusion, position.zw));
    vec4 v4 = p_to_s(vec4(position.xy - normal.xy,             position.zw));

    /* cap & connector */
    emit_vertex(v2, vec3(-linewidth,  linewidth, linewidth), color);
    emit_vertex(v3, vec3(-linewidth, -linewidth, linewidth), color);
    emit_vertex(v1, vec3(         0,  linewidth, linewidth), color);
    emit_vertex(v4, vec3(         0, -linewidth, linewidth), color);
}

void emit_round_cap_b(vec4 position, vec2 normal, float linewidth, vec4 color) {
    vec2 extrusion = -vec2(-normal.y, normal.x);

    vec4 v1 = p_to_s(vec4(position.xy + normal.xy,             position.zw));
    vec4 v2 = p_to_s(vec4(position.xy + normal.xy + extrusion, position.zw));
    vec4 v3 = p_to_s(vec4(position.xy - normal.xy + extrusion, position.zw));
    vec4 v4 = p_to_s(vec4(position.xy - normal.xy,             position.zw));

    /* connector & cap */
    emit_vertex(v1, vec3(        0,  linewidth, linewidth), color);
    emit_vertex(v4, vec3(        0, -linewidth, linewidth), color);
    emit_vertex(v2, vec3(linewidth,  linewidth, linewidth), color);
    emit_vertex(v3, vec3(linewidth, -linewidth, linewidth), color);
}

void emit_cap_a(int type, vec4 position, vec2 normal, float linewidth, vec4 color) {
    switch(type) {
    case CAP_TYPE_BUTT:
    default:
        emit_none(position, normal, linewidth, color);
        break;

    case CAP_TYPE_SQUARE:
        emit_square_cap_a(position, normal, linewidth, color);
        break;

    case CAP_TYPE_ROUND:
        emit_round_cap_a(position, normal, linewidth, color);
        break;

    }
}

void emit_cap_b(int type, vec4 position, vec2 normal, float linewidth, vec4 color) {
    switch(type) {
    case CAP_TYPE_BUTT:
    default:
        emit_none(position, normal, linewidth, color);
        break;

    case CAP_TYPE_SQUARE:
        emit_square_cap_b(position, normal, linewidth, color);
        break;

    case CAP_TYPE_ROUND:
        emit_round_cap_b(position, normal, linewidth, color);
        break;

    }
}


/* -- joins ------------------------------------------------------------------------------------- */

void emit_miter_join_a(vec4 position, vec2 normal, vec2 line, float linewidth, vec4 color) {
    /* basics */
    vec2 line_adj = position.xy - s_to_p(gl_in[0].gl_Position).xy;
    vec2 normal_adj = normalize(vec2(-line_adj.y, line_adj.x)) * linewidth;
    float a = cross(normal, normal_adj);
    float s = sign(a);

    /* simple join or cap if lines align perfectly */
    if (abs(a) < 0.1) {
        if (dot(normal, normal_adj) > 0)
            emit_none(position, normal, linewidth, color);
        else
            emit_cap_a(get_sane_cap_type(linewidth), position, normal, linewidth, color);
        return;
    }

    /* angle and extrusion */
    vec2 median_normal = normalize(normal + normal_adj);
    float angle_s = cross(median_normal, vec2(-normal.y, normal.x) / linewidth);
    float e_len = linewidth / angle_s;

    bool intersecting = isnan(e_len)
			|| e_len * e_len > length(line) * length(line) + linewidth * linewidth;

    /* line parameters */
    vec2 e_inner = -median_normal * e_len;
    vec2 e_outer;
    vec2 e_center;

    vec3 p_center;
    vec3 p0 = vec3(0,  s * linewidth, linewidth);
    vec3 p1 = vec3(0, -s * linewidth, linewidth);

	/* bevel join if angle is too small, miter otherwise */
    if (angle_s < MITER_ANGLE_LIMIT) {
    	float minline = min(length(line), length(line_adj));
        e_center = e_inner * angle_s * min(minline / (2 * linewidth), 1);
        e_outer = median_normal * linewidth * angle_s;
        p_center = vec3(0, -s * linewidth * angle_s, linewidth);
    } else {
        e_center = vec2(0, 0);
        e_outer = -e_inner;
        p_center = vec3(0, 0, linewidth);
    }

	/* slight overlap to remove fp-precision caused artifacts */
	e_center -= normal * 0.1 / linewidth;

    /* vertices */
    vec4 v0 = p_to_s(vec4(position.xy + s * e_outer,  position.zw));
    vec4 v1 = p_to_s(vec4(position.xy + s * e_inner,  position.zw));
    vec4 v2 = p_to_s(vec4(position.xy + s * normal,   position.zw));
    vec4 v3 = p_to_s(vec4(position.xy + s * e_center, position.zw));

	/* draw */
    if (s < 0) {
        emit_vertex(v2, p0,       color);			// outer join
        emit_vertex(v3, p_center, color);
        emit_vertex(v0, p0,       color);
        EndPrimitive();

        if (!intersecting) {
            emit_vertex(v3, p_center, color);		// inner join
            emit_vertex(v2, p0,       color);
            emit_vertex(v1, p1,       color);
            EndPrimitive();

            emit_vertex(v1, p1, color);				// connector
            emit_vertex(v2, p0, color);
        } else {
            emit_vertex(v3, p_center, color);		// connector
            emit_vertex(v2, p0,       color);
        }
    } else {
        emit_vertex(v2, p0,       color);			// outer join
        emit_vertex(v0, p0,       color);
        emit_vertex(v3, p_center, color);
        EndPrimitive();

        if (!intersecting) {
            emit_vertex(v2, p0,       color);		// inner join
            emit_vertex(v3, p_center, color);
            emit_vertex(v1, p1,       color);
            EndPrimitive();

            emit_vertex(v2, p0, color);				// connector
            emit_vertex(v1, p1, color);
        } else {
            emit_vertex(v2, p0,       color);		// connector
            emit_vertex(v3, p_center, color);
        }
    }
}

void emit_miter_join_b(vec4 position, vec2 normal, vec2 line, float linewidth, vec4 color) {
    /* basics */
    vec2 line_adj = s_to_p(gl_in[3].gl_Position).xy - position.xy;
    vec2 normal_adj = normalize(vec2(-line_adj.y, line_adj.x)) * linewidth;
    float a = cross(-normal, normal_adj);
    float s = sign(a);

    /* simple join or cap if lines align perfectly */
    if (abs(a) < 0.1) {
        if (dot(normal, normal_adj) > 0)
            emit_none(position, normal, linewidth, color);
        else
            emit_cap_b(get_sane_cap_type(linewidth), position, normal, linewidth, color);
        return;
    }

    /* angle and extrusion */
    vec2 median_normal = normalize(normal + normal_adj);
    float angle_s = cross(median_normal, vec2(-normal.y, normal.x) / linewidth);
    float e_len = linewidth / angle_s;

    bool intersecting = isnan(e_len)
			|| e_len * e_len > length(line) * length(line) + linewidth * linewidth;

    /* line parameters */
    vec2 e_inner = -median_normal * e_len;
    vec2 e_outer;
    vec2 e_center;

    vec3 p_center;
    vec3 p0 = vec3(0,  s * linewidth, linewidth);
    vec3 p1 = vec3(0, -s * linewidth, linewidth);

	/* bevel join if angle is too small, miter otherwise */
    if (angle_s < MITER_ANGLE_LIMIT) {
    	float minline = min(length(line), length(line_adj));
        e_center = e_inner * angle_s * min(minline / (2 * linewidth), 1);
        e_outer = median_normal * linewidth * angle_s;
        p_center = vec3(0, -s * linewidth * angle_s, linewidth);
    } else {
        e_center = vec2(0, 0);
        e_outer = -e_inner;
        p_center = vec3(0, 0, linewidth);
    }

	/* slight overlap to remove fp-precision-caused artifacts */
	e_center -= normal * 0.1 / linewidth;

    /* vertices */
    vec4 v0 = p_to_s(vec4(position.xy + s * e_outer,  position.zw));
    vec4 v1 = p_to_s(vec4(position.xy + s * e_inner,  position.zw));
    vec4 v2 = p_to_s(vec4(position.xy + s * normal,   position.zw));
    vec4 v3 = p_to_s(vec4(position.xy + s * e_center, position.zw));

    if (s < 0) {
        if (!intersecting) {
            emit_vertex(v1, p1, color);				// connector & inner join
            emit_vertex(v2, p0, color);
            emit_vertex(v3, p_center, color);

            EndPrimitive();
            emit_vertex(v3, p_center, color);		// outer join
            emit_vertex(v2, p0,       color);
            emit_vertex(v0, p0,       color);
        } else {
            emit_vertex(v3, p_center, color);		// connector & outer join
            emit_vertex(v2, p0,       color);
            emit_vertex(v0, p0,       color);
        }
    } else {
        if (!intersecting) {
            emit_vertex(v2, p0, color);				// connector & inner join
            emit_vertex(v1, p1, color);
            emit_vertex(v3, p_center, color);

            EndPrimitive();
            emit_vertex(v2, p0,       color);		// outer join
            emit_vertex(v3, p_center, color);
            emit_vertex(v0, p0,       color);
        } else {
            emit_vertex(v2, p0,       color);		// connector & outer join
            emit_vertex(v3, p_center, color);
            emit_vertex(v0, p0,       color);
        }
    }
}

void emit_bevel_join_a(vec4 position, vec2 normal, vec2 line, float linewidth, vec4 color) {
    /* basics */
    vec2 line_adj = position.xy - s_to_p(gl_in[0].gl_Position).xy;
    vec2 normal_adj = normalize(vec2(-line_adj.y, line_adj.x)) * linewidth;
    float a = cross(normal, normal_adj);
    float s = sign(a);

    /* simple join or cap if lines align perfectly */
    if (abs(a) < 0.1) {
        if (dot(normal, normal_adj) > 0)
            emit_none(position, normal, linewidth, color);
        else
            emit_cap_a(get_sane_cap_type(linewidth), position, normal, linewidth, color);
        return;
    }

    /* angle and extrusion */
    vec2 median_normal = normalize(normal + normal_adj);
    float angle_s = cross(median_normal, vec2(-normal.y, normal.x) / linewidth);
    float e_len = linewidth / angle_s;

    bool intersecting = isnan(e_len)
			|| e_len * e_len > length(line) * length(line) + linewidth * linewidth;

    /* extrusion vectors */
    vec2 e_inner = -median_normal * e_len;
    vec2 e_outer =  median_normal * linewidth * angle_s;
    vec2 e_center = e_inner * angle_s
            * min(min(length(line), length(line_adj)) / (2 * linewidth), 1)
            - normal * 0.1 / linewidth;

    /* parameters */
    vec3 p_center = vec3(0, -s * linewidth * angle_s, linewidth);
    vec3 p0 = vec3(0,  s * linewidth, linewidth);
    vec3 p1 = vec3(0, -s * linewidth, linewidth);

    /* vertices */
    vec4 v0 = p_to_s(vec4(position.xy + s * e_outer,  position.zw));
    vec4 v1 = p_to_s(vec4(position.xy + s * e_inner,  position.zw));
    vec4 v2 = p_to_s(vec4(position.xy + s * normal,   position.zw));
    vec4 v3 = p_to_s(vec4(position.xy + s * e_center, position.zw));

	/* draw */
    if (s < 0) {
        emit_vertex(v2, p0,       color);			// outer join
        emit_vertex(v3, p_center, color);
        emit_vertex(v0, p0,       color);
        EndPrimitive();

        if (!intersecting) {
            emit_vertex(v3, p_center, color);		// inner join
            emit_vertex(v2, p0,       color);
            emit_vertex(v1, p1,       color);
            EndPrimitive();

            emit_vertex(v1, p1, color);				// connector
            emit_vertex(v2, p0, color);
        } else {
            emit_vertex(v3, p_center, color);		// connector
            emit_vertex(v2, p0,       color);
        }
    } else {
        emit_vertex(v2, p0,       color);			// outer join
        emit_vertex(v0, p0,       color);
        emit_vertex(v3, p_center, color);
        EndPrimitive();

        if (!intersecting) {
            emit_vertex(v2, p0,       color);		// inner join
            emit_vertex(v3, p_center, color);
            emit_vertex(v1, p1,       color);
            EndPrimitive();

            emit_vertex(v2, p0, color);				// connector
            emit_vertex(v1, p1, color);
        } else {
            emit_vertex(v2, p0,       color);		// connector
            emit_vertex(v3, p_center, color);
        }
    }
}

void emit_bevel_join_b(vec4 position, vec2 normal, vec2 line, float linewidth, vec4 color) {
    /* basics */
    vec2 line_adj = s_to_p(gl_in[3].gl_Position).xy - position.xy;
    vec2 normal_adj = normalize(vec2(-line_adj.y, line_adj.x)) * linewidth;
    float a = cross(-normal, normal_adj);
    float s = sign(a);

    /* simple join or cap if lines align perfectly */
    if (abs(a) < 0.1) {
        if (dot(normal, normal_adj) > 0)
            emit_none(position, normal, linewidth, color);
        else
            emit_cap_b(get_sane_cap_type(linewidth), position, normal, linewidth, color);
        return;
    }

    /* angle and extrusion */
    vec2 median_normal = normalize(normal + normal_adj);
    float angle_s = cross(median_normal, vec2(-normal.y, normal.x) / linewidth);
    float e_len = linewidth / angle_s;

    bool intersecting = isnan(e_len)
			|| e_len * e_len > length(line) * length(line) + linewidth * linewidth;

    /* extrusion vectors */
    vec2 e_inner = -median_normal * e_len;
    vec2 e_outer =  median_normal * linewidth * angle_s;
    vec2 e_center = e_inner * angle_s
            * min(min(length(line), length(line_adj)) / (2 * linewidth), 1)
            - normal * 0.1 / linewidth;

    /* parameters */
    vec3 p_center = vec3(0, -s * linewidth * angle_s, linewidth);
    vec3 p0 = vec3(0,  s * linewidth, linewidth);
    vec3 p1 = vec3(0, -s * linewidth, linewidth);

    /* vertices */
    vec4 v0 = p_to_s(vec4(position.xy + s * e_outer,  position.zw));
    vec4 v1 = p_to_s(vec4(position.xy + s * e_inner,  position.zw));
    vec4 v2 = p_to_s(vec4(position.xy + s * normal,   position.zw));
    vec4 v3 = p_to_s(vec4(position.xy + s * e_center, position.zw));

    /* draw */
    if (s < 0) {
        if (!intersecting) {
            emit_vertex(v1, p1, color);				// connector & inner join
            emit_vertex(v2, p0, color);
            emit_vertex(v3, p_center, color);

            EndPrimitive();
            emit_vertex(v3, p_center, color);
            emit_vertex(v2, p0,       color);		// outer join
            emit_vertex(v0, p0,       color);
        } else {
            emit_vertex(v3, p_center, color);		// connector & outer join
            emit_vertex(v2, p0,       color);
            emit_vertex(v0, p0,       color);
        }
    } else {
        if (!intersecting) {
            emit_vertex(v2, p0, color);				// connector & inner join
            emit_vertex(v1, p1, color);
            emit_vertex(v3, p_center, color);

            EndPrimitive();
            emit_vertex(v2, p0,       color);		// outer join
            emit_vertex(v3, p_center, color);
            emit_vertex(v0, p0,       color);
        } else {
            emit_vertex(v2, p0,       color);		// connector & outer join
            emit_vertex(v3, p_center, color);
            emit_vertex(v0, p0,       color);
        }
    }
}

void emit_round_join_a(vec4 position, vec2 normal, vec2 line, float linewidth, vec4 color) {
    /* basics */
    vec2 line_adj = position.xy - s_to_p(gl_in[0].gl_Position).xy;
    vec2 normal_adj = normalize(vec2(-line_adj.y, line_adj.x)) * linewidth;
    float a = cross(normal, normal_adj);
    float s = sign(a);

    /* simple join or cap if lines align perfectly */
    if (abs(a) < 0.1) {
        if (dot(normal, normal_adj) > 0)
            emit_none(position, normal, linewidth, color);
        else
            emit_cap_a(get_sane_cap_type(linewidth), position, normal, linewidth, color);
        return;
    }

    /* angle and extrusion */
    vec2 median_normal = normalize(normal + normal_adj);
    float angle_s = cross(median_normal, vec2(-normal.y, normal.x) / linewidth);
    float angle_c =   dot(median_normal, vec2(-normal.y, normal.x) / linewidth);
    float e_len = linewidth / angle_s;

    bool intersecting = isnan(e_len)
			|| e_len * e_len > length(line) * length(line) + linewidth * linewidth;

    bool miter = 1 < 2 * angle_s;

    /* extrusion vectors */
    vec2 e_inner = -median_normal * e_len;

    /* parameters */
    vec3 p0 = vec3(0,          0, linewidth);
    vec3 p1 = vec3(0,  linewidth, linewidth);
    vec3 p2 = vec3(0, -linewidth, linewidth);

    /* vertices */
    vec4 v0 = p_to_s(vec4(position.xy - s * normal * 0.1 / linewidth, position.zw));
    vec4 v1 = p_to_s(vec4(position.xy + s * e_inner, position.zw));
    vec4 v2 = p_to_s(vec4(position.xy + s * normal,  position.zw));

    if (s < 0) {
        if (miter) {                        // outer join
            vec4 vm = p_to_s(vec4(position.xy - s * e_inner, position.zw));
            vec3 pm = vec3(-e_len * vec2(angle_c, angle_s), linewidth);

            emit_vertex(v0, p0, color);
            emit_vertex(vm, pm, color);
            emit_vertex(v2, p2, color);
        } else {
            vec2 l = normalize(line) * linewidth;
            mat2 r = mat2(angle_c, angle_s, -angle_s, angle_c);
            vec4 vo = p_to_s(vec4(position.xy - normal - l, position.zw));
            vec4 vm = p_to_s(vec4(position.xy + 1.5 * l * r, position.zw));
            vec3 po = vec3(linewidth, -linewidth, linewidth);
            vec3 pm = vec3(-1.5 * linewidth * vec2(angle_c, angle_s), linewidth);

            emit_vertex(v2, p2, color);
            emit_vertex(v0, p0, color);
            emit_vertex(vo, po, color);
            emit_vertex(vm, pm, color);
        }
        EndPrimitive();

        if (!intersecting) {
            emit_vertex(v0, p0, color);     // inner join
            emit_vertex(v2, p2, color);
            emit_vertex(v1, p1, color);
            EndPrimitive();

            emit_vertex(v1, p1, color);     // connector
            emit_vertex(v2, p2, color);
        } else {
            emit_vertex(v0, p0, color);     // connector
            emit_vertex(v2, p2, color);
        }

    } else {
        if (miter) {                        // outer join
            vec4 vm = p_to_s(vec4(position.xy - s * e_inner, position.zw));
            vec3 pm = vec3(e_len * vec2(angle_c, angle_s), linewidth);

            emit_vertex(v0, p0, color);
            emit_vertex(v2, p1, color);
            emit_vertex(vm, pm, color);
        } else {
            vec2 l = normalize(line) * linewidth;
            mat2 r = -mat2(angle_c, angle_s, -angle_s, angle_c);
            vec4 vo = p_to_s(vec4(position.xy + normal - l, position.zw));
            vec4 vm = p_to_s(vec4(position.xy + 1.5 * l * r, position.zw));
            vec3 po = vec3(linewidth, linewidth, linewidth);
            vec3 pm = vec3(1.5 * linewidth * vec2(angle_c, angle_s), linewidth);

            emit_vertex(v2, p1, color);
            emit_vertex(vo, po, color);
            emit_vertex(v0, p0, color);
            emit_vertex(vm, pm, color);
        }
        EndPrimitive();

        if (!intersecting) {
            emit_vertex(v0, p0, color);     // inner join
            emit_vertex(v1, p2, color);
            emit_vertex(v2, p1, color);
            EndPrimitive();

            emit_vertex(v2, p1, color);     // connector
            emit_vertex(v1, p2, color);
        } else {
            emit_vertex(v2, p1, color);     // connector
            emit_vertex(v0, p0, color);
        }
    }
}

void emit_round_join_b(vec4 position, vec2 normal, vec2 line, float linewidth, vec4 color) {
    /* basics */
    vec2 line_adj = s_to_p(gl_in[3].gl_Position).xy - position.xy;
    vec2 normal_adj = normalize(vec2(-line_adj.y, line_adj.x)) * linewidth;
    float a = cross(-normal, normal_adj);
    float s = sign(a);

    /* simple join or cap if lines align perfectly */
    if (abs(a) < 0.1) {
        if (dot(normal, normal_adj) > 0)
            emit_none(position, normal, linewidth, color);
        else
            emit_cap_b(get_sane_cap_type(linewidth), position, normal, linewidth, color);
        return;
    }

    /* angle and extrusion */
    vec2 median_normal = normalize(normal + normal_adj);
    float angle_s = cross(median_normal, vec2(-normal.y, normal.x) / linewidth);
    float angle_c =   dot(median_normal, vec2(-normal.y, normal.x) / linewidth);
    float e_len = linewidth / angle_s;

    bool intersecting = isnan(e_len)
			|| e_len * e_len > length(line) * length(line) + linewidth * linewidth;

    bool miter = 1 < 2 * angle_s;

    /* extrusion vectors */
    vec2 e_inner = -median_normal * e_len;

    /* parameters */
    vec3 p0 = vec3(0,          0, linewidth);
    vec3 p1 = vec3(0,  linewidth, linewidth);
    vec3 p2 = vec3(0, -linewidth, linewidth);

    /* vertices */
    vec4 v0 = p_to_s(vec4(position.xy - s * normal * 0.1 / linewidth, position.zw));
    vec4 v1 = p_to_s(vec4(position.xy + s * e_inner, position.zw));
    vec4 v2 = p_to_s(vec4(position.xy + s * normal,  position.zw));

    if (s < 0) {
        if (!intersecting) {
            emit_vertex(v1, p1, color);     // connector & inner join
            emit_vertex(v2, p2, color);
            emit_vertex(v0, p0, color);
        } else {
            emit_vertex(v0, p0, color);     // connector
            emit_vertex(v2, p2, color);
        }
        EndPrimitive();

        if (miter) {                        // outer join
            vec4 vm = p_to_s(vec4(position.xy + e_inner, position.zw));
            vec3 pm = vec3(-e_len * vec2(angle_c, angle_s), linewidth);

            emit_vertex(v0, p0, color);
            emit_vertex(v2, p2, color);
            emit_vertex(vm, pm, color);
        } else {
            vec2 l = normalize(line) * linewidth;
            mat2 r = mat2(angle_c, angle_s, -angle_s, angle_c);
            vec4 vo = p_to_s(vec4(position.xy - normal + l, position.zw));
            vec4 vm = p_to_s(vec4(position.xy + 1.5 * l * r, position.zw));
            vec3 po = vec3(-linewidth, -linewidth, linewidth);
            vec3 pm = vec3(-1.5 * linewidth * vec2(angle_c, angle_s), linewidth);

            emit_vertex(v2, p2, color);
            emit_vertex(vo, po, color);
            emit_vertex(v0, p0, color);
            emit_vertex(vm, pm, color);
        }

    } else {
        if (!intersecting) {
            emit_vertex(v2, p1, color);     // connector & inner join
            emit_vertex(v1, p2, color);
            emit_vertex(v0, p0, color);
        } else {
            emit_vertex(v2, p1, color);     // connector
            emit_vertex(v0, p0, color);
        }
        EndPrimitive();

        if (miter) {                        // outer join
            vec4 vm = p_to_s(vec4(position.xy - e_inner, position.zw));
            vec3 pm = vec3(e_len * vec2(angle_c, angle_s), linewidth);

            emit_vertex(v0, p0, color);
            emit_vertex(vm, pm, color);
            emit_vertex(v2, p1, color);
        } else {
            vec2 l = normalize(line) * linewidth;
            mat2 r = -mat2(angle_c, angle_s, -angle_s, angle_c);
            vec4 vo = p_to_s(vec4(position.xy + normal + l, position.zw));
            vec4 vm = p_to_s(vec4(position.xy + 1.5 * l * r, position.zw));
            vec3 po = vec3(-linewidth, linewidth, linewidth);
            vec3 pm = vec3(1.5 * linewidth * vec2(angle_c, angle_s), linewidth);

            emit_vertex(v2, p1, color);
            emit_vertex(v0, p0, color);
            emit_vertex(vo, po, color);
            emit_vertex(vm, pm, color);
        }
    }
}

void emit_join_a(int type, vec4 position, vec2 normal, vec2 line, float linewidth, vec4 color) {
    switch (type) {
    case JOIN_TYPE_NONE:
    default:
        emit_none(position, normal, linewidth, color);
        break;

    case JOIN_TYPE_MITER:
        emit_miter_join_a(position, normal, line, linewidth, color);
        break;

    case JOIN_TYPE_BEVEL:
        emit_bevel_join_a(position, normal, line, linewidth, color);
        break;

    case JOIN_TYPE_ROUND:
        emit_round_join_a(position, normal, line, linewidth, color);
        break;
    }
}

void emit_join_b(int type, vec4 position, vec2 normal, vec2 line, float linewidth, vec4 color) {
    switch (type) {
    case JOIN_TYPE_NONE:
    default:
        emit_none(position, normal, linewidth, color);
        break;

    case JOIN_TYPE_MITER:
        emit_miter_join_b(position, normal, line, linewidth, color);
        break;

    case JOIN_TYPE_BEVEL:
        emit_bevel_join_b(position, normal, line, linewidth, color);
        break;

    case JOIN_TYPE_ROUND:
        emit_round_join_b(position, normal, line, linewidth, color);
        break;
    }
}


/* -- main -------------------------------------------------------------------------------------- */

void main() {
    /* calculate the actual pixel-space line width */
    float linewidth = u_linewidth
            * (u_viewscale * u_viewscale_norm + u_zoom_correction)
            / (1 + u_zoom_correction);

    /* switch to butt-cap/none-join if line-width gets small enough */
    int cap_type = get_sane_cap_type(linewidth);
    int join_type = get_sane_join_type(linewidth);

    vec4 p1 = s_to_p(gl_in[1].gl_Position);
    vec4 p2 = s_to_p(gl_in[2].gl_Position);

    vec2 line = p2.xy - p1.xy;
    vec2 normal = normalize(vec2(-line.y, line.x)) * linewidth;

    /* draw line start */
    if (gl_in[0].gl_Position.xy == gl_in[1].gl_Position.xy) {           // cap
        emit_cap_a(cap_type, p1, normal, linewidth, vertex_color_gs[1]);
    } else {                                                            // join
        emit_join_a(join_type, p1, normal, line, linewidth, vertex_color_gs[1]);
    }

    /* draw line end */
    if (gl_in[2].gl_Position.xy == gl_in[3].gl_Position.xy) {           // cap
        emit_cap_b(cap_type, p2, normal, linewidth, vertex_color_gs[2]);
    } else {                                                            // join
        emit_join_b(join_type, p2, normal, line, linewidth, vertex_color_gs[2]);
    }
}
