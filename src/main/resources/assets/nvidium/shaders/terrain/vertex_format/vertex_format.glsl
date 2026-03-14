#define COLOR_SCALE        1.0 / 255.0

#ifdef USE_SODIUM_VERTEX_FORMAT
#import <nvidium:terrain/vertex_format/sodium_vertex_format.glsl>
#else
#import <nvidium:terrain/vertex_format/nvidium_vertex_format.glsl>
#endif

float getVertexAlphaCutoff(uint v) {
    return (float[](0.1f, 0.2f,0.2f,1.0f))[v];
}

vec4 sampleLight(vec2 uv) {
    //Its divided by 16 to match sodium/vanilla (it can never be 1 which is funny)
    return vec4(texture(tex_light, clamp(uv, vec2(0.5 / 16.0), vec2(15.5 / 16.0))).rgb, 1);
}

vec3 computeMultiplier(Vertex V) {
    vec4 tint = decodeVertexColour(V);
    tint *= sampleLight(decodeLightUV(V));
    tint *= tint.w;
    return tint.xyz;
}
