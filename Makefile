all: jar

clean:
	rm -rf bazel-inject

jar: clean
	# Consider moving this to Bazel
	mkdir -p bazel-inject/META-INF
	cp -f MANIFEST.MF bazel-inject/META-INF/MANIFEST.MF
	cp -f tools/jar_injector.sh bazel-inject
	javac -d bazel-inject InjectionManager.java 
	cd bazel-inject && jar cmfv \
		META-INF/MANIFEST.MF BazelInject.jar \
		*.class *.sh

## Development helpers

run:
	echo "Running with jar $$(shasum libmain.jar)"
	bazel shutdown
	bazel build tests/...

