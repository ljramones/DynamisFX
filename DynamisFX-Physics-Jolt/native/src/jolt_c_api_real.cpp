#include "jolt_c_api.h"

#include <Jolt/Jolt.h>
#include <Jolt/Core/Factory.h>
#include <Jolt/Core/JobSystemThreadPool.h>
#include <Jolt/Core/TempAllocator.h>
#include <Jolt/Physics/Body/BodyActivationListener.h>
#include <Jolt/Physics/Body/BodyCreationSettings.h>
#include <Jolt/Physics/Body/BodyInterface.h>
#include <Jolt/Physics/Body/BodyID.h>
#include <Jolt/Physics/Body/BodyLockInterface.h>
#include <Jolt/Physics/Body/BodyLockMulti.h>
#include <Jolt/Physics/Body/BodyLockRead.h>
#include <Jolt/Physics/Collision/BroadPhase/BroadPhaseLayer.h>
#include <Jolt/Physics/Collision/BroadPhase/BroadPhaseLayerInterface.h>
#include <Jolt/Physics/Collision/CollisionCollectorImpl.h>
#include <Jolt/Physics/Collision/ObjectLayer.h>
#include <Jolt/Physics/Collision/ObjectLayerPairFilter.h>
#include <Jolt/Physics/Collision/ObjectVsBroadPhaseLayerFilter.h>
#include <Jolt/Physics/Collision/RayCast.h>
#include <Jolt/Physics/Collision/Shape/BoxShape.h>
#include <Jolt/Physics/Collision/Shape/CapsuleShape.h>
#include <Jolt/Physics/Collision/Shape/Shape.h>
#include <Jolt/Physics/Collision/Shape/SphereShape.h>
#include <Jolt/Physics/PhysicsSystem.h>
#include <Jolt/RegisterTypes.h>

#include <algorithm>
#include <cmath>
#include <cstdint>
#include <limits>
#include <memory>
#include <mutex>
#include <new>
#include <unordered_map>

namespace {

using namespace JPH;

constexpr uint32_t kApiVersion = 1;
constexpr uint32_t kBackendMode = 1;

namespace Layers {
constexpr ObjectLayer NON_MOVING = 0;
constexpr ObjectLayer MOVING = 1;
constexpr ObjectLayer NUM_LAYERS = 2;
} // namespace Layers

namespace BroadPhaseLayers {
constexpr BroadPhaseLayer NON_MOVING(0);
constexpr BroadPhaseLayer MOVING(1);
constexpr uint32_t NUM_LAYERS = 2;
} // namespace BroadPhaseLayers

class BroadPhaseLayerInterfaceImpl final : public BroadPhaseLayerInterface {
public:
    BroadPhaseLayerInterfaceImpl() {
        mObjectToBroadPhase[Layers::NON_MOVING] = BroadPhaseLayers::NON_MOVING;
        mObjectToBroadPhase[Layers::MOVING] = BroadPhaseLayers::MOVING;
    }

    uint GetNumBroadPhaseLayers() const override {
        return BroadPhaseLayers::NUM_LAYERS;
    }

    BroadPhaseLayer GetBroadPhaseLayer(ObjectLayer inLayer) const override {
        return mObjectToBroadPhase[inLayer];
    }

#if defined(JPH_EXTERNAL_PROFILE) || defined(JPH_PROFILE_ENABLED)
    const char* GetBroadPhaseLayerName(BroadPhaseLayer inLayer) const override {
        switch ((BroadPhaseLayer::Type)inLayer) {
            case (BroadPhaseLayer::Type)BroadPhaseLayers::NON_MOVING:
                return "NON_MOVING";
            case (BroadPhaseLayer::Type)BroadPhaseLayers::MOVING:
                return "MOVING";
            default:
                return "INVALID";
        }
    }
#endif

private:
    BroadPhaseLayer mObjectToBroadPhase[Layers::NUM_LAYERS];
};

class ObjectVsBroadPhaseLayerFilterImpl final : public ObjectVsBroadPhaseLayerFilter {
public:
    bool ShouldCollide(ObjectLayer inLayer1, BroadPhaseLayer inLayer2) const override {
        switch (inLayer1) {
            case Layers::NON_MOVING:
                return inLayer2 == BroadPhaseLayers::MOVING;
            case Layers::MOVING:
                return true;
            default:
                return false;
        }
    }
};

class ObjectLayerPairFilterImpl final : public ObjectLayerPairFilter {
public:
    bool ShouldCollide(ObjectLayer inObject1, ObjectLayer inObject2) const override {
        switch (inObject1) {
            case Layers::NON_MOVING:
                return inObject2 == Layers::MOVING;
            case Layers::MOVING:
                return true;
            default:
                return false;
        }
    }
};

class BodyActivationListenerImpl final : public BodyActivationListener {
public:
    void OnBodyActivated(const BodyID&, uint64) override {
    }

