#include "jolt_c_api.h"

#include <jni.h>

#include <cstdint>

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
