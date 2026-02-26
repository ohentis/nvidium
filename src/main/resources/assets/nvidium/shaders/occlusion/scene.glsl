#ifdef USE_SODIUM_VERTEX_FORMAT
struct Vertex {
    uint16_t x;
    uint16_t y;
    uint16_t z;
    uint8_t material;
    uint8_t section;
    uint color;
    uint16_t u;
    uint16_t v;
    uint16_t skyLight;
    uint16_t blockLight;
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
    uint64_t a;
    uint64_t b;
};

ivec3 unpackRegionSize(Region region) {
    return ivec3((region.a>>59)&7, region.a>>62, (region.a>>56)&7);
}

uint unpackRegionTransformId(Region region) {
    return uint((region.b>>(64-24-10))&((1<<10)-1));
}

ivec3 unpackRegionPosition(Region region) {
    //TODO: optimize
    int x = int(int64_t(region.a<<(64-24-24))>>(64-24));
    int y = (int(region.a)<<8)>>8;
    int z = int(int64_t(region.b)>>(64-24));
    return ivec3(x,y,z);
}

int unpackRegionCount(Region region) {
    return int((region.a>>48)&255);
}

bool sectionEmpty(ivec4 header) {
    header.y &= ~0x1FF<<17;
    return header == ivec4(0);
}


layout(std140, binding=0) uniform SceneData {
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
    uint16_t regionCount;//Number of regions in regionIndicies
    //align(1)
    uint8_t frameId;
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
    uint8_t regionVisibility[];
};
layout(std430, binding=5) restrict buffer SectionVisibility {
    uint8_t sectionVisibility[];
};
layout(std430, binding=6) writeonly restrict buffer TerrainCommandBuffer {
    uvec2 terrainCommandBuffer[];
};
layout(std430, binding=7) writeonly restrict buffer TranslucencyCommandBuffer {
    uvec2 translucencyCommandBuffer[];
};
layout(std430, binding=8) readonly restrict buffer SortingRegionList {
    uint16_t sortingRegionList[];
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
    uint64_t originArray[];
};
layout(std430, binding=13) restrict buffer StatisticsBuffer {
    uint32_t statistics_buffer[];
};

mat4 getRegionTransformation(Region region) {
    return transformationArray[unpackRegionTransformId(region)];
}

ivec3 unpackOriginOffsetId(uint id) {
    uint64_t val = originArray[id];
    int x = (int(uint(val&0x1ffffff))<<7)>>7;
    int y = (int(uint((val>>50)&0x3fff))<<18)>>18;
    int z = (int(uint((val>>25)&0x1ffffff))<<7)>>7;
    return ivec3(x,y,z);
}

bool useBlockFaceCulling() {
    return (flags&1)!=0;
}
