package com.troy.chess;

import java.io.File;

public class Natives {

    static {
        LibraryLoader.load("giga_chess");
    }

    public enum GameType {
        Chess(0, 8),
        ContrastingChess(1, 10);

        int boardSize;

        GameType(int ordinal, int boardSize) {
            this.boardSize = boardSize;
            if (this.ordinal() != ordinal) {
                throw new RuntimeException("Enum kind " + this.toString() + " was expected to have ordinal: " + ordinal + " instead got assigned ordinal " + this.ordinal() + " from Java!");
            }
        }

    }

    // ============================== Functions Called from Other Java Code ==============================

    private static Main main;
    private static Long currentMove = null;
    private static final Object collectMoveWrangler = new Object();

    public static void init(Main main) {
        //Triggers static block on the first call
        Natives.main = main;
        init_rust();
    }

    /**
     * Passed a move made by a human player using the GUI to a waiting rust game thread
     * @param move
     */
    public static void giveRustMove(int srcSquare, int destSquare) {
        synchronized (Natives.collectMoveWrangler) {
            Natives.currentMove = packNativeMove(srcSquare, destSquare);
            //Wake up waiting rust thread
            Natives.collectMoveWrangler.notify();
        }
        main.displayMove(srcSquare, destSquare);
    }

    private static long packNativeMove(int srcSquare, int destSquare) {
        return ((long) srcSquare) << 32 | ((long) destSquare);
    }


    // ============================== Functions Implemented in Rust Via JNI ==============================

    public static native void init_rust();

    /**
     * Starts a new game with the given game type and algorithms. Returns when the game is complete.
     * During the invocation of this method it will periodically call back into display_move to have the UI show the latest move.
     * If a human player is playing the game, rust will call get_human_move to get the next move from the UI.
     * @param aAlgorithmName
     * @param bAlgorithmName
     * @param gameType
     */
    public static native void start_game(String aAlgorithmName, String bAlgorithmName, int gameType);

    // ============================== Functions Called From Rust ==============================

    public static void display_move(int srcSquare, int destSquare) {
        main.displayMove(srcSquare, destSquare);
    }

    /**
     * Called by rust to get the next move from a human player using the GUI
     * @param side The ID of the player who is to move. 0 for white, 1 for black, etc. for multiplayer games
     * @return A 64 bit integer containing the source square in the 32 high bits and the destination square in the 32 low bits -
     * representing the index of the piece which is to move and its destination index.
     * Called repeatedly for the same side until a legal move is made
     */
    public static long get_human_move(int side) {
        while (true) {
            synchronized (Natives.collectMoveWrangler) {
                if (Natives.currentMove == null) {
                    try {
                        Natives.collectMoveWrangler.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (Natives.currentMove == null) {
                    //Another thread got there before us
                    continue;
                }
                //We have exclusive access and currentMove is set to a move
                long move = Natives.currentMove;
                Natives.currentMove = null;
                return move;
            }
        }
    }

    /**
     * Called when the game starts or after each move is made to inform the UI about the time left for each player
     * @param TODO
     */
    public static void status_update(long TODO) {

    }

}
