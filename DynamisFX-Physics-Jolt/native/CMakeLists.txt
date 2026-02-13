cmake_minimum_required(VERSION 3.20)
project(dynamisfx_jolt_cshim C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(DYNAMISFX_JOLT_BACKEND "stub" CACHE STRING "Backend implementation to compile (stub or real)")
set_property(CACHE DYNAMISFX_JOLT_BACKEND PROPERTY STRINGS stub real)

if(NOT DYNAMISFX_JOLT_BACKEND STREQUAL "stub" AND NOT DYNAMISFX_JOLT_BACKEND STREQUAL "real")
    message(FATAL_ERROR "DYNAMISFX_JOLT_BACKEND must be one of: stub, real")
endif()

set(DYNAMISFX_JOLT_SOURCES
    src/jolt_jni_bridge.cpp
)

set(DYNAMISFX_JOLT_BACKEND_MODE 0)
if(DYNAMISFX_JOLT_BACKEND STREQUAL "real")
    set(DYNAMISFX_JOLT_BACKEND_MODE 1)
    set(JOLT_SDK_DIR "" CACHE PATH "Path to local Jolt SDK/source tree containing Jolt/Jolt.h")
    if(JOLT_SDK_DIR STREQUAL "")
        message(FATAL_ERROR "JOLT_SDK_DIR must be set when DYNAMISFX_JOLT_BACKEND=real")
    endif()
    if(NOT EXISTS "${JOLT_SDK_DIR}/Jolt/Jolt.h")
        message(FATAL_ERROR "Expected Jolt header not found: ${JOLT_SDK_DIR}/Jolt/Jolt.h")
    endif()
    set(JOLT_LIBRARY "" CACHE FILEPATH "Path to libJolt shared/static library")
    if(JOLT_LIBRARY STREQUAL "")
        message(FATAL_ERROR "JOLT_LIBRARY must be set when DYNAMISFX_JOLT_BACKEND=real")
    endif()
    if(NOT EXISTS "${JOLT_LIBRARY}")
        message(FATAL_ERROR "Expected Jolt library not found: ${JOLT_LIBRARY}")
    endif()
    list(APPEND DYNAMISFX_JOLT_SOURCES src/jolt_c_api_real.cpp)
else()
    list(APPEND DYNAMISFX_JOLT_SOURCES src/jolt_c_api.cpp)
endif()

add_library(dynamisfx_jolt_cshim SHARED
    ${DYNAMISFX_JOLT_SOURCES}
)

find_package(JNI REQUIRED)

target_include_directories(dynamisfx_jolt_cshim
    PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}
    PRIVATE ${JNI_INCLUDE_DIRS}
)

if(DYNAMISFX_JOLT_BACKEND STREQUAL "real")
    find_package(Threads REQUIRED)
    target_include_directories(dynamisfx_jolt_cshim PRIVATE "${JOLT_SDK_DIR}")
    target_link_libraries(dynamisfx_jolt_cshim PRIVATE "${JOLT_LIBRARY}" Threads::Threads)
endif()

target_compile_definitions(dynamisfx_jolt_cshim
    PRIVATE DFX_JOLT_BACKEND_MODE=${DYNAMISFX_JOLT_BACKEND_MODE}
)

if(APPLE)
    set_target_properties(dynamisfx_jolt_cshim PROPERTIES
        OUTPUT_NAME "dynamisfx_jolt_cshim"
    )
elseif(UNIX)
    set_target_properties(dynamisfx_jolt_cshim PROPERTIES
        OUTPUT_NAME "dynamisfx_jolt_cshim"
    )
elseif(WIN32)
    set_target_properties(dynamisfx_jolt_cshim PROPERTIES
        OUTPUT_NAME "dynamisfx_jolt_cshim"
    )
endif()
