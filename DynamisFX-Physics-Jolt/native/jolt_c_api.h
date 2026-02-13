#ifndef DYNAMISFX_JOLT_C_API_H
#define DYNAMISFX_JOLT_C_API_H

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct dfx_jolt_world dfx_jolt_world;

typedef struct {
    double x;
    double y;
    double z;
} dfx_jolt_vec3;

typedef struct {
    double x;
    double y;
    double z;
    double w;
} dfx_jolt_quat;

typedef struct {
    dfx_jolt_vec3 position;
    dfx_jolt_quat orientation;
    dfx_jolt_vec3 linear_velocity;
    dfx_jolt_vec3 angular_velocity;
} dfx_jolt_body_state;

typedef struct {
    int32_t solver_iterations;
    double friction;
    double restitution;
    double cfm;
    double restitution_threshold;
} dfx_jolt_runtime_tuning;

typedef struct {
    dfx_jolt_vec3 gravity;
    double fixed_step_seconds;
    dfx_jolt_runtime_tuning tuning;
} dfx_jolt_world_config;

typedef enum {
    DFX_JOLT_BODY_STATIC = 0,
    DFX_JOLT_BODY_KINEMATIC = 1,
    DFX_JOLT_BODY_DYNAMIC = 2
} dfx_jolt_body_type;

typedef enum {
    DFX_JOLT_SHAPE_SPHERE = 0,
    DFX_JOLT_SHAPE_BOX = 1,
    DFX_JOLT_SHAPE_CAPSULE = 2
} dfx_jolt_shape_type;

typedef struct {
    dfx_jolt_shape_type shape_type;
    dfx_jolt_body_type body_type;
    double mass_kg;
    dfx_jolt_vec3 shape_size;
    dfx_jolt_body_state initial_state;
} dfx_jolt_body_desc;

typedef struct {
    uint64_t body_id;
    dfx_jolt_body_state state;
} dfx_jolt_body_state_row;

typedef struct {
    dfx_jolt_vec3 origin;
    dfx_jolt_vec3 direction;
    double max_distance;
} dfx_jolt_raycast_request;

typedef struct {
    uint64_t body_id;
    double distance;
    dfx_jolt_vec3 normal;
} dfx_jolt_raycast_hit;

typedef struct {
    uint64_t body_a;
    uint64_t body_b;
    dfx_jolt_vec3 point;
    dfx_jolt_vec3 normal;
    double penetration_depth;
} dfx_jolt_contact_row;

dfx_jolt_world* dfx_jolt_world_create(const dfx_jolt_world_config* config);
void dfx_jolt_world_destroy(dfx_jolt_world* world);

uint64_t dfx_jolt_body_create(dfx_jolt_world* world, const dfx_jolt_body_desc* desc);
int32_t dfx_jolt_body_destroy(dfx_jolt_world* world, uint64_t body_id);
int32_t dfx_jolt_body_get_state(dfx_jolt_world* world, uint64_t body_id, dfx_jolt_body_state* out_state);
int32_t dfx_jolt_body_set_state(dfx_jolt_world* world, uint64_t body_id, const dfx_jolt_body_state* state);

int32_t dfx_jolt_world_step(dfx_jolt_world* world, double dt_seconds);

int32_t dfx_jolt_world_get_body_states(
        dfx_jolt_world* world,
        const uint64_t* body_ids,
        uint32_t body_count,
        dfx_jolt_body_state_row* out_rows);

int32_t dfx_jolt_world_raycast(
        dfx_jolt_world* world,
        const dfx_jolt_raycast_request* request,
        dfx_jolt_raycast_hit* out_hit);

uint32_t dfx_jolt_world_read_contacts(
        dfx_jolt_world* world,
        dfx_jolt_contact_row* out_rows,
        uint32_t max_rows);

#ifdef __cplusplus
}
#endif

#endif
