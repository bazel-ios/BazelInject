#!/bin/bash
# A helper file to dynamically load BlazeModules at startup
set -e

readonly REPO_DIR="$( cd "$( dirname "$(dirname "${BASH_SOURCE[0]}" )" )" >/dev/null 2>&1 && pwd )"

jar_injector() {
    local srcroot="$1"

    local user_jar="$INJECT_JAR"
    [[ -f "$user_jar" ]] || (echo "error: missing jar $1" && exit 1)

    local injected_jar_n="injector.server.$(/usr/bin/shasum $user_jar | cut -d ' ' -f1).jar"

    # injecting A-Server.jar inside of this dir
    local wd="$srcroot/bazel-inject/"
    mkdir -p "$wd"
    local injected_jar="$srcroot/bazel-inject/$injected_jar_n"

    if [[ ! -f "$injected_jar" ]]; then
        local bazel_jar="$BAZEL_JAR"
        [[ -f "$bazel_jar" ]] || \
            (echo "missing install $bazel_jar" && exit 1)
        cp "$bazel_jar" "$injected_jar"

        (cd "$wd" && rm -rf injectsrc)
        (cd "$wd" && mkdir injectsrc && cd injectsrc && unzip $user_jar)
        (cd "$wd" && find injectsrc) # Diagnostics
        (cd "$wd" && jar -uf $injected_jar -C injectsrc/ .)
        (cd "$wd" && cp $injected_jar A-Server.jar)
    fi
    echo "injected $injected_jar"
}

jar_injector "$REPO_DIR"
