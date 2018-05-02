package talf.jfx;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import talf.mechanics.Coordinate;
import talf.mechanics.board.BoardModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoardController {
    private final BoardView boardView;
    private final BoardModel boardModel;

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

    public BoardController(BoardView boardView, BoardModel boardModel) {
        this.boardView = boardView;
        this.boardModel = boardModel;
    }

    public BoardController(BoardModel board, boolean canMoveSilver, boolean canMoveGold) {
        this.boardModel = board;
        boardView = new BoardView(board, canMoveSilver, canMoveGold);
    }

    private boolean canMovePiece(Coordinate coordinate) {
        if (board.isSilverPiece(coordinate)) {
            return canMoveSilver;
        }
        if (board.isGoldPiece(coordinate)) {
            return canMoveGold;
        }
        return false;
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
            ArrayList<Coordinate> moves = board.create();
            moves.add(c);
            board.findMoveMoves(moves, c);

            List<Coordinate> attacks;
            if (true) {
                //FIXME: this can be done better
                attacks = new ArrayList<>(4);
                board.findAttackMoves(attacks, c);
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
                    if (board.canMove(pi, c)) {
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
