#version 150

/* -- style limits ------------------------------------------------------------------------------ */
#define LINE_WIDTH_LIMIT_CAP    1.5
#define LINE_WIDTH_LIMIT_JOIN   1.5


/* -- shader inputs ----------------------------------------------------------------------------- */
layout(lines_adjacency) in;
layout(triangle_strip, max_vertices = 16) out;

in vec4[] vertex_color_gs;
out vec4  vertex_color;
out vec3  vertex_line;

uniform vec4  u_viewport;
uniform float u_viewscale;
uniform float u_viewscale_norm;
uniform float u_zoom_correction =       0.03;

uniform float u_linewidth;


/* -- misc -------------------------------------------------------------------------------------- */

/* vec2 cross function */
float cross(vec2 a, vec2 b) { return a.x * b.y - a.y * b.x; }

/* screen-space pixel-space tranformations */
vec4 s_to_p(vec4 v) { return vec4(v.xy * u_viewport.xy, v.zw); }
vec4 p_to_s(vec4 v) { return vec4(v.xy * u_viewport.zw, v.zw); }

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


/* -- joins ------------------------------------------------------------------------------------- */

void emit_round_join_a(vec4 position, vec2 normal, vec2 line, float linewidth, vec4 color) {
    /* basics */
    vec2 line_adj = position.xy - s_to_p(gl_in[0].gl_Position).xy;
    vec2 normal_adj = normalize(vec2(-line_adj.y, line_adj.x)) * linewidth;
    float a = cross(normal, normal_adj);
    float s = sign(a);

    /* simple join or cap if lines align perfectly */
    if (abs(a) < 0.1) {
        if (dot(normal, normal_adj) > 0 || linewidth < LINE_WIDTH_LIMIT_CAP)
            emit_none(position, normal, linewidth, color);
        else
            emit_round_cap_a(position, normal, linewidth, color);
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
        if (dot(normal, normal_adj) > 0 || linewidth < LINE_WIDTH_LIMIT_CAP)
            emit_none(position, normal, linewidth, color);
        else
            emit_round_cap_b(position, normal, linewidth, color);
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


/* -- main -------------------------------------------------------------------------------------- */

void main() {
    /* calculate the actual pixel-space line width */
    float linewidth = u_linewidth
            * (u_viewscale * u_viewscale_norm + u_zoom_correction)
            / (1 + u_zoom_correction);

    vec4 p1 = s_to_p(gl_in[1].gl_Position);
    vec4 p2 = s_to_p(gl_in[2].gl_Position);

    vec2 line = p2.xy - p1.xy;
    vec2 normal = normalize(vec2(-line.y, line.x)) * linewidth;

    /* draw line start */
    if (gl_in[0].gl_Position.xy == gl_in[1].gl_Position.xy) {           // cap
        if (linewidth > LINE_WIDTH_LIMIT_CAP)
            emit_round_cap_a(p1, normal, linewidth, vertex_color_gs[1]);
        else
            emit_none(p1, normal, linewidth, vertex_color_gs[1]);
    } else {                                                            // join
        if (linewidth > LINE_WIDTH_LIMIT_JOIN)
            emit_round_join_a(p1, normal, line, linewidth, vertex_color_gs[1]);
        else
            emit_none(p1, normal, linewidth, vertex_color_gs[1]);
    }

    /* draw line end */
    if (gl_in[2].gl_Position.xy == gl_in[3].gl_Position.xy) {           // cap
        if (linewidth > LINE_WIDTH_LIMIT_CAP)
            emit_round_cap_b(p2, normal, linewidth, vertex_color_gs[2]);
        else
            emit_none(p2, normal, linewidth, vertex_color_gs[2]);
    } else {                                                            // join
        if (linewidth > LINE_WIDTH_LIMIT_JOIN)
            emit_round_join_b(p2, normal, line, linewidth, vertex_color_gs[2]);
        else
            emit_none(p2, normal, linewidth, vertex_color_gs[2]);
    }
}
