cmake_minimum_required(VERSION 3.22.1)
project("shizukuinjector")

add_library(
        shizukuinjector
        SHARED
        native-lib.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        shizukuinjector
        ${log-lib}
)