    void OnBodyDeactivated(const BodyID&, uint64) override {
    }
};

class ContactListenerImpl final : public ContactListener {
public:
    ValidateResult OnContactValidate(
            const Body&,
            const Body&,
            RVec3Arg,
            const CollideShapeResult&) override {
        return ValidateResult::AcceptAllContactsForThisBodyPair;
    }

    void OnContactAdded(const Body&, const Body&, const ContactManifold&, ContactSettings&) override {
    }

    void OnContactPersisted(const Body&, const Body&, const ContactManifold&, ContactSettings&) override {
    }

    void OnContactRemoved(const SubShapeIDPair&) override {
    }
};

struct dfx_jolt_world {
    dfx_jolt_world_config config {};
    std::mutex mutex;

    TempAllocatorImpl temp_allocator {10U * 1024U * 1024U};
    JobSystemThreadPool job_system {cMaxPhysicsJobs, cMaxPhysicsBarriers, 4};
    BroadPhaseLayerInterfaceImpl broad_phase_layer_interface;
    ObjectVsBroadPhaseLayerFilterImpl object_vs_broad_phase_filter;
    ObjectLayerPairFilterImpl object_layer_pair_filter;
    BodyActivationListenerImpl body_activation_listener;
    ContactListenerImpl contact_listener;
    PhysicsSystem physics_system;

    uint64_t next_body_id {1};
    std::unordered_map<uint64_t, BodyID> bodies;

    explicit dfx_jolt_world(const dfx_jolt_world_config& in_config)
        : config(in_config) {
    }
};

std::once_flag g_jolt_init_once;
std::mutex g_jolt_shutdown_mutex;
uint32_t g_world_count = 0;

bool is_finite(double v) {
    return std::isfinite(v) != 0;
}

bool validate_vec3(const dfx_jolt_vec3& v) {
    return is_finite(v.x) && is_finite(v.y) && is_finite(v.z);
}

bool validate_quat(const dfx_jolt_quat& q) {
    return is_finite(q.x) && is_finite(q.y) && is_finite(q.z) && is_finite(q.w);
}

bool validate_body_state(const dfx_jolt_body_state& s) {
    return validate_vec3(s.position)
            && validate_quat(s.orientation)
            && validate_vec3(s.linear_velocity)
            && validate_vec3(s.angular_velocity);
}

RVec3 to_rvec3(const dfx_jolt_vec3& v) {
    return RVec3(v.x, v.y, v.z);
}

Vec3 to_vec3(const dfx_jolt_vec3& v) {
    return Vec3((float)v.x, (float)v.y, (float)v.z);
}

Quat to_quat(const dfx_jolt_quat& q) {
    return Quat((float)q.x, (float)q.y, (float)q.z, (float)q.w);
}

dfx_jolt_vec3 from_rvec3(RVec3Arg v) {
    return dfx_jolt_vec3 {v.GetX(), v.GetY(), v.GetZ()};
}

dfx_jolt_vec3 from_vec3(Vec3Arg v) {
    return dfx_jolt_vec3 {v.GetX(), v.GetY(), v.GetZ()};
}

dfx_jolt_quat from_quat(QuatArg q) {
    return dfx_jolt_quat {q.GetX(), q.GetY(), q.GetZ(), q.GetW()};
}

ObjectLayer to_object_layer(dfx_jolt_body_type t) {
    return t == DFX_JOLT_BODY_STATIC ? Layers::NON_MOVING : Layers::MOVING;
}

EMotionType to_motion_type(dfx_jolt_body_type t) {
    switch (t) {
        case DFX_JOLT_BODY_STATIC:
            return EMotionType::Static;
        case DFX_JOLT_BODY_KINEMATIC:
            return EMotionType::Kinematic;
        case DFX_JOLT_BODY_DYNAMIC:
        default:
            return EMotionType::Dynamic;
    }
}

RefConst<Shape> create_shape(const dfx_jolt_body_desc& desc) {
    switch (desc.shape_type) {
        case DFX_JOLT_SHAPE_SPHERE:
            return new SphereShape((float)std::max(0.0, desc.shape_size.x));
        case DFX_JOLT_SHAPE_CAPSULE:
            return new CapsuleShape((float)std::max(0.0, desc.shape_size.y), (float)std::max(0.0, desc.shape_size.x));
        case DFX_JOLT_SHAPE_BOX:
        default:
            return new BoxShape(Vec3((float)(desc.shape_size.x * 0.5), (float)(desc.shape_size.y * 0.5), (float)(desc.shape_size.z * 0.5)));
    }
}

void initialize_jolt_once() {
    std::call_once(g_jolt_init_once, []() {
        RegisterDefaultAllocator();
        Factory::sInstance = new Factory();
        RegisterTypes();
    });
}

void increment_world_count() {
    std::lock_guard<std::mutex> lock(g_jolt_shutdown_mutex);
    ++g_world_count;
}

void decrement_world_count() {
    std::lock_guard<std::mutex> lock(g_jolt_shutdown_mutex);
    if (g_world_count == 0) {
        return;
    }
    --g_world_count;
    if (g_world_count == 0) {
        UnregisterTypes();
        delete Factory::sInstance;
        Factory::sInstance = nullptr;
    }
}

} // namespace

