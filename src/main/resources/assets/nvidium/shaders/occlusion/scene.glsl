#ifdef USE_SODIUM_VERTEX_FORMAT
struct Vertex {
    uint posa;
    uint posb;
    uint color;
    uint uv;
    uint light;
};
#else
#define Vertex uvec4
#endif

// this is cause in the section rasterizer you get less cache misses thus higher throughput
struct Section {
    ivec4 header;
    //Header.x -> 0-3=offsetx 4-7=sizex 8-31=chunk x
    //Header.y -> 0-3=offsetz 4-7=sizez 8-31=chunk z
    //Header.z -> 0-3=offsety 4-7=sizey 8-15=chunk y
    //Header.w -> quad offset
    ivec4 renderRanges;
    int   translucencyDataIdx;
};

struct Region {
    uvec2 a;  // a.x = lower 32 bits, a.y = upper 32 bits
    uvec2 b;
};

ivec3 unpackRegionSize(Region region) {
    // bits 56-58 = size.z, 59-61 = size.x, 62-63 = size.y, all in a.y (upper 32)
    return ivec3((region.a.y >> 27u) & 7u,   // bits 59-61
    (region.a.y >> 30u) & 3u,   // bits 62-63
    (region.a.y >> 24u) & 7u);  // bits 56-58
}

int unpackRegionCount(Region region) {
    // bits 48-55 = count, in a.y bits 16-23
    return int((region.a.y >> 16u) & 0xFFu);
}

ivec3 unpackRegionPosition(Region region) {
    // y: bits 0-23 of a → a.x bits 0-23
    int y = (int(region.a.x) << 8) >> 8;

    // x: bits 24-47 of a → a.x bits 24-31 + a.y bits 0-15
    int x = int((region.a.x >> 24u) | ((region.a.y & 0xFFFFu) << 8u));
    x = (x << 8) >> 8;

    // z: bits 40-63 of b → b.x bits 8-31 + ... wait
    // z is at bits 40-63 of b, shifted left by 40 in the long
    // b.x = bits 0-31 of b, b.y = bits 32-63 of b
    // bits 40-63 of b = b.x bits 8-31 (bits 40-63 relative to b start...
    // bit 40 of b = bit 8 of b.x? no...
    // b.x = bits 0-31, b.y = bits 32-63
    // bit 40 of b = bit 8 of b.y
    // z occupies bits 40-63 of b = b.y bits 8-31
    int z = int(region.b.y >> 8u);
    z = (z << 8) >> 8;

    return ivec3(x, y, z);
}

uint unpackRegionTransformId(Region region) {
    // bits 30-39 of b
    // bit 30 of b = bit 30 of b.x (since b.x = bits 0-31)
    // bit 39 of b = bit 7 of b.y
    return ((region.b.x >> 30u) | ((region.b.y & 0xFFu) << 2u)) & 0x3FFu;
}

bool sectionEmpty(ivec4 header) {
    header.y &= ~0x1FF<<17;
    return header == ivec4(0);
}


layout(std430, binding=0) readonly restrict buffer SceneData {
    //Need to basicly go in order of alignment
    //align(16)
    mat4 MVP;
    #ifdef RENDER_FOG
    mat4 MVPInv;
    #endif
    ivec4 chunkPosition;
    vec4 subchunkOffset;
    vec4 fogColour;

    //vec4  subChunkPosition;//The subChunkTranslation is already done inside the MVP
    //align(8)
    //Terrain command buffer, the first 4 bytes are actually the count



    //TODO: possibly make this a uniform instead of a buffer, but it might get quite large is the issue
    //readonly restrict u64vec4 *terrainData;
    //uvec4 *terrainData;


    vec2 screenSize;
    vec2 texCoordShrink;

    float fogStart;
    float fogEnd;
    bool isCylindricalFog;

    uint flags;

    //align(2)
    uint regionCount;//Number of regions in regionIndicies
    //align(1)
    uint frameId;
};
layout(std430, binding=1) readonly restrict buffer RegionIndicies {
    uint16_t regionIndicies[];
};
layout(std430, binding=2) readonly restrict buffer RegionData {
    Region regionData[];
};
layout(std430, binding=3) restrict buffer SectionData {
    Section sectionData[];
};
layout(std430, binding = 4) restrict buffer RegionVisibility {
    uint regionVisibility[];
};
layout(std430, binding=5) restrict buffer SectionVisibility {
    uint sectionVisibility[];
};
layout(std430, binding=6) writeonly restrict buffer TerrainCommandBuffer {
    uvec2 terrainCommandBuffer[];
};
layout(std430, binding=7) writeonly restrict buffer TranslucencyCommandBuffer {
    uvec2 translucencyCommandBuffer[];
};
layout(std430, binding=8) readonly restrict buffer SortingRegionList {
    uint sortingRegionList[];
};
layout(std430, binding=9) restrict buffer TerrainData {
    Vertex terrainData[];
};
layout(std430, binding=10) restrict buffer TranslucencyIndexData {
    uint translucencyIndexData[];
};
layout(std430, binding=11) readonly restrict buffer TransformationArray {
    mat4 transformationArray[];
};
layout(std430, binding=12) readonly restrict buffer OriginArray {
    uvec2 originArray[];
};
layout(std430, binding=13) restrict buffer StatisticsBuffer {
    uint statistics_buffer[];
};

mat4 getRegionTransformation(Region region) {
    return transformationArray[unpackRegionTransformId(region)];
}

ivec3 unpackOriginOffsetId(uint id) {
    uint lo = originArray[id].x;
    uint hi = originArray[id].y;
    int x = (int(lo & 0x1ffffffu) << 7) >> 7;
    int z = (int((lo >> 25) | ((hi & 0x3u) << 7)) << 7) >> 7;
    int y = (int((hi >> 18) & 0x3fffu) << 18) >> 18;
    return ivec3(x, y, z);
}

bool useBlockFaceCulling() {
    return (flags&1)!=0;
}
