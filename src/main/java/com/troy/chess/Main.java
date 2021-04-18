package com.troy.chess;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Main extends Application {
    private BorderPane board = new BorderPane();
    private Pane pane = new Pane();

    private int boardSize;
    private int osTitleBarSize;

    private ImageView[] pieces;

    private Image KING, ELEPHANT;

    /**
     * The index of the last square that was clicked or -1 in no square has been clicked yet.
     * Used for storing the first square clicked when making a move
     */
    private int lastClickedIndex = -1;


    private static double squareX(int size, int file, double squarePX) {
        return squarePX * file;
    }

    private static double squareY(int size, int rank, double squarePX) {
        double boardHeightPX = size * squarePX;
        return boardHeightPX - rank * squarePX;
    }

    private double lastSquarePX = 10;

    /**
     * Resizes the existing squares in java fx so that they each have the requested
     * size in pixels
     * 
     * @param squarePX How wide and tall in pixels each square should be. Use -1 to use the last value
     */
    private void resizeBoard(double squarePX) {
        if (squarePX == -1) {
            squarePX = this.lastSquarePX;
            System.out.println("Using last square px" + squarePX);
        } else {
            this.lastSquarePX = squarePX;
        }
        double sideLen = Math.sqrt(this.board.getChildren().size());

        for (int ii = 0; ii < this.board.getChildren().size(); ii++) {
            final int i = ii;

            int rank = i / this.boardSize;
            int file = i % this.boardSize;
            Rectangle rectangle = (Rectangle) board.getChildren().get(i);
            rectangle.setX(squareX(this.boardSize, file, squarePX));
            rectangle.setY(squareY(this.boardSize, rank, squarePX));
            rectangle.setWidth(squarePX);
            rectangle.setHeight(squarePX);

            ImageView piece = this.pieces[i];
            if (piece != null) {
                pane.getChildren().remove(piece);
                piece.setX(squareX(this.boardSize, file, squarePX));
                piece.setY(squareY(this.boardSize, rank, squarePX));
                piece.setFitWidth(squarePX);
                piece.setFitHeight(squarePX);
                piece.setOnMouseClicked((event -> {
                    handleClick(i);
                }));
                pane.getChildren().add(piece);
                System.out.println("Got object at " + i);
            }
        }
    }

    private void handleClick(int index) {
        if (this.lastClickedIndex == -1) {
            //start of move source square
            this.lastClickedIndex = index;
            System.out.println("Storing move: " + index);
        } else {
            //We have a finished move to deal with
            System.out.println("About to make move " + this.lastClickedIndex + " -> " + index);
            Natives.giveRustMove(this.lastClickedIndex, index);
            this.lastClickedIndex = -1;
        }
    }

    /**
     * Setups the squares of the board
     * @param squarePX
     */
    private void setupBoard(int squarePX) {
        this.board.getChildren().clear();
        for (int rank = 0; rank < this.boardSize; rank++) {
            for (int file = 0; file < this.boardSize; file++) {
                int i = file + rank * this.boardSize;
                double x = file * squarePX;
                double y = rank * squarePX;
                Rectangle square = new Rectangle(x, y, squarePX, squarePX);
                Paint color = ((rank % 2 ^ file % 2) == 1) ? Color.WHITE : Color.BROWN;
                square.setFill(color);
                board.getChildren().add(square);
                square.setOnMouseClicked((event) -> {
                    handleClick(i);
                });

            }
        }
    }

    private void setupToolbar() {
        BorderPane root = new BorderPane();
        VBox topContainer = new VBox(); // Creates a container to hold all Menu Objects.
        MenuBar mainMenu = new MenuBar(); // Creates our main menu to hold our Sub-Menus.
        ToolBar toolBar = new ToolBar(); // Creates our tool-bar to hold the buttons.

        topContainer.getChildren().add(mainMenu);
        topContainer.getChildren().add(toolBar);
        root.setTop(topContainer);

        // Create and add the "File" sub-menu options.
        Menu file = new Menu("File");
        MenuItem openFile = new MenuItem("Open File");
        MenuItem exitApp = new MenuItem("Exit");
        file.getItems().addAll(openFile, exitApp);
        // Create and add the "Edit" sub-menu options.
        Menu edit = new Menu("Edit");
        MenuItem properties = new MenuItem("Properties");
        edit.getItems().add(properties);
        // Create and add the "Help" sub-menu options.
        Menu help = new Menu("Help");
        MenuItem visitWebsite = new MenuItem("Visit Website");
        help.getItems().add(visitWebsite);
        mainMenu.getMenus().addAll(file, edit, help);

        pane.getChildren().add(root);
    }

    public Main() {
        Natives.init(this);
        this.boardSize = 10;

        this.pieces = new ImageView[this.boardSize * this.boardSize];
        loadImages();

        this.pieces[5] = new ImageView();
        this.pieces[5].setImage(KING);

        this.pieces[52] = new ImageView();
        this.pieces[52].setImage(ELEPHANT);

        setupBoard(10);
        setupToolbar();

        pane.getChildren().add(board);
        pane.getChildren().add(this.pieces[5]);
        pane.getChildren().add(this.pieces[52]);
    }

    public void displayMove(int srcSquare, int destSquare) {
        if (srcSquare == destSquare)
            return;
        ImageView capturedPiece = this.pieces[destSquare];
        this.pieces[destSquare] = this.pieces[srcSquare];
        this.pieces[srcSquare] = null;
        System.out.println("moved " + srcSquare + " to " + destSquare);
        if (capturedPiece != null) {
            this.pane.getChildren().remove(capturedPiece);
        }

        //Refresh the board so that the piece that was just moved is displayed in its new location
        resizeBoard(-1.0);
    }

    static class IntHolder {
        int value;
    }

    @Override
    public void start(Stage stage) {
        final IntHolder width = new IntHolder();
        final IntHolder height = new IntHolder();
        width.value = 512;
        height.value = 512;

        stage.setScene(new Scene(pane, width.value, height.value));
        stage.setTitle("Contrasting Chess by Troy Neubauer");
        stage.show();
        this.osTitleBarSize = (int) stage.getHeight() - height.value;
        System.out.println("Window title bar size is: " + this.osTitleBarSize);

        doResize(width.value, height.value);

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            width.value = newVal.intValue();
            doResize(width.value, height.value);
        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            height.value = newVal.intValue();
            doResize(width.value, height.value);
        });

    }

    private void doResize(int width, int height) {
        // Always leave one square of padding
        double squareWidthPX = (double) width / (this.boardSize + 1);
        double squareHeightPX = (double) (height - this.osTitleBarSize) / (this.boardSize + 1);
        double squarePX = Math.min(squareWidthPX, squareHeightPX);

        resizeBoard(squarePX);
    }

    private void loadImages() {
        this.KING = new Image(this.getClass().getResourceAsStream("/contrasting_chess/king.png"));
        this.ELEPHANT = new Image(this.getClass().getResourceAsStream("/contrasting_chess/elephant.png"));
    }
}
