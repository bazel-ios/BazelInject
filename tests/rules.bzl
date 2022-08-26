def _repo_impl(ctx):
    print("PATH", ctx.attr.path)
    ctx.execute(["mkdir", "-p", "external/" + ctx.name])
    ctx.execute(["touch", "BUILD"])

x_repo = repository_rule(
    implementation = _repo_impl,
    local = True,
    # fragments = [],
    attrs = {
        "path": attr.label(
            mandatory = True,
        ),
    },
)

def _rule_impl(ctx):
    print("PATH", ctx.attr.path)
    # Here we call our custom swift_common
    # print("PKG", swift_common.compile(copts=["Some", "Other"]))

x_rule = rule(
    implementation = _rule_impl,
    attrs = {
        "path": attr.string(
            mandatory = True,
        ),
    },
)
