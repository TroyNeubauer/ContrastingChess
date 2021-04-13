package com.troy.chess;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Main extends Application {
    Pane board = new Pane();
    Pane pane = new Pane();

    private int boardSize;
    private int osTitleBarSize;

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
    private static void resizeBoard(Pane board, double squarePX, int size) {
        double sideLen = Math.sqrt(board.getChildren().size());

        for (int i = 0; i < board.getChildren().size(); i++) {
            int rank = i / size;
            int file = i % size;
            Rectangle rectangle = (Rectangle) board.getChildren().get(i);
            rectangle.setX(squareX(size, file, squarePX));
            rectangle.setY(squareY(size, rank, squarePX));
            rectangle.setWidth(squarePX);
            rectangle.setHeight(squarePX);
        }
    }

    private static void setupBoard(Pane board, int size, int squarePX) {
        board.getChildren().clear();
        for (int rank = 0; rank < size; rank++) {
            for (int file = 0; file < size; file++) {
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
        this.boardSize = 10;
        setupBoard(board, this.boardSize, 10);

        Button start = new Button();
        start.relocate(50, 0);
        start.setText("Start");
        start.autosize();

        pane.getChildren().add(board);
        pane.getChildren().add(start);

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
    	//Always leave one square of padding
        double squareWidthPX = (double) width / (this.boardSize + 1);
        double squareHeightPX = (double) (height - this.osTitleBarSize) / (this.boardSize + 1);
        double squarePX = Math.min(squareWidthPX, squareHeightPX);

        resizeBoard(board, squarePX, this.boardSize);
    }
}