extern "C" {

uint32_t dfx_jolt_api_version(void) {
    return kApiVersion;
}

uint32_t dfx_jolt_backend_mode(void) {
    return kBackendMode;
}

dfx_jolt_world* dfx_jolt_world_create(const dfx_jolt_world_config* config) {
    if (config == nullptr || !validate_vec3(config->gravity) || !is_finite(config->fixed_step_seconds) || config->fixed_step_seconds <= 0.0) {
        return nullptr;
    }

    initialize_jolt_once();

    auto* world = new (std::nothrow) dfx_jolt_world(*config);
    if (world == nullptr) {
        return nullptr;
    }

    constexpr uint32_t max_bodies = 65536;
    constexpr uint32_t num_body_mutexes = 0;
    constexpr uint32_t max_body_pairs = 65536;
    constexpr uint32_t max_contact_constraints = 16384;

    world->physics_system.Init(
            max_bodies,
            num_body_mutexes,
            max_body_pairs,
            max_contact_constraints,
            world->broad_phase_layer_interface,
            world->object_vs_broad_phase_filter,
            world->object_layer_pair_filter);

    world->physics_system.SetBodyActivationListener(&world->body_activation_listener);
    world->physics_system.SetContactListener(&world->contact_listener);

    world->physics_system.SetGravity(to_vec3(config->gravity));
    increment_world_count();
    return world;
}

void dfx_jolt_world_destroy(dfx_jolt_world* world) {
    if (world == nullptr) {
        return;
    }

    {
        std::lock_guard<std::mutex> lock(world->mutex);
        BodyInterface& body_interface = world->physics_system.GetBodyInterface();
        for (const auto& entry : world->bodies) {
            body_interface.RemoveBody(entry.second);
            body_interface.DestroyBody(entry.second);
        }
        world->bodies.clear();
    }

    delete world;
    decrement_world_count();
}

uint64_t dfx_jolt_body_create(dfx_jolt_world* world, const dfx_jolt_body_desc* desc) {
    if (world == nullptr || desc == nullptr || !validate_body_state(desc->initial_state) || !validate_vec3(desc->shape_size) || !is_finite(desc->mass_kg) || desc->mass_kg < 0.0) {
        return 0;
    }

    std::lock_guard<std::mutex> lock(world->mutex);

    RefConst<Shape> shape = create_shape(*desc);
    BodyCreationSettings settings(
            shape,
            to_rvec3(desc->initial_state.position),
            to_quat(desc->initial_state.orientation),
            to_motion_type(desc->body_type),
            to_object_layer(desc->body_type));

    if (desc->body_type == DFX_JOLT_BODY_DYNAMIC) {
        settings.mMotionQuality = EMotionQuality::LinearCast;
        if (desc->mass_kg > 0.0) {
            settings.mOverrideMassProperties = EOverrideMassProperties::CalculateInertia;
            settings.mMassPropertiesOverride.mMass = (float)desc->mass_kg;
        }
    }

    BodyInterface& body_interface = world->physics_system.GetBodyInterface();
    Body* body = body_interface.CreateBody(settings);
    if (body == nullptr) {
        return 0;
    }

    const BodyID id = body->GetID();
    const EActivation activation = desc->body_type == DFX_JOLT_BODY_STATIC ? EActivation::DontActivate : EActivation::Activate;
    body_interface.AddBody(id, activation);
    body_interface.SetLinearVelocity(id, to_vec3(desc->initial_state.linear_velocity));
    body_interface.SetAngularVelocity(id, to_vec3(desc->initial_state.angular_velocity));

    const uint64_t ext_id = world->next_body_id++;
    world->bodies.emplace(ext_id, id);
    return ext_id;
}

int32_t dfx_jolt_body_destroy(dfx_jolt_world* world, uint64_t body_id) {
    if (world == nullptr || body_id == 0) {
        return -1;
    }

    std::lock_guard<std::mutex> lock(world->mutex);
    auto it = world->bodies.find(body_id);
    if (it == world->bodies.end()) {
        return -2;
    }

    BodyInterface& body_interface = world->physics_system.GetBodyInterface();
    body_interface.RemoveBody(it->second);
    body_interface.DestroyBody(it->second);
    world->bodies.erase(it);
    return 0;
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

    const BodyLockInterface& lock_interface = world->physics_system.GetBodyLockInterface();
    BodyLockRead lock_read(lock_interface, it->second);
    if (!lock_read.Succeeded()) {
        return -3;
    }

    const Body& body = lock_read.GetBody();
    out_state->position = from_rvec3(body.GetPosition());
    out_state->orientation = from_quat(body.GetRotation());
    out_state->linear_velocity = from_vec3(body.GetLinearVelocity());
    out_state->angular_velocity = from_vec3(body.GetAngularVelocity());
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

    BodyInterface& body_interface = world->physics_system.GetBodyInterface();
    body_interface.SetPositionAndRotation(it->second, to_rvec3(state->position), to_quat(state->orientation), EActivation::DontActivate);
    body_interface.SetLinearVelocity(it->second, to_vec3(state->linear_velocity));
    body_interface.SetAngularVelocity(it->second, to_vec3(state->angular_velocity));
    return 0;
}

int32_t dfx_jolt_world_step(dfx_jolt_world* world, double dt_seconds) {
    if (world == nullptr || !is_finite(dt_seconds) || dt_seconds <= 0.0) {
        return -1;
    }

    std::lock_guard<std::mutex> lock(world->mutex);
    const int collision_steps = std::max(1, world->config.tuning.solver_iterations);
    const int status = world->physics_system.Update(
            (float)dt_seconds,
            collision_steps,
            &world->temp_allocator,
            &world->job_system);
    return status == EPhysicsUpdateError::None ? 0 : -3;
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
        const uint64_t ext_id = body_ids[i];
        auto it = world->bodies.find(ext_id);
        if (it == world->bodies.end()) {
            return -2;
        }

        const BodyLockInterface& lock_interface = world->physics_system.GetBodyLockInterface();
        BodyLockRead lock_read(lock_interface, it->second);
        if (!lock_read.Succeeded()) {
            return -3;
        }

        const Body& body = lock_read.GetBody();
        out_rows[i].body_id = ext_id;
        out_rows[i].state.position = from_rvec3(body.GetPosition());
        out_rows[i].state.orientation = from_quat(body.GetRotation());
        out_rows[i].state.linear_velocity = from_vec3(body.GetLinearVelocity());
        out_rows[i].state.angular_velocity = from_vec3(body.GetAngularVelocity());
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

    const Vec3 dir = to_vec3(request->direction);
    const float len = dir.Length();
    if (len <= std::numeric_limits<float>::epsilon()) {
        return -3;
    }

    RayCast ray(to_rvec3(request->origin), (request->max_distance / len) * dir);
    RayCastResult result;
    const bool hit = world->physics_system.GetNarrowPhaseQuery().CastRay(ray, result);
    if (!hit) {
        return -2;
    }

    uint64_t external_id = 0;
    for (const auto& entry : world->bodies) {
        if (entry.second == result.mBodyID) {
            external_id = entry.first;
            break;
        }
    }
    if (external_id == 0) {
        return -2;
    }

    out_hit->body_id = external_id;
    out_hit->distance = request->max_distance * result.mFraction;

    const BodyLockInterface& lock_interface = world->physics_system.GetBodyLockInterface();
    BodyLockRead lock_read(lock_interface, result.mBodyID);
    if (!lock_read.Succeeded()) {
        return -3;
    }

    const Body& body = lock_read.GetBody();
    RVec3 hit_pos = ray.GetPointOnRay(result.mFraction);
    Vec3 normal = (hit_pos - body.GetCenterOfMassPosition()).ToVec3();
    if (normal.LengthSq() > std::numeric_limits<float>::epsilon()) {
        normal = normal.Normalized();
    } else {
        normal = Vec3::sAxisY();
    }
    out_hit->normal = from_vec3(normal);
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
