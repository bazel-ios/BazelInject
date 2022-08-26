## BazelInject

BazelInject is an experiment to add advanced capabilities to the Bazel ecosystem
via external APIs. Today you can extend Bazel with Starlark, but said
capabilities aren't possible to achieve via Starlark. Upstreaming the rules to
Bazel would make it too flexible _per proposal rejections_, so enable it via
_external_ APIs. This is the alternative to doing it in ad-hoc forks of Bazel
and promotes reuse.

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

### External "native" API's for Starlark

Today you can add a macro or rule in starlark, these APIs enable your starlark
rules to interface with Bazel internals how you see fit. _think [post analysis
actions](https://docs.google.com/document/d/16iogGwUlISoN2WLha2TAaUdpYCjRiVQ2sRQ7--INxkg/edit#heading=h.9bo6b686lx37)
and other concepts you want but won't go into Bazel._

This is a new set of APIs with similar ideas as the Starlark API, but enables an
interface to primitives _like_ `Action`, `RuleDefinition`, and `Artifact` to
unlock advanced capabilities. The gist is you might include jvm features in
existing Starklark rules e.g. alongside `starklark_library`. For instance, a
making your own add "build" APIs like `cc_common`, your own rules like
`objc_library` based on `RuleDefinition`.

_Note: while this may use similar concepts or directly use primitives like `RuleDefinition`, or `Spawn`, - it
doesn't mean we'd make it intra-process or operate in the same way._

### BlazeModule subset hooks

A new startup Bazel option `-Dbazel.module=` that loads a `BlazeModule` or
similar at the specified path: e.g. 
```
-Dbazel.module=com.my.bazel.module:/path/to/module.jar
```
_`BlazeModule` is low level way to add custom spawn strategies, rules, and more.
This API could start with a subset of BlazeModule_

## Why do you want to give developers advanced capabilities?

Flexibility - there are key features community build system engineers would like
to add. The capabilites we'll enable here aren't suited for Bazel's core
codebase, release workflow, or PR review process.

Maintainability - interfacing with native Bazel providers, rules, and spawns is
piviotal to using it at first, but for more advanced ecosystems implicates
maintainer thrash and has severe limitations.

## Why not just cut Bazel releases for rules_ios, Foo, or Bar's usage?

Reuse - developing a set of advanced capabilities we can depend on in
`rules_ios` - and can be integrated by RBE vendors - or others already creating
their own builds. This might mean restricting to the Java rules API.

Finally, some of the rules will hinge on assumptions about actions end to end
e.g. action refinement based on how we build an iOS app with `rules_ios`.  _I'd
also want to provide optional features that call native functionaity directly in
`rules_ios` e.g. flipping on optimizations backed by JVM bytecode and these
APIs`_

## Use cases

[Powerful APIs](https://github.com/bazel-ios/BazelInject#external-java-rules-api-for-starlark) and/or adding rules via `BlazeModule`, jar injection, enable a number of features like:

- [Post analysis actions](https://docs.google.com/document/d/16iogGwUlISoN2WLha2TAaUdpYCjRiVQ2sRQ7--INxkg/edit#heading=h.9bo6b686lx37)
- Comprehensive integration of [rules_ios Virtual frameworks](https://github.com/bazel-ios/rules_ios/pull/277)
- Variants of C++ header scanning
