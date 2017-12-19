package marblegame.gamemechanics;

import marblegame.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

    public boolean canMove(int move, BoardState boardState) {
        int min = startFields[boardState.turn];
        int max = endFields[boardState.turn];
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
     * @param moveIndex the move
     * @return the winning amount, or MOVE_RESULT_WIN when is was a winning move
     */
    public int move(int moveIndex) {
        return move(moveIndex, boardState);
    }

    /**
     * @param moveIndex the move
     * @param board on the board
     * @return the winning amount, or MOVE_RESULT_WIN when is was a winning move
     */
    public int move(int moveIndex, BoardState board) {
        int win = 0;
        int stones = board.fields[moveIndex];
        if (stones <= 0) {
            throw new Error("Illegal move: move " + moveIndex + " on \n" + toString(board));
        }

        board.fields[moveIndex] = 0;

        int i;
        for (i = moveIndex + 1; i <= moveIndex + stones; i++) {
            board.fields[i % board.fields.length]++;
        }
        i %= board.fields.length;
        int last = (i == 0 ? board.fields.length - 1 : i - 1);
        int nextLast = (last == 0 ? board.fields.length - 1 : last - 1);

        if (onlyWinOnOtherTerritory && canMove(last, board)) {
            // The final stones are not taken away
            // There is no win
            win = 0;
        } else {
            if (board.fields[last] == targetAmount[board.turn]) {
                board.points[board.turn] += board.fields[last];
                board.fields[last] = 0;
                win = 1;

                if (board.fields[nextLast] == target2Amount[board.turn]) {
                    board.points[board.turn] += board.fields[nextLast];
                    board.fields[nextLast] = 0;
                    win = 2;
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
     * @return iff there cannot happen any move anymore
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
     * @return a possible move with the lowest index
     */
    public int nextPossibleMove() {
        PossibleMoveIterator possibleMoveIterator = PossibleMoveIterator.from(this);
        if (possibleMoveIterator.hasNext()) {
            return possibleMoveIterator.next();
        }
        return -1;
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
