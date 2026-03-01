const uint POSITION_BITS        = 16u;
const uint POSITION_MAX_COORD   = 1u << POSITION_BITS;
const uint POSITION_MAX_VALUE   = POSITION_MAX_COORD - 1u;

const uint TEXTURE_BITS         = 15u;
const uint TEXTURE_MAX_COORD    = 1u << TEXTURE_BITS;
const uint TEXTURE_MAX_VALUE    = TEXTURE_MAX_COORD - 1u;

const float VERTEX_SCALE = 32.0 / float(POSITION_MAX_COORD);
const float VERTEX_OFFSET = -8.0;

uvec3 _deinterleave_u20x3(Vertex v) {
    return uvec3(v.posa,v.posa >> 16u, v.posb) & 0xFFFFu;
}

vec3 decodeVertexPosition(Vertex v) {
    return (_deinterleave_u20x3(v) * VERTEX_SCALE) + VERTEX_OFFSET;
}

vec2 decodeVertexRawUV(Vertex v) {
    return vec2(v.uv & 0xFFFFu, v.uv >> 16u) / float(TEXTURE_MAX_COORD);
}

vec2 decodeVertexUVBias(Vertex v) {
    return mix(vec2(-1.0), vec2(1.0), bvec2( uvec2(v.uv & 0xFFFFu, v.uv >> 16u) >> TEXTURE_BITS));
}

vec2 decodeVertexUV(Vertex v) {
    return (decodeVertexUVBias(v) * 0) + decodeVertexRawUV(v); //I don't know what the bias is for, but I am disabling it for now.
}

vec2 decodeLightUV(Vertex v) {
    return vec2(v.light & 0xFFFFu, v.light >> 16u)/256.0;
}

bool hasMipping(Vertex v) {
    return bool(int(v.posb >> 16u) & 1);
}

uint rawVertexAlphaCutoff(Vertex v) {
    return (int(v.posb >> 16u) >> 1) & 3;
}

vec4 decodeVertexColour(Vertex v) {
    uvec3 packed_color = (uvec3(v.color) >> uvec3(0, 8, 16)) & uvec3(0xFFu);
    return vec4(vec3(packed_color) * COLOR_SCALE, 1);
}
