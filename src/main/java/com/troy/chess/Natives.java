package com.troy.chess;

import java.io.File;

public class Natives {

    private static final String LIB_NAME = "giga_chess";

    static {
        // Our job is to load the giga chess native Rust library.
        // There are a couple different ways we can do this:
        // - If the directory containing the library is in the system's list of search
        // dirs for a dynamic library then load it from there.
        // - If we are in a dev environment, then cargo will generate
        // target/debug/libname.xxx
        // - Lastly if we are in a jar file then we need to extract it frow the
        // classpath into a temp file and then load that
        //
        try {
            // Try search path of java.library.path
            System.loadLibrary(LIB_NAME);
            System.out.println("Successfully loaded " + LIB_NAME + " from platform specific dirs");

        } catch (UnsatisfiedLinkError e) {

            System.out.println("Failed to load " + LIB_NAME + " from platform specific dirs");
            try {
                // Try cargo rust dev folder
                String path = new File("GigaChess/target/debug/" + System.mapLibraryName(LIB_NAME)).getAbsolutePath();
                System.out.println("Trying to load path: " + path);
                System.load(path);
                System.out.println("Successfully loaded " + LIB_NAME + " from the dev debug build folder");

            } catch (UnsatisfiedLinkError e2) {
                System.out.println("Failed to load " + LIB_NAME + " from the dev debug build folder");
                // TODO load from classpath
                throw new RuntimeException("Native library missing!");
            }
        }
    }

    public static native void init0();

    public static void init() {
        // Nop to trigger class initialization
        init0();
    }

}
