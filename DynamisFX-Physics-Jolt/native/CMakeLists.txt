cmake_minimum_required(VERSION 3.20)
project(dynamisfx_jolt_cshim C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(dynamisfx_jolt_cshim SHARED
    src/jolt_c_api.cpp
)

target_include_directories(dynamisfx_jolt_cshim
    PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}
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
