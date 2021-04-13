package com.troy.chess;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;

public class Main extends Application {
    private Pane board = new Pane();
    private Pane pane = new Pane();

    private int boardSize;
    private int osTitleBarSize;

    private ImageView[] pieces;

    private Image KING, ELEPHANT;

    private static double squareX(int size, int file, double squarePX) {
        return squarePX * file;
    }

    private static double squareY(int size, int rank, double squarePX) {
        double boardHeightPX = size * squarePX;
        return boardHeightPX - rank * squarePX;
    }

    /**
     * Resizes the existing squares in java fx so that they each have the requested
     * size in pixels
     * 
     * @param squarePX
     */
    private void resizeBoard(double squarePX) {
        double sideLen = Math.sqrt(this.board.getChildren().size());

        for (int i = 0; i < this.board.getChildren().size(); i++) {
            int rank = i / this.boardSize;
            int file = i % this.boardSize;
            Rectangle rectangle = (Rectangle) board.getChildren().get(i);
            rectangle.setX(squareX(this.boardSize, file, squarePX));
            rectangle.setY(squareY(this.boardSize, rank, squarePX));
            rectangle.setWidth(squarePX);
            rectangle.setHeight(squarePX);

            if (this.pieces[i] != null) {
                ImageView view = this.pieces[i];
                view.setX(squareX(this.boardSize, file, squarePX));
                view.setY(squareY(this.boardSize, rank, squarePX));
                view.setFitWidth(squarePX);
                view.setFitHeight(squarePX);

            }
        }
    }

    private void setupBoard(int squarePX) {
        this.board.getChildren().clear();
        for (int rank = 0; rank < this.boardSize; rank++) {
            for (int file = 0; file < this.boardSize; file++) {
                int i = file + rank * this.boardSize;
                double x = file * squarePX;
                double y = rank * squarePX;
                Rectangle square = new Rectangle(x, y, squarePX, squarePX);
                Paint color = ((rank % 2) ^ (file % 2)) == 0 ? Color.WHITE : Color.BROWN;
                square.setFill(color);
                board.getChildren().add(square);

            }
        }
    }

    public Main() {
        Natives.init();
        this.boardSize = 10;

        this.pieces = new ImageView[this.boardSize * this.boardSize];
        loadImages();

        this.pieces[5] = new ImageView();
        this.pieces[5].setImage(KING);

        this.pieces[52] = new ImageView();
        this.pieces[52].setImage(ELEPHANT);

        setupBoard(10);

        Button start = new Button();
        start.relocate(50, 0);
        start.setText("Start");
        start.autosize();

        pane.getChildren().add(board);
        pane.getChildren().add(start);
        pane.getChildren().add(this.pieces[5]);
        pane.getChildren().add(this.pieces[52]);

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
