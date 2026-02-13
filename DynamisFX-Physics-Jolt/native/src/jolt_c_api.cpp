#include "jolt_c_api.h"

#include <jni.h>

#include <algorithm>
#include <cmath>
#include <cstddef>
#include <cstdint>
#include <limits>
#include <mutex>
#include <new>
#include <unordered_map>

#ifndef DFX_JOLT_BACKEND_MODE
#define DFX_JOLT_BACKEND_MODE 0
#endif

namespace {

struct body_record {
    dfx_jolt_body_desc desc {};
    dfx_jolt_body_state state {};
};

bool is_finite(double value) {
    return std::isfinite(value) != 0;
}

bool validate_vec3(const dfx_jolt_vec3& value) {
    return is_finite(value.x) && is_finite(value.y) && is_finite(value.z);
}

bool validate_quat(const dfx_jolt_quat& value) {
    return is_finite(value.x) && is_finite(value.y) && is_finite(value.z) && is_finite(value.w);
}

bool validate_body_state(const dfx_jolt_body_state& state) {
    return validate_vec3(state.position)
            && validate_quat(state.orientation)
            && validate_vec3(state.linear_velocity)
            && validate_vec3(state.angular_velocity);
}

double vec3_length(const dfx_jolt_vec3& v) {
    return std::sqrt((v.x * v.x) + (v.y * v.y) + (v.z * v.z));
}

dfx_jolt_vec3 vec3_sub(const dfx_jolt_vec3& a, const dfx_jolt_vec3& b) {
    dfx_jolt_vec3 result {};
    result.x = a.x - b.x;
    result.y = a.y - b.y;
    result.z = a.z - b.z;
    return result;
}

dfx_jolt_vec3 vec3_add(const dfx_jolt_vec3& a, const dfx_jolt_vec3& b) {
    dfx_jolt_vec3 result {};
    result.x = a.x + b.x;
    result.y = a.y + b.y;
    result.z = a.z + b.z;
    return result;
}

dfx_jolt_vec3 vec3_mul(const dfx_jolt_vec3& v, double scalar) {
    dfx_jolt_vec3 result {};
    result.x = v.x * scalar;
    result.y = v.y * scalar;
    result.z = v.z * scalar;
    return result;
}

double body_radius_guess(const body_record& body) {
    switch (body.desc.shape_type) {
        case DFX_JOLT_SHAPE_SPHERE:
            return std::max(0.0, body.desc.shape_size.x);
        case DFX_JOLT_SHAPE_CAPSULE:
            return std::max(0.0, body.desc.shape_size.x + body.desc.shape_size.y);
        case DFX_JOLT_SHAPE_BOX:
        default:
            return vec3_length(body.desc.shape_size) * 0.5;
    }
}

} // namespace

struct dfx_jolt_world {
    dfx_jolt_world_config config {};
    uint64_t next_body_id {1};
    std::unordered_map<uint64_t, body_record> bodies;
    std::mutex mutex;
};

