#version 460

#extension GL_ARB_shading_language_include : enable
#pragma optionNV(unroll all)
#define UNROLL_LOOP
#import <nvidium:occlusion/mesh_ext_calls.glsl>

#extension GL_KHR_shader_subgroup_basic : require
#extension GL_KHR_shader_subgroup_ballot : require
#extension GL_KHR_shader_subgroup_vote : require

#import <nvidium:occlusion/scene.glsl>

//This is 1 since each task shader workgroup -> multiple meshlets. its not each globalInvocation (afaik)
layout(local_size_x=1) in;
#ifdef USE_GL_EXT_MESH_SHADERS
out taskPayloadSharedEXT Task {
#else
taskNV out Task {
#endif
    uint _visOutBase;// The base offset for the visibility output of the shader
    uint _offset;//start offset for regions (can/should probably be a uint16 since this is just the region id << 8)
    //uint64_t bitcheck[4];//TODO: MAYBE DO THIS, each bit is whether there a section at that index, doing so is faster than pulling metadata to check if a section is valid or not
    mat4 regionTransform;
    ivec3 chunkShift;
};

void main() {
    //TODO: see whats faster, atomicAdd (for mdic) or dispatching alot of empty calls (mdi)
    //TODO: experiment with emitting 8 workgroups with the 8th always being 0
    // doing so would enable to batch memory write 2 commands
    // thus taking 4 mem moves instead of 7

    //Emit 7 workloads per chunk
    uint cmdIdx = gl_WorkGroupID.x;
    uint transCmdIdx = (uint(regionCount) - gl_WorkGroupID.x) - 1;

    //Early exit if the region wasnt visible
    if (regionVisibility[gl_WorkGroupID.x] == 0) {
        terrainCommandBuffer[cmdIdx] = uvec2(0);
        translucencyCommandBuffer[transCmdIdx] = uvec2(0);
        EMIT_MESH_TASKS(0,0,0);
        return;
    }

    #ifdef STATISTICS_REGIONS
    atomicAdd(statistics_buffer, 1);
    #endif

    //FIXME: It might actually be more efficent to just upload the region data straight into the ubo
    uint offset = uint(regionIndicies[gl_WorkGroupID.x]);
    Region data = regionData[offset];
    int count = unpackRegionCount(data)+1;

    //Write in order
    _visOutBase = offset<<8;//This makes checking visibility very fast and quick in the compute shader
    _offset = offset<<8;
    regionTransform = getRegionTransformation(data);

    chunkShift = (-chunkPosition.xyz) - unpackOriginOffsetId(unpackRegionTransformId(data));
    EMIT_MESH_TASKS(count,1,1);

    terrainCommandBuffer[cmdIdx] = uvec2(uint(count), _visOutBase);
    //TODO: add a bit to the region header to determine whether or not a region has any translucent
    // sections, if it doesnt, write 0 to the command buffer
    translucencyCommandBuffer[transCmdIdx] = uvec2(uint(count), _visOutBase);
}
