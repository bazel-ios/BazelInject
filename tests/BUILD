load("//tests:rules.bzl", "x_rule")
x_rule(name="x", path="z")

genrule(
    name = "foo",
    srcs = [],
    outs = ["foo.h"],
    cmd = "echo xx > $(OUTS)",
)
