package marblegame.gamemechanics;

import marblegame.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A match which can be played between players.
 * This class stores the rules that will be used.
 */
public class Match {
    public static final int MOVE_RESULT_WIN = Integer.MAX_VALUE;
    final BoardState boardState;
    final int[] startFields;
    final int[] endFields;
    private final int[] targetAmount;
    private final int[] target2Amount;
    private boolean onlyWinOnOtherTerritory = true;

    public Match(BoardState boardState, int[] startFields, int[] endFields, int[] targetAmount, int[] target2Amount) {
        this.boardState = boardState;
        this.startFields = startFields;
        this.endFields = endFields;
        this.targetAmount = targetAmount;
        this.target2Amount = target2Amount;
    }

    private boolean isWinnerByPoints(int player, BoardState boardState) {
        return getPointsToWin(player, boardState) <= 0;
    }

    public int getPointsToWin(int player) {
        return getPointsToWin(player, boardState);
    }

    public int getPointsToWin(int player, BoardState boardState) {
        int score = boardState.points[player];
        int oppScore = boardState.getMaxOtherPlayerPoints(player);
        int possible = boardState.remainingPoints();

        int maxScore = score + possible;
        int maxOppScore = oppScore + possible;

        if (maxOppScore < score) {
            // Win
            return 0;
        }
        if (maxScore <= oppScore) {
            // Lose
            return -1;
        }
        return (maxOppScore - score) / 2 + 1;
    }

    public BoardState getBoardState() {
        return new BoardState(boardState);
    }

    public boolean canMove(int move) {
        return canMove(move, boardState);
    }

    public boolean canMove(int move, int player) {
        return canMove(move, player, boardState);
    }

    public boolean canMove(int move, BoardState boardState) {
        return canMove(move, boardState.turn, boardState);
    }

    public boolean canMove(int move, int player, BoardState boardState) {
        int min = startFields[player];
        int max = endFields[player];
        return move >= min && move <= max && boardState.fields[move] > 0;
    }

    /**
     * @param boardState
     * @return -1 iff not finished, the winner index otherwise
     */
    int calcWinner(BoardState boardState) {
        if (isWinnerByPoints(boardState.turn, boardState)) {
            return boardState.turn;
        }
        int min = startFields[boardState.turn];
        int max = endFields[boardState.turn];
        for (int i = min; i <= max; i++) {
            if (this.boardState.fields[i] > 0) {
                return -1;
            }
        }
        return 1 - boardState.turn;
    }

    @Override
    public String toString() {
        return boardState.toString();
    }

