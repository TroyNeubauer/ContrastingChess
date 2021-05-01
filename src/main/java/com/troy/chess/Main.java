package com.troy.chess;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;

import javafx.application.Application;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class Main extends Application {
    private Pane board = new Pane();
    private VBox root = new VBox();
    private MenuBar mainMenu = new MenuBar();

    private int boardSize;

    private ImageView[] pieces;

    private ArrayList<Image> WHITE_PIECES = new ArrayList<>();
    private ArrayList<Image> BLACK_PIECES = new ArrayList<>();

    /**
     * The index of the last square that was clicked or -1 in no square has been
     * clicked yet. Used for storing the first square clicked when making a move
     */
    private int lastClickedIndex = -1;

    private static double squareX(int size, int file, double squarePX) {
        return squarePX * file;
    }

    private static double squareY(int size, int rank, double squarePX) {
        double boardHeightPX = size * squarePX;
        return boardHeightPX - (rank + 1) * squarePX;
        // (rank + 1) here because rank ranges from 0..(this.boardSize-1) and we want
        // the top rank to be at y=0
    }

    private double lastSquarePX = 10;

    /**
     * Resizes the existing squares in java fx so that they each have the requested
     * size in pixels
     * 
     * @param squarePX How wide and tall in pixels each square should be. Use -1 to
     *                 use the last value
     */
    private void resizeWindow(double squarePX) {
        if (squarePX == -1.0) {
            squarePX = this.lastSquarePX;
            // System.out.println("Using last square px" + squarePX);
        } else {
            this.lastSquarePX = squarePX;
        }

        for (int ii = 0; ii < this.board.getChildren().size(); ii++) {
            final int i = ii;
            Node node = board.getChildren().get(i);
            if (!(node instanceof Rectangle))
                break;

            int rank = i / this.boardSize;
            int file = i % this.boardSize;
            Rectangle rectangle = (Rectangle) node;
            rectangle.setX(squareX(this.boardSize, file, squarePX));
            rectangle.setY(squareY(this.boardSize, rank, squarePX));
            rectangle.setWidth(squarePX);
            rectangle.setHeight(squarePX);

            ImageView piece = this.pieces[i];
            if (piece != null) {
                board.getChildren().remove(piece);
                piece.setX(squareX(this.boardSize, file, squarePX));
                piece.setY(squareY(this.boardSize, rank, squarePX));
                piece.setFitWidth(squarePX);
                piece.setFitHeight(squarePX);
                piece.setOnMouseClicked((event -> {
                    handleClick(i);
                }));
                board.getChildren().add(piece);
                // System.out.println("Got object at " + i);
            }
        }
    }

    private void handleClick(int index) {
        if (this.lastClickedIndex == -1) {
            // start of move source square
            this.lastClickedIndex = index;
            // System.out.println("Storing move: " + index);
        } else {
            // We have a finished move to deal with
            // System.out.println("About to make move " + this.lastClickedIndex + " -> " +
            // index);
            Natives.giveRustMove(this.lastClickedIndex, index);
            this.lastClickedIndex = -1;
        }
    }

    /**
     * Changes the size of the board to a new width. Clearing all pieces in the
     * process
     */
    private void setBoardSize(int boardWidth) {
        this.boardSize = boardWidth;
        this.pieces = new ImageView[this.boardSize * this.boardSize];
        setupBoard(10);
    }

    /**
     * Setups the squares of the board
     * 
     * @param squarePX
     */
    private void setupBoard(int squarePX) {
        this.board.getChildren().clear();
        Color light = Color.color(0xff / 255.0, 0xce / 255.0, 0x9e / 255.0);
        Color dark = Color.color(0xd1 / 255.0, 0x8b / 255.0, 0x47 / 255.0);

        for (int rank = 0; rank < this.boardSize; rank++) {
            for (int file = 0; file < this.boardSize; file++) {
                int i = file + rank * this.boardSize;
                double x = file * squarePX;
                double y = rank * squarePX;
                Rectangle square = new Rectangle(x, y, squarePX, squarePX);
                Paint color = ((rank % 2 ^ file % 2) == 1) ? light : dark;
                square.setFill(color);
                board.getChildren().add(square);
                square.setOnMouseClicked((event) -> {
                    handleClick(i);
                });

            }
        }
    }

    private void setupToolbar() {

        // Create and add the "File" sub-menu options.
        Menu fileMenu = new Menu("File");
        MenuItem importGame = new MenuItem("Import Game");
        importGame.onActionProperty().set((event) -> {
            FileChooser chooser = new FileChooser();
            chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("FEN file", "fen"));
            File file = chooser.showOpenDialog(null);
            if (file == null || !file.exists()) {
                return;
            }
            try {
                String fen = new String(Files.readAllBytes(file.toPath()));
                parseFEN(fen);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Failed to load file: " + file);
                alert.setContentText(e.getClass() + " - " + e.getMessage());
                alert.showAndWait();
            }
        });
        MenuItem exitApp = new MenuItem("Exit");
        fileMenu.getItems().addAll(importGame, exitApp);
        // Create and add the "Edit" sub-menu options.
        Menu edit = new Menu("Edit");
        MenuItem properties = new MenuItem("Properties");
        edit.getItems().add(properties);
        // Create and add the "Help" sub-menu options.
        Menu help = new Menu("Help");
        MenuItem visitRepository = new MenuItem("Visit Repository");
        visitRepository.onActionProperty().set((event) -> {
            getHostServices().showDocument("https://github.com/TroyNeubauer/ContrastingChess");
        });
        help.getItems().add(visitRepository);

        MenuItem reportBug = new MenuItem("Report Bug");
        reportBug.onActionProperty().set((event) -> {
            getHostServices().showDocument("https://github.com/TroyNeubauer/ContrastingChess/issues/new");
        });

        help.getItems().add(reportBug);

        this.mainMenu.getMenus().addAll(fileMenu, edit, help);

        this.root.getChildren().add(this.mainMenu);
    }

    private static final String[] IMAGE_NAMES = new String[] { "king", "queen", "rook", "bishop", "night", "pawn",
            "donkey", "elephant", "moose" };

    private void parseFEN(String fen) {
        // FEN files are in the format:
        // -rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1 (the starting
        // position)
        // -rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2
        // (The board then who is to move, then castling right per side, then the
        // en-passant square, then the move of
        // half moves since the last capture of pawn advance, then the move number)
        int sideLength = 1;
        String[] records = fen.split(" ");
        if (records.length != 6) {
            throw new RuntimeException("FEN file has " + records.length + " expected 6");
        }
        String board = records[0];
        String toMove = records[1];
        String castling = records[2];
        String enPassant = records[3];
        String halfmove = records[4];
        String fullMove = records[5];
        // Count the number of /'s to determine board size
        for (char c : fen.toCharArray()) {
            if (c == '/') {
                sideLength++;
            }
        }
        setBoardSize(sideLength);
        int index = 0;

        // 0-7 for an 8x8 board
        int rank = 7;
        int file = 0;
        while (index < board.length() && rank >= 0) {
            char c = board.charAt(index++);
            if (c == '/') {
                rank--;
                file = 0;

            } else if (Character.isDigit(c)) {
                file += c - '0';

            } else {
                char piece = Character.toLowerCase(c);
                int pieceIndex = -1;
                for (int i = 0; i < IMAGE_NAMES.length; i++) {
                    if (IMAGE_NAMES[i].charAt(0) == piece) {
                        pieceIndex = i;
                    }
                }
                if (pieceIndex == -1) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Invalid FEN file");
                    alert.setContentText("Unknown piece type: " + c + " at byte " + index);
                    alert.showAndWait();
                    return;
                }
                Image image;
                if (Character.isUpperCase(c)) {
                    image = WHITE_PIECES.get(pieceIndex);
                } else {
                    image = BLACK_PIECES.get(pieceIndex);
                }
                this.pieces[rank * sideLength + file] = new ImageView(image);
                file++;
            }
        }
        // Ignore everything after the board for now
        // TODO: implement full parsing

        doResize(this.board.getWidth(), this.board.getHeight());
    }

    public Main() {
        Natives.init(this);
        this.boardSize = 10;

        this.pieces = new ImageView[this.boardSize * this.boardSize];
        loadImages();

        for (int i = 0; i < 50; i++) {
            int pos = (int) (Math.random() * this.pieces.length);
            int piece = (int) (Math.random() * WHITE_PIECES.size());

            if (Math.random() > 0.5) {
                this.pieces[pos] = new ImageView(WHITE_PIECES.get(piece));
            } else {
                this.pieces[pos] = new ImageView(BLACK_PIECES.get(piece));
            }

        }

        setupBoard(10);
        setupToolbar();

        this.root.getChildren().add(this.board);
        this.root.setAlignment(Pos.TOP_CENTER);
        this.root.setFillWidth(true);
        this.board.prefWidthProperty().bind(this.root.widthProperty());
        this.board.prefHeightProperty().bind(this.root.heightProperty());
    }

    public void displayMove(int srcSquare, int destSquare) {
        if (srcSquare == destSquare)
            return;
        ImageView capturedPiece = this.pieces[destSquare];
        this.pieces[destSquare] = this.pieces[srcSquare];
        this.pieces[srcSquare] = null;
        // System.out.println("moved " + srcSquare + " to " + destSquare);
        if (capturedPiece != null) {
            this.board.getChildren().remove(capturedPiece);
        }

        // Refresh the board so that the piece that was just moved is displayed in its
        // new location
        resizeWindow(-1.0);
    }

    static class DoubleHolder {
        double value;
    }

    @Override
    public void start(Stage stage) {

        stage.setScene(new Scene(this.root, 512, 512));
        stage.setTitle("Contrasting Chess by Troy Neubauer");

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            doResize(this.board.getWidth(), this.board.getHeight());
        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            doResize(this.board.getWidth(), this.board.getHeight());
        });

        stage.show();
        doResize(this.board.getWidth(), this.board.getHeight());
    }

    private void doResize(double width, double height) {
        // Always leave one square of padding
        double squareWidthPX = width / this.boardSize;
        double squareHeightPX = height / this.boardSize; // (height - this.osTitleBarSize - this.mainMenuSize) /
                                                         // (this.boardSize);
        double squarePX = Math.min(squareWidthPX, squareHeightPX);

        resizeWindow(squarePX);
    }

    private Image loadImage(String path, boolean errorOk) {
        InputStream stream = this.getClass().getResourceAsStream(path);
        if (stream == null) {
            if (errorOk) {
                return null;
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Failed to load images");
                alert.setHeaderText("Failed to load chess piece images");
                alert.setContentText(
                        "These images are usually located inside the jar file. Something must have gone wrong...");
                alert.showAndWait();
                System.exit(1);
                return null;
            }
        }
        return new Image(stream);
    }

    private void tryPixel(int x, int y, ArrayDeque<Integer> todo, PixelReader reader, int w, int h) {
        if (x >= 0 && x < w && y >= 0 && y < h) {
            todo.push(y * w + x);
        }
    }

    private void loadImages() {
        for (String name : IMAGE_NAMES) {
            WHITE_PIECES.add(loadImage("/contrasting_chess/" + name + ".png", false));
        }

        // Invert colors for black pieces
        for (int i = 0; i < WHITE_PIECES.size(); i++) {
            Image whitePiece = WHITE_PIECES.get(i);
            Image blackImage = loadImage("/contrasting_chess/" + IMAGE_NAMES[i] + "_black.png", true);
            if (blackImage != null) {
                BLACK_PIECES.add(blackImage);
            } else {
                int w = (int) whitePiece.getWidth();
                int h = (int) whitePiece.getHeight();
                WritableImage blackPiece = new WritableImage(w, h);
                PixelWriter writer = blackPiece.getPixelWriter();
                PixelReader writerReader = blackPiece.getPixelReader();
                PixelReader reader = whitePiece.getPixelReader();
                BitSet bitset = new BitSet(w * h);
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        // Retrieving the color of the pixel of the loaded image
                        Color color = reader.getColor(x, y);
                        // Setting the color to the writable image
                        writer.setColor(x, y, color.invert());
                    }
                }
                ArrayDeque<Integer> todo = new ArrayDeque<>();
                todo.add(0);
                // We also want to make the now white boarders on the piece also black
                // So use a simple BFS algorithm to find accessible pixels and make them black
                while (!todo.isEmpty()) {
                    int pos = todo.pop();
                    if (bitset.get(pos)) {
                        continue;
                    }
                    bitset.set(pos, true);
                    int x = pos % w;
                    int y = pos / w;
                    Color color = writerReader.getColor(x, y);
                    if (color.getRed() == 0.0 && color.getGreen() == 0.0 && color.getBlue() == 0.0 && color.getOpacity() == 1.0) {
                        //Don't touch black that we just inverted
                        continue;
                    }
                    if (color.getOpacity() != 0.0) {
                        writer.setColor(x, y, Color.BLACK);
                    }
                    tryPixel(x + 1, y, todo, writerReader, w, h);
                    tryPixel(x, y + 1, todo, writerReader, w, h);
                    tryPixel(x - 1, y, todo, writerReader, w, h);
                    tryPixel(x, y - 1, todo, writerReader, w, h);

                }
                BLACK_PIECES.add(blackPiece);
            }
        }
    }
}

