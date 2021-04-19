package com.troy.chess;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LibraryLoader {

    private static final List<LoadingMethod> methods = new ArrayList<>();

    static {
        methods.add(new JavaLibraryPathLoadingMethod());
        methods.add(new CargoBuildFolderLoadingMethod());
        methods.add(new ClasspathLoadingMethod());
    }

    public static void load(String libName) {
        System.out.println("Starting load of native library \"" + libName + "\"");
        for (LoadingMethod method : methods) {
            try {
                boolean success = method.load(libName);
                if (success) {
                    System.out.println("Successfully loaded library \"" + libName + "\" Using " + method.getName());
                    return;
                } else {
                    System.out.println(
                            "Failed to load library using " + method.getName() + " trying next available methods");
                }
            } catch (Exception e) {
                System.out.println("Loading method " + method.getName() + " threw exception while trying to load \"" + libName + "\"!");
                e.printStackTrace();
            }
        }
        throw new RuntimeException("Failed to load library using any method");
    }

    /**
     * Our job is to load a native Rust library. There are a couple different ways
     * we can accomplish this:
     *
     * - If the directory containing the library is in the system's list of search
     * dirs for a dynamic library then load it from there.
     *
     * - If we are in a dev environment, then cargo will generate
     * ./target/debug/lib_name.xxx so we can craft that path and try to load it
     *
     * - Lastly if we are in a jar file then we can try to extract it from the
     * classpath into a temp file and then load the temp file
     *
     *
     * These three methods are expressed using inheritance with one class for each
     * method
     */
    private static abstract class LoadingMethod {
        public abstract boolean load(String libName);

        public abstract String getName();

    }

    private static class JavaLibraryPathLoadingMethod extends LoadingMethod {
        public boolean load(String libName) {
            try {
                // Try search path of java.library.path in case the library is already installed
                // or the user setup their paths nicely
                // See system.loadLibrary documentation for more info
                System.loadLibrary(libName);
                return true;
            } catch (UnsatisfiedLinkError e) {
                return false;
            }
        }

        public String getName() {
            return "java.library.path native library loader";
        }

    }

    private static class CargoBuildFolderLoadingMethod extends LoadingMethod {
        public boolean load(String libName) {

            try {
                // Try cargo rust dev folder
                String path = new File("GigaChess/target/debug/" + System.mapLibraryName(libName)).getAbsolutePath();
                System.load(path);
                return true;
            } catch (UnsatisfiedLinkError e2) {
                return false;
            }

        }

        public String getName() {
            return "Rust build folder library loader";
        }
    }

    private static class ClasspathLoadingMethod extends LoadingMethod {
        public boolean load(String libName) {
            throw new RuntimeException("TODO. Try running outside of a jar using `gradle run`");
        }

        public String getName() {
            return "classpath library loader";
        }

    }

}
