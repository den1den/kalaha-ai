package marblegame;

import com.sun.istack.internal.NotNull;
import marblegame.players.Player;
import marblegame.players.RecordedPlayer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by dennis on 2-3-17.
 */
public class Match {
    BoardState boardState;
    final int[] startFields;
    final int[] endFields;
    final int[] targetAmount;
    final int[] target2Amount;
    private boolean onlyWinOnOtherTeritory = true;

    public Match(BoardState boardState, int[] startFields, int[] endFields, int[] targetAmount, int[] target2Amount) {
        this.boardState = boardState;
        this.startFields = startFields;
        this.endFields = endFields;
        this.targetAmount = targetAmount;
        this.target2Amount = target2Amount;
    }

    private static boolean isPlayerWinnerByPoints(BoardState boardState) {
        return boardState.getPlayerPoints() > boardState.getMaxOtherPlayerPoints() + boardState.remainingPoints();
    }

    public boolean isInRange(int move, BoardState board) {
        int min = startFields[board.turn];
        int max = endFields[board.turn];
        return move >= min && move <= max && getBoardState().fields[move] > 0;
    }

    public boolean isInRange(int move) {
        return isInRange(move, boardState);
    }

    int isFinished() {
        return isFinished(boardState);
    }

    private static boolean isPlayerWinnerByPoints(BoardState boardState, int player) {
        return boardState.getPlayerPoints(player) > boardState.getMaxOtherPlayerPoints(player) + boardState.remainingPoints();
    }

    int getNPlayers() {
        return startFields.length;
    }

    /**
     * @param board
     * @return -1 iff not finished, the calcWinner index otherwise
     */
    int isFinished(BoardState board) {
        if (isPlayerWinnerByPoints(board)) {
            return board.turn;
        }
        int min = startFields[board.turn];
        int max = endFields[board.turn];
        for (int i = min; i <= max; i++) {
            if (getBoardState().fields[i] > 0) {
                return -1;
            }
        }
        return 1 - board.turn;
    }

    public boolean isPlayerWinnerByPoints(int player) {
        return isPlayerWinnerByPoints(boardState, player);
    }

    public boolean isPlayerWinnerByPoints() {
        return isPlayerWinnerByPoints(boardState);
    }

    public Iterator<Integer> getPossibleMoves() {
        return AvailableMoveIterator.from(this);
    }

    @Override
    public String toString() {
        return boardState.toString();
    }

    public String toString(BoardState board, Player[] players) {
        return toString(board, players, true, true);
    }

    public String toString(BoardState board) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < getNPlayers(); i++) {
            s.append(System.lineSeparator()).append(board.toStringPlayer(i));
        }
        return s.toString();
    }

    public String toString(BoardState board, Player[] players, boolean showTurn, boolean showIndices) {
        int prevTurn = board.turn == 0 ? getNPlayers() - 1 : board.turn - 1;
        String s = "";
        for (int i = 0; i < getNPlayers(); i++) {
            Player p = players[i];

            if (showIndices && i == 0) {
                s += String.format("                %s\n", board.toStringPlayerIndices(i));
            }

            String boardString;
            if (showTurn && i == prevTurn && p != null && p instanceof RecordedPlayer) {
                boardString = board.toStringPlayer(i, ((RecordedPlayer) p).getLastMove());
            } else {
                boardString = board.toStringPlayer(i);
            }
            s += String.format("%15s %s\n", p.getName(), boardString);
        }
        return s;
    }

    public int move(int moveIndex) {
        return move(moveIndex, boardState);
    }

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

        if (onlyWinOnOtherTeritory && isInRange(last, board)) {
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

        if (isPlayerWinnerByPoints(board)) {
            win = Integer.MAX_VALUE;
        }

        nextTurn(board);

        return win;
    }

    private void nextTurn(BoardState board) {
        board.turn++;
        board.turn %= getNPlayers();
    }

    public BoardState getBoardState() {
        return new BoardState(boardState);
    }

    public int getTurn() {
        return boardState.turn;
    }

    int calcWinner() {
        boolean[] canMove = new boolean[getNPlayers()];
        for (int i = 0; i < canMove.length; i++) {
            canMove[i] = AvailableMoveIterator.from(this, i).hasNext();
        }

        for (int i = 0; i < getNPlayers(); i++) {
            if (isPlayerWinnerByPoints(i)) {
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

    public static class AvailableMoveIterator implements Iterator<Integer> {
        private final int[] fields;

        private boolean nextSet = false;
        private int current;
        private int max;

        public static AvailableMoveIterator from(Match match, int player) {
            int min = match.startFields[player];
            int max = match.endFields[player];
            return new AvailableMoveIterator(match.getBoardState().fields, min, max);
        }

        public static AvailableMoveIterator from(@NotNull Match match) {
            return from(match, match.getBoardState());
        }

        public static AvailableMoveIterator from(@NotNull Match match, @NotNull BoardState boardState) {
            int min = match.startFields[boardState.turn];
            int max = match.endFields[boardState.turn];
            return new AvailableMoveIterator(boardState.fields, min, max);
        }

        AvailableMoveIterator(final int[] fields, int current, int max) {
            this.fields = fields;
            this.current = current;
            this.max = max;
            if (current < 0) {
                throw new IndexOutOfBoundsException();
            }
            if (max >= fields.length) {
                throw new IndexOutOfBoundsException();
            }
        }

        private boolean findNext() {
            while (true) {
                if (current > max) {
                    nextSet = false;
                    return false;
                }
                int stones = fields[current];
                if (stones != 0) {
                    nextSet = true;
                    return true;
                }
                current++;
            }
        }

        @Override
        public boolean hasNext() {
            return nextSet || findNext();
        }

        @Override
        public Integer next() {
            if (!this.nextSet && !findNext()) {
                throw new NoSuchElementException();
            } else {
                this.nextSet = false;
                int next = current;
                current++;
                return next;
            }
        }
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
