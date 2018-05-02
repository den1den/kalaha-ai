package talf.mechanics;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import talf.mechanics.board.BoardModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Match {
    /**
     * Keeps track of the board and the turn
     */
    public final BoardModel board;

    private BooleanProperty silverTurn;
    private boolean firstTurn = true;

    private int moves;
    private int turns;

    public Match(BoardModel board, boolean silverFirst) {
        this(board, silverFirst, true, 0, 0);
    }

    private Match(BoardModel board, boolean silverTurn, boolean firstTurn,
                  int moves, int turns) {
        this.board = board;
        this.firstTurn = firstTurn;
        this.moves = moves;
        this.turns = turns;
        this.silverTurn = new SimpleBooleanProperty(this, "it's silver's turn", silverTurn);
    }

    public Match copy() {
        return new Match(board.copy(), silverTurn.get(), firstTurn, moves, turns);
    }

    public void getMoves(Map<Coordinate, List<Coordinate>> moves) {
        if (silverTurn.get()) {
            fill(moves, this.board.silverPieces());
        } else {
            fill(moves, this.board.goldSmallPieces());
            if (firstTurn) {
                Coordinate king = this.board.king();
                if (king != null) {
                    ArrayList<Coordinate> kingMoves = board.findMoves(king);
                    if (!kingMoves.isEmpty())
                        moves.put(king, kingMoves);
                }
            }
        }
    }

    public Map<Coordinate, List<Coordinate>> getMoves() {
        Map<Coordinate, List<Coordinate>> moves = new HashMap<>();
        getMoves(moves);
        return moves;
    }

    private void fill(Map<Coordinate, List<Coordinate>> moves, Iterable<Coordinate> i) {
        for (Coordinate next : i) {
            ArrayList<Coordinate> movesOfNext;
            if (firstTurn) {
                movesOfNext = board.findMoves(next);
            } else {
                movesOfNext = board.create();
                board.findMoveMoves(movesOfNext, next);
            }
            if (!movesOfNext.isEmpty()) {
                moves.put(next, movesOfNext);
            }
        }
    }

    public int move(Move move) {
        Coordinate source = move.getSource();
        Coordinate target = move.getTarget();
        assert board.canMove(source, target);
        boolean isKing = board.isKing(source);
        boolean isHit = !board.isEmpty(target);
        int r = board.move(source, target);
        if (silverTurn.get()) {
            if (firstTurn) {
                if (isHit) {
                    nextTurn();
                } else {
                    firstTurn = false;
                }
            } else {
                firstTurn = true;
                nextTurn();
            }
        } else {
            if (firstTurn) {
                if (isKing) {
                    nextTurn();
                } else {
                    if (isHit) {
                        nextTurn();
                    } else {
                        firstTurn = false;
                    }
                }
            } else {
                firstTurn = true;
                nextTurn();
            }
        }
        moves += 1;
        return r;
    }

    protected void nextTurn() {
        turns += 1;
        silverTurn.set(!silverTurn.get());
    }

    public int getTurns() {
        return turns;
    }

    public boolean isTurnSilver() {
        return silverTurn.get();
    }

    public boolean canMove(Coordinate source) {
        if (silverTurn.get()) {
            return board.isSilverPiece(source);
        } else {
            if (firstTurn) {
                return board.isGoldPiece(source);
            } else {
                return board.isSmallGoldPiece(source);
            }
        }
    }

    public boolean canMove(Coordinate source, Coordinate target) {
        if (!canMove(source)) {
            return false;
        }
        if (!firstTurn) {
            // Cannot hit twice
            if (!board.isEmpty(target)) {
                return false;
            }
        }
        return board.canMove(source, target);
    }

    public boolean isCanAttach() {
        return this.firstTurn;
    }

    public BooleanProperty turnSilverProperty() {
        return silverTurn;
    }

    public boolean isTurnGold() {
        return !silverTurn.get();
    }

    public boolean isFirsTurn() {
        return firstTurn;
    }
}
