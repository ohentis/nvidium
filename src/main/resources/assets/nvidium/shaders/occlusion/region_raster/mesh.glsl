#version 460
#extension GL_ARB_shading_language_include : enable
#pragma optionNV(unroll all)
#define UNROLL_LOOP

#import <nvidium:occlusion/mesh_ext_calls.glsl>



#import <nvidium:occlusion/scene.glsl>

#define ADD_SIZE (0.1f/16)

//TODO: maybe do multiple cubes per workgroup? this would increase utilization of individual sm's
layout(local_size_x = 8) in;
layout(triangles, max_vertices=8, max_primitives=12) out;

const uint PILUTTA[] = {0, 1, 0, 6, 0, 5, 1, 7};
const uint PILUTTB[] = {1, 3, 2, 4, 4, 1, 5, 3};
const uint PILUTTC[] = {2, 2, 6, 0, 5, 0, 7, 1};
const uint PILUTTD[] = {4, 7, 2, 2};
const uint PILUTTE[] = {6, 5, 7, 3};
const uint PILUTTF[] = {7, 4, 6, 7};

void emitIndicies(int visIndex) {
    SET_MESH_PRIMITIVE_TRIANGLE_INDICIES(gl_LocalInvocationID.x, 0, PILUTTA[gl_LocalInvocationID.x]);
    SET_MESH_PRIMITIVE_TRIANGLE_INDICIES(gl_LocalInvocationID.x, 1, PILUTTB[gl_LocalInvocationID.x]);
    SET_MESH_PRIMITIVE_TRIANGLE_INDICIES(gl_LocalInvocationID.x, 2, PILUTTC[gl_LocalInvocationID.x]);
    MESHPRIMITIVES[gl_LocalInvocationID.x].gl_PrimitiveID = visIndex;
}

void emitParital(int visIndex) {
    SET_MESH_PRIMITIVE_TRIANGLE_INDICIES((gl_LocalInvocationID.x+8), 0, PILUTTD[gl_LocalInvocationID.x]);
    SET_MESH_PRIMITIVE_TRIANGLE_INDICIES((gl_LocalInvocationID.x+8), 1, PILUTTE[gl_LocalInvocationID.x]);
    SET_MESH_PRIMITIVE_TRIANGLE_INDICIES((gl_LocalInvocationID.x+8), 2, PILUTTF[gl_LocalInvocationID.x]);
    MESHPRIMITIVES[gl_LocalInvocationID.x+8].gl_PrimitiveID = visIndex;
}

void main() {
    //FIXME: It might actually be more efficent to just upload the region data straight into the ubo
    // this remove an entire level of indirection and also puts region data in the very fast path
    Region data = regionData[regionIndicies[gl_WorkGroupID.x]];//fetch the region data

    int visibilityIndex = int(gl_WorkGroupID.x);
    //If the region metadata was empty, return
    if (data.a.x == uint(-1) && data.a.y == uint(-1)) {
        regionVisibility[visibilityIndex] = 0;
        SET_MESH_OUTPUTS(0,0);
        return;
    }
    SET_MESH_OUTPUTS(48,12);

    ivec3 pos = unpackRegionPosition(data);
    pos -= chunkPosition.xyz;
    pos -= unpackOriginOffsetId(unpackRegionTransformId(data));
    ivec3 size = unpackRegionSize(data);

    vec3 start = pos - ADD_SIZE;
    vec3 end = start + 1 + size + (ADD_SIZE*2);

    //TODO: Look into only doing 4 locals, for 2 reasons, its more effective for reducing duplicate computation and bandwidth
    // it also means that each thread can emit 3 primatives, 9 indicies each

    //can also do 8 threads then each thread emits a primative and 4 indicies each then the lower 4 emit 1 indice extra each

    vec3 corner = vec3(((gl_LocalInvocationID.x&1)==0)?start.x:end.x, ((gl_LocalInvocationID.x&4)==0)?start.y:end.y, ((gl_LocalInvocationID.x&2)==0)?start.z:end.z);
    corner *= 16.0f;
    MESHVERTICES[gl_LocalInvocationID.x].gl_Position = MVP*(getRegionTransformation(data)*vec4(corner, 1.0));


    emitIndicies(visibilityIndex);
    if (gl_LocalInvocationID.x < 4) {
        emitParital(visibilityIndex);

        if (gl_LocalInvocationID.x == 0) {
            bool cameraInRegion = all(lessThan(start*16+subchunkOffset.xyz, vec3(ADD_SIZE*16))) && all(lessThan(vec3(-ADD_SIZE*16), end*16+subchunkOffset.xyz));
            regionVisibility[visibilityIndex] = cameraInRegion?1:0;
        }
    }
}
