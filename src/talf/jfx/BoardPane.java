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
import talf.mechanics.Match;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoardPane extends GridPane {
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
    private final Match match;
    private final BorderPane[][] cells;
    private boolean canMoveSilver, canMoveGold;
    private ListProperty<Coordinate> highlighted = new SimpleListProperty<>(
        this, "Highlighted move coordinates", FXCollections.observableArrayList()
    );
    private ListProperty<Coordinate> highlightedAttack = new SimpleListProperty<>(
        this, "Highlighted attack coordinates", FXCollections.observableArrayList()
    );
    private ObjectProperty<Coordinate> selected = new SimpleObjectProperty<>(
        this, "Selected up piece");
    private Runnable onMoved;

    public BoardPane(Match match, boolean canMoveSilver, boolean canMoveGold) {
        this.match = match;
        this.canMoveSilver = canMoveSilver;
        this.canMoveGold = canMoveGold;
        for (int i = 0; i < match.getHeight(); i++) {
            RowConstraints c = new RowConstraints(50);
            getRowConstraints().add(c);
        }
        for (int i = 0; i < match.getWidth(); i++) {
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
        cells = new BorderPane[match.getWidth()][];
        for (int x = 0; x < cells.length; x++) {
            cells[x] = new BorderPane[match.getHeight()];
            for (int y = 0; y < cells[x].length; y++) {
                Coordinate c = new Coordinate(x, y);
                BorderPane cell = new BorderPane();
                if (match.isCenter(c)) {
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
                if (match.isEmpty(c)) {
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
        if (match.isKing(coordinate)) {
            c.setFill(Color.GOLD);
            Line l1 = new Line(radius, 0, radius, 2 * radius);
            l1.setStrokeWidth(radius / 8);
            Line l2 = new Line(0, radius, 2 * radius, radius);
            l2.setStrokeWidth(radius / 8);
            return new StackPane(c, l1, l2);
        }
        if (match.isSilverPiece(coordinate)) {
            c.setFill(Color.SILVER);
        } else {
            c.setFill(Color.GOLD);
        }
        return c;
    }

    private boolean canMovePiece(Coordinate coordinate) {
        if (match.isSilverPiece(coordinate)) {
            return canMoveSilver;
        }
        if (match.isGoldPiece(coordinate)) {
            return canMoveGold;
        }
        return false;
    }

    private void move(Coordinate source, Coordinate target) {
        int win = match.move(source, target);
        if (win > 0) {
            System.out.println("win = " + win);
        }
        BorderPane s = cells[source.x][source.y];
        BorderPane t = cells[target.x][target.y];

        Node n = s.getCenter();
        s.setCenter(null);
        t.setCenter(n);

        PieceHandler pieceHandler = (PieceHandler) n.getOnMouseClicked();
        pieceHandler.c = target;

        onMoved();
    }

    public void onMoved() {
        if (onMoved != null) {
            onMoved.run();
        }
    }

    public void setOnMoved(Runnable onMoved) {
        this.onMoved = onMoved;
    }

    private class PieceHandler implements EventHandler<MouseEvent> {
        private Coordinate c;

        PieceHandler(Coordinate c) {
            this.c = c;
        }

        @Override
        public void handle(MouseEvent event) {
            if (selected.get() != null) {
                return;
            }
            if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
                if (canMovePiece(c)) {
                    selected.set(c);
                }
                event.consume();
                return;
            }
            ArrayList<Coordinate> moves = match.create();
            moves.add(c);
            match.findMoveMoves(moves, c);

            List<Coordinate> attacks;
            if (match.isCanAttach()) {
                attacks = new ArrayList<>(4);
                match.findAttackMoves(attacks, c);
            } else {
                attacks = Collections.emptyList();
            }

            if (event.getEventType() == MouseEvent.MOUSE_ENTERED) {
                highlighted.addAll(moves);
                highlightedAttack.addAll(attacks);
            } else if (event.getEventType() == MouseEvent.MOUSE_EXITED) {
                highlighted.removeAll(moves);
                highlightedAttack.removeAll(attacks);
            }
        }
    }

    private class CellHandler implements EventHandler<MouseEvent> {
        private final Coordinate c;

        public CellHandler(Coordinate c) {
            this.c = c;
        }

        @Override
        public void handle(MouseEvent event) {
            System.out.println("CellHandler.handle:");
            if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
                Coordinate pi = selected.get();
                if (pi != null) {
                    selected.set(null);
                    highlighted.clear();
                    highlightedAttack.clear();
                    if (match.canMove(pi, c)) {
                        // Move
                        System.out.println("\tmove: " + pi + "->" + c);
                        move(pi, c);
                    }
                    event.consume();
                }
            }
        }
    }
}