extern "C" {

uint32_t dfx_jolt_api_version(void) {
    return 1;
}

uint32_t dfx_jolt_backend_mode(void) {
    return DFX_JOLT_BACKEND_MODE;
}

dfx_jolt_world* dfx_jolt_world_create(const dfx_jolt_world_config* config) {
    if (config == nullptr) {
        return nullptr;
    }
    if (!validate_vec3(config->gravity) || !is_finite(config->fixed_step_seconds) || config->fixed_step_seconds <= 0.0) {
        return nullptr;
    }
    auto* world = new (std::nothrow) dfx_jolt_world();
    if (world == nullptr) {
        return nullptr;
    }
    world->config = *config;
    return world;
}

void dfx_jolt_world_destroy(dfx_jolt_world* world) {
    delete world;
}

uint64_t dfx_jolt_body_create(dfx_jolt_world* world, const dfx_jolt_body_desc* desc) {
    if (world == nullptr || desc == nullptr) {
        return 0;
    }
    if (!validate_body_state(desc->initial_state) || !validate_vec3(desc->shape_size) || !is_finite(desc->mass_kg) || desc->mass_kg < 0.0) {
        return 0;
    }
    std::lock_guard<std::mutex> lock(world->mutex);
    const uint64_t body_id = world->next_body_id++;
    body_record body {};
    body.desc = *desc;
    body.state = desc->initial_state;
    world->bodies.emplace(body_id, body);
    return body_id;
}

int32_t dfx_jolt_body_destroy(dfx_jolt_world* world, uint64_t body_id) {
    if (world == nullptr || body_id == 0) {
        return -1;
    }
    std::lock_guard<std::mutex> lock(world->mutex);
    return world->bodies.erase(body_id) == 1 ? 0 : -2;
}

int32_t dfx_jolt_body_get_state(dfx_jolt_world* world, uint64_t body_id, dfx_jolt_body_state* out_state) {
    if (world == nullptr || body_id == 0 || out_state == nullptr) {
        return -1;
    }
    std::lock_guard<std::mutex> lock(world->mutex);
    auto it = world->bodies.find(body_id);
    if (it == world->bodies.end()) {
        return -2;
    }
    *out_state = it->second.state;
    return 0;
}

int32_t dfx_jolt_body_set_state(dfx_jolt_world* world, uint64_t body_id, const dfx_jolt_body_state* state) {
    if (world == nullptr || body_id == 0 || state == nullptr) {
        return -1;
    }
    if (!validate_body_state(*state)) {
        return -3;
    }
    std::lock_guard<std::mutex> lock(world->mutex);
    auto it = world->bodies.find(body_id);
    if (it == world->bodies.end()) {
        return -2;
    }
    it->second.state = *state;
    return 0;
}

int32_t dfx_jolt_world_step(dfx_jolt_world* world, double dt_seconds) {
    if (world == nullptr || !is_finite(dt_seconds) || dt_seconds <= 0.0) {
        return -1;
    }
    std::lock_guard<std::mutex> lock(world->mutex);
    for (auto& entry : world->bodies) {
        body_record& body = entry.second;
        if (body.desc.body_type == DFX_JOLT_BODY_STATIC) {
            continue;
        }
        if (body.desc.body_type == DFX_JOLT_BODY_DYNAMIC) {
            body.state.linear_velocity = vec3_add(body.state.linear_velocity, vec3_mul(world->config.gravity, dt_seconds));
        }
        body.state.position = vec3_add(body.state.position, vec3_mul(body.state.linear_velocity, dt_seconds));
    }
    return 0;
}

int32_t dfx_jolt_world_get_body_states(
        dfx_jolt_world* world,
        const uint64_t* body_ids,
        uint32_t body_count,
        dfx_jolt_body_state_row* out_rows) {
    if (world == nullptr || body_ids == nullptr || out_rows == nullptr) {
        return -1;
    }
    std::lock_guard<std::mutex> lock(world->mutex);
    for (uint32_t i = 0; i < body_count; i++) {
        const uint64_t id = body_ids[i];
        auto it = world->bodies.find(id);
        if (it == world->bodies.end()) {
            return -2;
        }
        out_rows[i].body_id = id;
        out_rows[i].state = it->second.state;
    }
    return 0;
}

int32_t dfx_jolt_world_raycast(
        dfx_jolt_world* world,
        const dfx_jolt_raycast_request* request,
        dfx_jolt_raycast_hit* out_hit) {
    if (world == nullptr || request == nullptr || out_hit == nullptr) {
        return -1;
    }
    if (!validate_vec3(request->origin) || !validate_vec3(request->direction) || !is_finite(request->max_distance) || request->max_distance <= 0.0) {
        return -3;
    }
    std::lock_guard<std::mutex> lock(world->mutex);
    double best = request->max_distance;
    bool found = false;
    uint64_t best_id = 0;
    dfx_jolt_vec3 best_normal {};
    for (const auto& entry : world->bodies) {
        const uint64_t id = entry.first;
        const body_record& body = entry.second;
        dfx_jolt_vec3 to_center = vec3_sub(body.state.position, request->origin);
        const double distance = vec3_length(to_center) - body_radius_guess(body);
        if (distance >= 0.0 && distance <= best) {
            best = distance;
            best_id = id;
            const double nlen = vec3_length(to_center);
            if (nlen > 0.0) {
                best_normal.x = to_center.x / nlen;
                best_normal.y = to_center.y / nlen;
                best_normal.z = to_center.z / nlen;
            } else {
                best_normal.x = 0.0;
                best_normal.y = 1.0;
                best_normal.z = 0.0;
            }
            found = true;
        }
    }
    if (!found) {
        return -2;
    }
    out_hit->body_id = best_id;
    out_hit->distance = best;
    out_hit->normal = best_normal;
    return 0;
}

uint32_t dfx_jolt_world_read_contacts(
        dfx_jolt_world* world,
        dfx_jolt_contact_row* out_rows,
        uint32_t max_rows) {
    (void)world;
    (void)out_rows;
    (void)max_rows;
    return 0;
}

} // extern "C"

namespace {

dfx_jolt_world* as_world(jlong handle) {
    return reinterpret_cast<dfx_jolt_world*>(static_cast<uintptr_t>(handle));
}

jlong as_handle(dfx_jolt_world* world) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(world));
}

} // namespace

