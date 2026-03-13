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

bool shouldRenderVisible(uint sectionId) {
    return (sectionVisibility[sectionId]&1) != 0;
}

#import <nvidium:terrain/task_common.glsl>

void main() {
    #ifdef USE_GL_EXT_MESH_SHADERS
    uint sectionId = terrainCommandBuffer[gl_DrawID].w + gl_WorkGroupID.x;
    #else
    uint sectionId = gl_WorkGroupID.x;
    #endif

    if (!shouldRenderVisible(sectionId)) {
        //Early exit if the section isnt visible
        EMIT_MESH_TASKS(0,0,0);
        return;
    }

    #ifdef STATISTICS_SECTIONS
    atomicAdd(statistics_buffer+1, 1);
    #endif

    ivec4 header = sectionData[sectionId].header;
    ivec3 chunk = ivec3(header.xyz)>>8;
    chunk.y &= 0x1ff;
    chunk.y <<= 32-9;
    chunk.y >>= 32-9;
    chunk -= chunkPosition.xyz;
    transformationId = unpackRegionTransformId(regionData[sectionId>>8]);
    chunk -= unpackOriginOffsetId(transformationId);

    origin = vec3(chunk<<4);
    baseOffset = uint(header.w);

    populateTasks(chunk, uvec4(sectionData[sectionId].renderRanges));


    #ifdef STATISTICS_QUADS
    atomicAdd(statistics_buffer+2, quadCount);
    #endif
}
