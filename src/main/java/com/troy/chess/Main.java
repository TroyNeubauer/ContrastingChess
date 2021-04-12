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

    /**
     * Resizes the existing squares in java fx so that they each have the requested size in pixels
     * @param squarePX
     */
    private static void resizeBoard(Pane board, double widthPX, double heightPX) {
    	double sideLen = Math.sqrt(board.getChildren().size());
    	
        for (int i = 0; i < board.getChildren().size(); i++) {
            Rectangle rectangle = (Rectangle) board.getChildren().get(i);
            if (oldWidth == 0.0) {
                oldWidth = rectangle.getWidth();
                conversionRatio = squarePX / oldWidth;
            }
            rectangle.setX(rectangle.getX() * conversionRatio);
            rectangle.setY(rectangle.getY() * conversionRatio);
            rectangle.setWidth(squarePX);
            rectangle.setHeight(squarePX);
        }
    }

    private static void setupBoard(Pane board, int size, int squarePX) {
        board.getChildren().clear();
        for (int rank = 0; rank < size; rank++) {
            for (int file = 0; file < size; file++) {
                System.out.println("looping " + file + ", " + rank);
                double x = file * squarePX;
                double y = rank * squarePX;
                System.out.println("x: " + x + ", y: " + y);
                Rectangle square = new Rectangle(x, y, squarePX, squarePX);
                Paint color = ((rank % 2) ^ (file % 2)) == 0 ? Color.WHITE : Color.BROWN;
                square.setFill(color);
                board.getChildren().add(square);
            }
        }
    }

    public Main() {
        setupBoard(board, 8, 10);

        Button start = new Button();
        start.relocate(100, 270);
        start.setText("Start");
        start.autosize();

        pane.getChildren().add(board);
        pane.getChildren().add(start);

    }

    @Override
    public void start(Stage stage) {
        stage.setScene(new Scene(pane, 512, 512));
        stage.show();

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            resizeBoard(board, newVal.doubleValue() / 10);
        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            resizeBoard(board, newVal.doubleValue() / 10);
        });

    }

}
