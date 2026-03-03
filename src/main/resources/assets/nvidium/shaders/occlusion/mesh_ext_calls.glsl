#ifdef USE_GL_EXT_MESH_SHADERS
#extension GL_EXT_mesh_shader : require
#define MESHVERTICES gl_MeshVerticesEXT
#define MESHPRIMITIVES gl_MeshPrimitivesEXT
#define MESH_PRIMITIVE_TRIANGLE_INDICIES gl_PrimitiveTriangleIndicesEXT
#define SET_MESH_PRIMITIVE_TRIANGLE_INDICIES(triangle, vertice, value) (gl_PrimitiveTriangleIndicesEXT[triangle][vertice]=value)
#define EMIT_MESH_TASKS(x, y, z) EmitMeshTasksEXT(x, y, z)
#define SET_MESH_OUTPUTS(verts, prims) SetMeshOutputsEXT(verts, prims)
#else
#extension GL_NV_mesh_shader : require
#define MESHVERTICES gl_MeshVerticesNV
#define MESHPRIMITIVES gl_MeshPrimitivesNV
#define MESH_PRIMITIVE_TRIANGLE_INDICIES gl_PrimitiveIndicesNV
#define SET_MESH_PRIMITIVE_TRIANGLE_INDICIES(triangle, vertice, value) (gl_PrimitiveIndicesNV[(triangle) * 3 + (vertice)] = (value))
#define EMIT_MESH_TASKS(x, y, z) (gl_TaskCountNV = (x) * (y) * (z))
#define SET_MESH_OUTPUTS(verts, prims) (gl_PrimitiveCountNV = prims)
#endif
