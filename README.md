## BazelInject

BazelInject is an experiment to add advanced capabilities to the Bazel ecosystem
via external APIs. Today you can extend Bazel with Starlark, but said
capabilities aren't possible to achieve via Starlark. Upstreaming the rules to
Bazel would make it too flexible _per proposal rejections_, so add an external
API to Bazel. This is a better alternative than everyone making forks and
promotes reuse.

### Usage

_Bazel inject provides low level Jar injection atm_
```
# Build from source to bazel-inject/BazelInject.jar
git clone https://github.com/bazel-ios/bazelinject.git
make
```

```
# First, build your rules into a Jar file:
# e.g: cd /path/to/bazel
# bazel build src/main/java/com/google/devtools/build/lib/bazel:main

# Then, BazelInject injects /path/to/my.jar into Bazel
--host_jvm_args -Dbazel.inject=/path/to/bazel/bazel-bin/src/main/java/com/google/devtools/build/lib/bazel/libmain.jar
--host_jvm_args -jar
--host_jvm_args /path/to/bazelinject/bazel-inject/BazelInject.jar
```

_Note: if you're developing BazelInject - use a sha in the path - see `tools/bazel` for usage_

## Proposed APIs and longer term goals

### External Java rules API for Starlark

A new set of Java rule APIs with similar ideas as the Starlark API, but based on
`RuleDefinition` to enable most capabilities. The gist is you might include jvm
features in existing Starklark rules e.g. alongside `starklark_library`. For
instance, a custom `RuleDefinition` or APIs like `cc_common`.

_Note: while this may may use `RuleDefinition` - it doesn't mean we'd make it
intra-process or operate in the same way._


### BlazeModule startup option

A new startup Bazel option `-Dbazel.module=` that loads a `BlazeModule` at the
specified path: e.g. 
```
-Dbazel.module=com.my.bazel.module:/path/to/module.jar
```
_`BlazeModule` is useful to add custom spawn strategies, rules, and more_

### Why do you want to give developers advanced capabilities?

Maintainability - touching Bazel providers, rules, or spawns has implicated
serious maintainer thrash and worse, they only work how the Bazel authors want
them to.  This is a similar situation that `rules_ios` has with `rules_apple`
and `Tulsi` - but on a lower level. Dropping the Bazel rules will fix most
issues and reduce complexity

## Why not just cut Bazel releases for rules_ios, Foo, or Bar's usage?

Reuse - developing a set of advanced capabilities we can depend on in
`rules_ios` - and can be integrated by RBE vendors - or others already creating
their own builds. This might mean restricting to the Java rules API.

Finally, some of the rules will hinge on assumptions about actions end to end
e.g. how we build an iOS app in `rules_ios`. If it is beneficial, Bazel
maintainers can optionally adopt the proposed API in `bazelbuild`. _And, I'd
like to include the Java code into `rules_ios` - and cutting releases of the
core jars with `rules_ios`_

