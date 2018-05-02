package talf.jfx;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;
import talf.mechanics.Coordinate;
import talf.mechanics.board.BoardModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoardView extends GridPane {

    private final BorderPane[][] cells;

    private static final Background NORMAL_BACKGOURND = new Background(
        new BackgroundFill(Color.BLACK, null, null)
    );
    private static final Background HIGHLIGHTED_BACKGOURND = new Background(
        new BackgroundFill(Color.gray(0.2), null, null)
    );
    private static final Background HIGHLIGHTED_ATTACK_BACKGOURND = new Background(
        new BackgroundFill(Color.DARKRED, null, null)
    );
    private static final double NORMAL_STROKE = 0;
    private static final double SELECTED_STROKE = 3;
    private static final Paint STROKE_COLOR = Color.WHITE;

    public BoardView() {
        for (int i = 0; i < board.getHeight(); i++) {
            RowConstraints c = new RowConstraints(50);
            getRowConstraints().add(c);
        }
        for (int i = 0; i < board.getWidth(); i++) {
            ColumnConstraints c = new ColumnConstraints(50);
            getColumnConstraints().add(c);
        }

        Border border = new Border(new BorderStroke(
            Color.LIGHTGRAY,
            BorderStrokeStyle.SOLID,
            null, BorderStroke.THIN
        ));
        Border centerBorder = new Border(new BorderStroke(
            Color.WHITE,
            BorderStrokeStyle.SOLID,
            null, BorderStroke.MEDIUM
        ));
        cells = new BorderPane[board.getWidth()][];
        for (int x = 0; x < cells.length; x++) {
            cells[x] = new BorderPane[board.getHeight()];
            for (int y = 0; y < cells[x].length; y++) {
                Coordinate c = new Coordinate(x, y);
                BorderPane cell = new BorderPane();
                if (board.isInCenter(c)) {
                    cell.setBorder(centerBorder);
                } else {
                    cell.setBorder(border);
                }
                cell.setBackground(NORMAL_BACKGOURND);
                cell.setOnMouseClicked(new CellHandler(c));
                add(cell, x, y);
                cells[x][y] = cell;
            }
        }
        updatePieces();
        //setBackground(NORMAL_BACKGOURND);

        highlighted.addListener(new ListChangeListener<Coordinate>() {
            @Override
            public void onChanged(Change<? extends Coordinate> c) {
                while (c.next()) {
                    for (Coordinate coordinate : c.getRemoved()) {
                        cells[coordinate.x][coordinate.y]
                            .setBackground(NORMAL_BACKGOURND);
                    }
                    for (Coordinate coordinate : c.getAddedSubList()) {
                        cells[coordinate.x][coordinate.y]
                            .setBackground(HIGHLIGHTED_BACKGOURND);
                    }
                }
            }
        });
        highlightedAttack.addListener(new ListChangeListener<Coordinate>() {
            @Override
            public void onChanged(Change<? extends Coordinate> c) {
                while (c.next()) {
                    for (Coordinate coordinate : c.getRemoved()) {
                        cells[coordinate.x][coordinate.y]
                            .setBackground(NORMAL_BACKGOURND);
                    }
                    for (Coordinate coordinate : c.getAddedSubList()) {
                        cells[coordinate.x][coordinate.y]
                            .setBackground(HIGHLIGHTED_ATTACK_BACKGOURND);
                    }
                }
            }
        });

        selected.addListener(new ChangeListener<Coordinate>() {
            @Override
            public void changed(ObservableValue<? extends Coordinate> observable, Coordinate oldValue, Coordinate newValue) {
                if (oldValue != null) {
                    Shape shape = getShape(oldValue);
                    shape.setStrokeWidth(NORMAL_STROKE);
                }
                if (newValue != null) {
                    Shape shape = getShape(newValue);
                    shape.setStrokeWidth(SELECTED_STROKE);
                }
            }
        });
    }

    public void updatePieces() {
        for (int x = 0; x < cells.length; x++) {
            for (int y = 0; y < cells[x].length; y++) {
                Coordinate c = new Coordinate(x, y);
                if (board.isEmpty(c)) {
                    cells[c.x][c.y].setCenter(null);
                } else {
                    Node piece = constructPiece(c);
                    PieceHandler ph = new PieceHandler(c);
                    piece.setOnMouseEntered(ph);
                    piece.setOnMouseExited(ph);
                    piece.setOnMouseClicked(ph);
                    cells[c.x][c.y].setCenter(piece);
                }
            }
        }
    }

    private Shape getShape(Coordinate c) {
        Node piece = cells[c.x][c.y].getCenter();
        assert piece != null;
        Shape shape;
        if (piece instanceof Shape) {
            shape = (Shape) piece;
        } else {
            shape = (Shape) ((Pane) piece).getChildren().get(0);
        }
        return shape;
    }

    private Node constructPiece(Coordinate coordinate) {
        double radius = 15;
        Circle c = new Circle(radius);
        c.setStrokeType(StrokeType.INSIDE);
        c.setStrokeWidth(NORMAL_STROKE);
        c.setStroke(STROKE_COLOR);
        if (board.isKing(coordinate)) {
            c.setFill(Color.GOLD);
            Line l1 = new Line(radius, 0, radius, 2 * radius);
            l1.setStrokeWidth(radius / 8);
            Line l2 = new Line(0, radius, 2 * radius, radius);
            l2.setStrokeWidth(radius / 8);
            return new StackPane(c, l1, l2);
        }
        if (board.isSilverPiece(coordinate)) {
            c.setFill(Color.SILVER);
        } else {
            c.setFill(Color.GOLD);
        }
        return c;
    }


    public void onMoved() {
        if (onMoved != null) {
            onMoved.run();
        }
    }

    public void setOnMoved(Runnable onMoved) {
        this.onMoved = onMoved;
    }

}
