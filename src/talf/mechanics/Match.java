package talf.mechanics;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import talf.mechanics.board.BoardState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Match {
    private BoardState board;

    private BooleanProperty silverTurn;
    private boolean firstTurn = true;

    private int moves;
    private int turns;

    public Match(BoardState board, boolean silverFirst) {
        this(board, silverFirst, true, 0, 0);
    }

    private Match(BoardState board, boolean silverTurn, boolean firstTurn,
                  int moves, int turns) {
        this.board = board;
        this.firstTurn = firstTurn;
        this.moves = moves;
        this.turns = turns;
        this.silverTurn = new SimpleBooleanProperty(this, "it's silver's turn", silverTurn);
    }

    public void reset(BoardState newState, boolean silverFirst) {
        silverTurn.setValue(null);
        firstTurn = true;
        moves = 0;
        board = newState;
        silverTurn.set(silverFirst);
    }

    public Match copy() {
        return new Match(board.copy(), silverTurn.get(), firstTurn, moves, turns);
    }

    public BoardState getBoardCopy() {
        return board.copy();
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

    public int move(Coordinate from, Coordinate to) {
        assert board.canMove(from, to);
        boolean isKing = board.isKing(from);
        boolean isHit = !board.isEmpty(to);
        int r = board.move(from, to);
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

    public int move(Move move) {
        return move(move.getSource(), move.getTarget());
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

    public int getDistanceToBorderKing() {
        return board.distanceToBorderKing();
    }

    public int getHeight() {
        return board.getHeight();
    }

    public ArrayList<Coordinate> create() {
        return board.create();
    }

    public void findMoveMoves(ArrayList<Coordinate> result, Coordinate c) {
        board.findMoveMoves(result, c);
    }

    public void findAttackMoves(List<Coordinate> result, Coordinate c) {
        board.findAttackMoves(result, c);
    }

    public boolean isCenter(Coordinate c) {
        return board.isInCenter(c);
    }

    public int getWidth() {
        return board.getWidth();
    }

    public boolean isKing(Coordinate coordinate) {
        return board.isKing(coordinate);
    }

    public boolean isSilverPiece(Coordinate coordinate) {
        return board.isSilverPiece(coordinate);
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

    public int maxDistToBorder() {
        return board.maxDistToBorder();
    }

    public int countGoldCovered() {
        return board.countGoldCovered();
    }

    public int countGoldCanHit() {
        return board.countGoldCanHit();
    }

    public int countGold() {
        return board.countGold();
    }

    public boolean hasKing() {
        return board.hasKing();
    }

    public boolean isEmpty(Coordinate coordinate) {
        return board.isEmpty(coordinate);
    }

    public boolean canHitKing() {
        return board.canHitKing();
    }

    public double getMaxGoldPieces() {
        return board.getMaxGoldPieces();
    }

    public int countSilver() {
        return board.countSilver();
    }

    public int getMaxSilverPieces() {
        return board.getMaxSilverPieces();
    }

    public boolean isFirsTurn() {
        return firstTurn;
    }

    public boolean isGoldPiece(Coordinate coordinate) {
        return board.isGoldPiece(coordinate);
    }
}