extern "C" {

JNIEXPORT jint JNICALL Java_org_dynamisfx_physics_jolt_JoltNativeBridge_nativeApiVersion(
        JNIEnv*,
        jclass) {
    return static_cast<jint>(dfx_jolt_api_version());
}

JNIEXPORT jint JNICALL Java_org_dynamisfx_physics_jolt_JoltNativeBridge_nativeBackendMode(
        JNIEnv*,
        jclass) {
    return static_cast<jint>(dfx_jolt_backend_mode());
}

JNIEXPORT jlong JNICALL Java_org_dynamisfx_physics_jolt_JoltNativeBridge_nativeWorldCreate(
        JNIEnv*,
        jclass,
        jdouble gx,
        jdouble gy,
        jdouble gz,
        jdouble fixed_step_seconds,
        jint solver_iterations,
        jdouble friction,
        jdouble restitution,
        jdouble cfm,
        jdouble restitution_threshold) {
    dfx_jolt_world_config config {};
    config.gravity = dfx_jolt_vec3 {gx, gy, gz};
    config.fixed_step_seconds = fixed_step_seconds;
    config.tuning.solver_iterations = solver_iterations;
    config.tuning.friction = friction;
    config.tuning.restitution = restitution;
    config.tuning.cfm = cfm;
    config.tuning.restitution_threshold = restitution_threshold;
    return as_handle(dfx_jolt_world_create(&config));
}

JNIEXPORT void JNICALL Java_org_dynamisfx_physics_jolt_JoltNativeBridge_nativeWorldDestroy(
        JNIEnv*,
        jclass,
        jlong world_handle) {
    dfx_jolt_world_destroy(as_world(world_handle));
}

JNIEXPORT jlong JNICALL Java_org_dynamisfx_physics_jolt_JoltNativeBridge_nativeBodyCreate(
        JNIEnv*,
        jclass,
        jlong world_handle,
        jint body_type,
        jint shape_type,
        jdouble mass_kg,
        jdouble sx,
        jdouble sy,
        jdouble sz,
        jdouble px,
        jdouble py,
        jdouble pz,
        jdouble qx,
        jdouble qy,
        jdouble qz,
        jdouble qw,
        jdouble lvx,
        jdouble lvy,
        jdouble lvz,
        jdouble avx,
        jdouble avy,
        jdouble avz) {
    dfx_jolt_body_desc desc {};
    desc.shape_type = static_cast<dfx_jolt_shape_type>(shape_type);
    desc.body_type = static_cast<dfx_jolt_body_type>(body_type);
    desc.mass_kg = mass_kg;
    desc.shape_size = dfx_jolt_vec3 {sx, sy, sz};
    desc.initial_state.position = dfx_jolt_vec3 {px, py, pz};
    desc.initial_state.orientation = dfx_jolt_quat {qx, qy, qz, qw};
    desc.initial_state.linear_velocity = dfx_jolt_vec3 {lvx, lvy, lvz};
    desc.initial_state.angular_velocity = dfx_jolt_vec3 {avx, avy, avz};
    return static_cast<jlong>(dfx_jolt_body_create(as_world(world_handle), &desc));
}

JNIEXPORT jint JNICALL Java_org_dynamisfx_physics_jolt_JoltNativeBridge_nativeBodyDestroy(
        JNIEnv*,
        jclass,
        jlong world_handle,
        jlong body_id) {
    return dfx_jolt_body_destroy(as_world(world_handle), static_cast<uint64_t>(body_id));
}

JNIEXPORT jint JNICALL Java_org_dynamisfx_physics_jolt_JoltNativeBridge_nativeBodyGetState(
        JNIEnv* env,
        jclass,
        jlong world_handle,
        jlong body_id,
        jdoubleArray out_state_13) {
    if (out_state_13 == nullptr || env->GetArrayLength(out_state_13) < 13) {
        return -1;
    }
    dfx_jolt_body_state state {};
    int32_t status = dfx_jolt_body_get_state(as_world(world_handle), static_cast<uint64_t>(body_id), &state);
    if (status != 0) {
        return status;
    }
    jdouble values[13] = {
            state.position.x, state.position.y, state.position.z,
            state.orientation.x, state.orientation.y, state.orientation.z, state.orientation.w,
            state.linear_velocity.x, state.linear_velocity.y, state.linear_velocity.z,
            state.angular_velocity.x, state.angular_velocity.y, state.angular_velocity.z
    };
    env->SetDoubleArrayRegion(out_state_13, 0, 13, values);
    return 0;
}

JNIEXPORT jint JNICALL Java_org_dynamisfx_physics_jolt_JoltNativeBridge_nativeBodySetState(
        JNIEnv* env,
        jclass,
        jlong world_handle,
        jlong body_id,
        jdoubleArray state_13) {
    if (state_13 == nullptr || env->GetArrayLength(state_13) < 13) {
        return -1;
    }
    jdouble values[13] = {};
    env->GetDoubleArrayRegion(state_13, 0, 13, values);
    dfx_jolt_body_state state {};
    state.position = dfx_jolt_vec3 {values[0], values[1], values[2]};
    state.orientation = dfx_jolt_quat {values[3], values[4], values[5], values[6]};
    state.linear_velocity = dfx_jolt_vec3 {values[7], values[8], values[9]};
    state.angular_velocity = dfx_jolt_vec3 {values[10], values[11], values[12]};
    return dfx_jolt_body_set_state(as_world(world_handle), static_cast<uint64_t>(body_id), &state);
}

JNIEXPORT jint JNICALL Java_org_dynamisfx_physics_jolt_JoltNativeBridge_nativeWorldStep(
        JNIEnv*,
        jclass,
        jlong world_handle,
        jdouble dt_seconds) {
    return dfx_jolt_world_step(as_world(world_handle), dt_seconds);
}

} // extern "C"