    public String toString(BoardState board) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < this.startFields.length; i++) {
            s.append(System.lineSeparator()).append(board.toStringPlayer(i));
        }
        return s.toString();
    }


    /**
     * @param moveIndex the moveNow
     * @return the winning amount, or MOVE_RESULT_WIN when is was a winning moveNow
     */
    public int move(int moveIndex) {
        return move(moveIndex, boardState);
    }

    /**
     * @param moveIndex the moveNow
     * @param board on the board
     * @return the winning amount, or MOVE_RESULT_WIN when is was a winning moveNow
     */
    public int move(int moveIndex, BoardState board) {
        BoardState original = new BoardState(board);
        int win = 0;

        // Deplete the field of the moveNow
        int stones = board.fields[moveIndex];
        if (stones <= 0) {
            throw new Error("Illegal moveNow: moveNow " + moveIndex + " on \n" + toString(board));
        }
        board.fields[moveIndex] = 0;

        // Lay the stones on all next fields
        int s = 1;
        int fI;
        do {
            fI = (moveIndex + s) % board.fields.length;
            board.fields[fI]++;
        } while (s++ < stones);

        // Remember the last two fields
        int lastFI = fI;

        if (onlyWinOnOtherTerritory && canMove(lastFI, board)) {
            // The final stones are not taken away
            // There is no win
            win = 0;
        } else {
            // The final stones end up in enemy territory
            if (board.fields[lastFI] == targetAmount[board.turn]) {
                // The final stone matches the target amount
                board.points[board.turn] += board.fields[lastFI];
                board.fields[lastFI] = 0;
                win = 1;

                if (stones > 1) {
                    int nextLastFI = (lastFI == 0 ? board.fields.length - 1 : lastFI - 1);
                    if (board.fields[nextLastFI] == target2Amount[board.turn]) {
                        // The next last stones match the target2 amount
                        if (onlyWinOnOtherTerritory && canMove(nextLastFI, board)) {
                            // The next last stones are of the player itself
                            // So no extra price will be given
                        } else {
                            // Extra points are scores on enemy territory
                            board.points[board.turn] += board.fields[nextLastFI];
                            board.fields[nextLastFI] = 0;
                            win = 2;
                        }
                    }
                }
            }
        }

        if (isWinnerByPoints(board.turn, board)) {
            win = MOVE_RESULT_WIN;
        }

        board.turn++;
        board.turn %= this.startFields.length;

        return win;
    }

    int calcWinner() {
        boolean[] canMove = new boolean[this.startFields.length];
        for (int i = 0; i < canMove.length; i++) {
            canMove[i] = PossibleMoveIterator.from(this, i).hasNext();
        }

        for (int i = 0; i < this.startFields.length; i++) {
            if (isWinnerByPoints(i, boardState)) {
                return i;
            }
            boolean otherBlocked = true;
            for (int i2 = 0; i2 < canMove.length; i2++) {
                if (i != i2 && canMove[i2]) {
                    otherBlocked = false;
                    break;
                }
            }
            if (otherBlocked) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @return iff there cannot happen any moveNow anymore
     */
    public boolean isPad() {
        int min = startFields[boardState.turn];
        int max = endFields[boardState.turn];
        for (int i = min; i < max; i++) {
            if (boardState.fields[i] > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return a possible moveNow with the lowest index
     */
    public int nextPossibleMove() {
        PossibleMoveIterator possibleMoveIterator = PossibleMoveIterator.from(this);
        if (possibleMoveIterator.hasNext()) {
            return possibleMoveIterator.next();
        }
        return -1;
    }

    public int getNPlayer() {
        return boardState.points.length;
    }

    public int getPoints(int player) {
        return boardState.points[player];
    }

    public int[] getFieldsCopy() {
        return boardState.getFieldsCopy();
    }

    public boolean hasWon(int player) {
        return getPointsToWin(player, boardState) == 0;
    }

    public List<Integer> getMoves() {
        List<Integer> moves = new ArrayList<>();
        PossibleMoveIterator iterator = PossibleMoveIterator.from(this);
        while (iterator.hasNext()) {
            moves.add(iterator.next());
        }
        return moves;
    }

    public BoardState getBoardStateAfterMove(int move) {
        BoardState newBs = new BoardState(this.boardState);
        int gain = move(move, newBs);
        return newBs;
    }

    public static class Serializer {
        public static JSONObject toJson(Match match) {
            JSONObject r = new JSONObject();
            r.put("state", BoardState.Serializer.toJson(match.boardState));
            r.put("startFields", Util.toArray(match.startFields));
            r.put("endFields", Util.toArray(match.endFields));
            r.put("targetAmount", Util.toArray(match.targetAmount));
            r.put("target2Amount", Util.toArray(match.target2Amount));
            return r;
        }

        public static Match fromJSONObject(JSONObject object) {
            return new Match(
                    BoardState.Serializer.fromJSONObject((JSONObject) object.get("state")),
                    Util.toArray((JSONArray) object.get("startFields")),
                    Util.toArray((JSONArray) object.get("endFields")),
                    Util.toArray((JSONArray) object.get("targetAmount")),
                    Util.toArray((JSONArray) object.get("target2Amount"))
            );
        }
    }
}
