import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.ClassLoader;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.util.Map;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;

// Injects user jars into Bazel, then starts the server
public class InjectionManager {
    private Thread nativeRuleJVMThread;

    public InjectionManager() {}
   
    private URLClassLoader loadJar(String path) throws Exception {
        URL serverJarURL = new URL(path);
        URLClassLoader loader = new URLClassLoader(
            new URL[] {serverJarURL},
            this.getClass().getClassLoader()
        );
        return loader;
    }

    // Injects injectJar into baseJar
    int injectJar(String injectorPath, String baseJar, String injectJar) throws Exception {
        String[] command = {"/bin/bash", "-c", "source " + injectorPath};
        ProcessBuilder pb = new ProcessBuilder(command);
        // Consider refactoring this a bit
        Map<String, String> env = pb.environment();
        env.put("BAZEL_JAR", baseJar);
        if (injectJar != null) {
            env.put("INJECT_JAR", injectJar);
        }

        Process cmdProc = pb.start();
        BufferedReader stdoutReader = new BufferedReader(
                new InputStreamReader(cmdProc.getInputStream()));
        String line;
        while ((line = stdoutReader.readLine()) != null) {
           System.out.println(line);
        }

        BufferedReader stderrReader = new BufferedReader(
                 new InputStreamReader(cmdProc.getErrorStream()));
        while ((line = stderrReader.readLine()) != null) {
           System.err.println(line);
        }
        cmdProc.waitFor();
        return cmdProc.exitValue();
    }

    // Load the injector script from this archive
    String unpackInjector() throws Exception {
        File folder = new File("bazel-inject/");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String injector = "jar_injector.sh";
        File injectorF = new File(folder, injector);
        FileOutputStream output = new FileOutputStream(injectorF);
        int bytesRead = 0;
        byte[] buffer = new byte[4096];
        InputStream input = InjectionManager.class.getResourceAsStream(injector);
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        return injectorF.getPath();
    }

    void callMain(String workspaceRoot, String[] serverArgs, String baseJar, String injectJar) throws Exception {
        String injector = unpackInjector();
        int injectStatus = injectJar(injector, baseJar, injectJar);
        if (injectStatus != 0) {
            throw new java.lang.Error("Failed to inject" + baseJar);
        }

        // Load the injected server
        String server = "file://" + workspaceRoot + "/bazel-inject/A-Server.jar";
        URLClassLoader loader = loadJar(server);
        
        // src/main/java/com/google/devtools/build/lib/bazel/Bazel.java
        Class classToLoad = Class.forName("com.google.devtools.build.lib.bazel.Bazel", true, loader);
        Method method = classToLoad.getMethod("main", java.lang.String[].class);
        System.err.println("[INFO] loaded injector class..");
        method.invoke(null, new Object[]{serverArgs});
    }

    // The gist of this method is that we'll call Bazel's main - giving us the
    // ability to govern what modules are loaded: replacing/adding our own
    public void startBazel(String[] args) throws Exception {
        System.err.println("[Info] Bazelwrapper starting");
        
        if (args.length < 3) {
            throw new java.lang.Error("Invalid args" + Arrays.toString(args));
        }

        // Get/drop JVM args: e.g. [-jar, /var/tmp/_bazel_$x/install/$yx/A-server.jar,..]
        // These 2 lines have assuptions on Bazel CLI / invocation process
        String bazelJar = args[1];
        String[] serverArgs = Arrays.copyOfRange(args, 2, args.length);

        // TODO: Update to proposed API: 
        // A new --blaze_module starup option - to define a jar
        // --blaze_module=com.my.bazel.module=/path/to/module.jar
        String injectJar = null;
        for (int i = 0; i < serverArgs.length; i++) {
            String arg = serverArgs[i];
            if (arg.startsWith("--host_jvm_args=-Dbazel.inject=") && arg.endsWith(".jar")) {
                injectJar = arg.substring(arg.lastIndexOf("=") + 1, arg.length());
                break;
            }
        }
        // Assume they launch from the workspace dir
        String workspaceDir = System.getProperty("user.dir");
        callMain(workspaceDir, serverArgs, bazelJar, injectJar);
    }

    public static void main(java.lang.String[] args) {
        InjectionManager im = new InjectionManager();
        try {
            im.startBazel(args);
        } catch(Exception e) {
            System.err.println("[ERROR] Failed");
            System.err.println(e);
            System.exit(1);
        }
    }
}
