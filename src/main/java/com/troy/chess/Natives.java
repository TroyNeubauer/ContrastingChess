package com.troy.chess;

public class Natives {
    
    static {
        System.load("giga_chess");
    }

    public static void init() {
        //Nop to trigger class initialization
    }


    public static native void test();



}
