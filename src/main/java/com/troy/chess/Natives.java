package com.troy.chess;

import java.io.File;

import javafx.application.Platform;

public class Natives {

    static {
        LibraryLoader.load("giga_chess");
    }

    public enum GameType {
        Chess(0, 8), ContrastingChess(1, 10);

        int boardSize;

        GameType(int ordinal, int boardSize) {
            this.boardSize = boardSize;
            if (this.ordinal() != ordinal) {
                throw new RuntimeException("Enum kind " + this.toString() + " was expected to have ordinal: " + ordinal
                        + " instead got assigned ordinal " + this.ordinal() + " from Java!");
            }
        }

    }

    // ============================== Functions Called from Other Java Code
    // ==============================

    private static Main main;
    private static Long currentMove = null;
    private static final Object collectMoveWrangler = new Object();

    public static void init(Main main) {
        // Triggers static block on the first call
        Natives.main = main;
        init_rust();
    }

    /**
     * Passed a move made by a human player using the GUI to a waiting rust game
     * thread
     * 
     * @param move
     */
    public static void giveRustMove(int srcSquare, int destSquare) {
        synchronized (Natives.collectMoveWrangler) {
            Natives.currentMove = packNativeMove(srcSquare, destSquare);
            // Wake up waiting rust thread
            Natives.collectMoveWrangler.notify();
        }
    }

    private static long packNativeMove(int srcSquare, int destSquare) {
        return ((long) srcSquare) << 32 | ((long) destSquare);
    }

    // ================== Functions Implemented in Rust Via JNI ==================

    public static native void init_rust();

    /**
     * Starts a new game with the given game type and algorithms. Returns when the
     * game is complete. During the invocation of this method it will periodically
     * call back into display_move to have the UI show the latest move. If a human
     * player is playing the game, rust will call get_human_move to get the next
     * move from the UI.
     * 
     * @param aAlgorithmName
     * @param bAlgorithmName
     * @param gameType
     * @param gameID         a unique id for the game. The same ID will be passed
     *                       back to display_move et al.
     */
    public static native boolean start_game(String aAlgorithmName, String bAlgorithmName, int gameType, int gameID);

    // ==================== Functions Called From Rust ====================
    // All return true if the game is continuing, false if it has ended
    // Were calling from a different thread so all UI related thing must be enqued
    // to run later on the main thread

    public static boolean display_move(int gameID, int srcSquare, int destSquare) {
        Platform.runLater(() -> {
            main.displayMove(srcSquare, destSquare);
        });
        return gameID == main.getCurrentGameID();
    }

    public static boolean set_square(int gameID, int square, int pieceKind, int color) {
        Platform.runLater(() -> {
            main.setSquare(square, pieceKind, color);
        });

        return gameID == main.getCurrentGameID();
    }

    public static boolean set_board_size(int gameID, int size) {
        Platform.runLater(() -> {
            main.setBoardSize(size);
        });
        return gameID == main.getCurrentGameID();
    }

    /**
     * Called by rust to get the next move from a human player using the GUI
     * 
     * @param side The ID of the player who is to move. 0 for white, 1 for black,
     *             etc. for multiplayer games
     * @return A 64 bit integer containing the source square in the 32 high bits and
     *         the destination square in the 32 low bits - representing the index of
     *         the piece which is to move and its destination index. Called
     *         repeatedly for the same side until a legal move is made
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
                    // Another thread got there before us
                    continue;
                }
                // We have exclusive access and currentMove is set to a move
                long move = Natives.currentMove;
                Natives.currentMove = null;
                return move;
            }
        }
    }

}
