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
    State state;
    final int[] startFields;
    final int[] endFields;
    final int[] targetAmount;
    final int[] target2Amount;
    private boolean onlyWinOnOtherTeritory = true;

    public Match(State state, int[] startFields, int[] endFields, int[] targetAmount, int[] target2Amount) {
        this.state = state;
        this.startFields = startFields;
        this.endFields = endFields;
        this.targetAmount = targetAmount;
        this.target2Amount = target2Amount;
    }

    private static boolean isPlayerWinner(State state) {
        return state.getPlayerPoints() > state.getMaxOtherPlayerPoints() + state.remainingPoints();
    }

    public boolean isInRange(int move, State board) {
        int min = startFields[board.turn];
        int max = endFields[board.turn];
        return move >= min && move <= max && getState().fields[move] > 0;
    }

    public boolean isInRange(int move) {
        return isInRange(move, state);
    }

    int isFinished() {
        return isFinished(state);
    }

    /**
     * @param board
     * @return -1 iff not finished, the winner index otherwise
     */
    int isFinished(State board) {
        if (isPlayerWinner(board)) {
            return board.turn;
        }
        int min = startFields[board.turn];
        int max = endFields[board.turn];
        for (int i = min; i <= max; i++) {
            if (getState().fields[i] > 0) {
                return -1;
            }
        }
        return 1 - board.turn;
    }

    public boolean isPlayerWinner() {
        return isPlayerWinner(state);
    }

    public Iterator<Integer> getPossibleMoves() {
        return AvailableMoveIterator.from(this);
    }

    @Override
    public String toString() {
        return state.toString();
    }

    public String toString(State board, Player[] players) {
        return toString(board, players, true, true);
    }

    public String toString(State board) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < startFields.length; i++) {
            s.append(System.lineSeparator()).append(board.toStringPlayer(i));
        }
        return s.toString();
    }

    public String toString(State board, Player[] players, boolean showTurn, boolean showIndices) {
        int prevTurn = board.turn == 0 ? startFields.length - 1 : board.turn - 1;
        String s = "";
        for (int i = 0; i < startFields.length; i++) {
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
        return move(moveIndex, state);
    }

    public int move(int moveIndex, State board) {
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

        if (onlyWinOnOtherTeritory && !isInRange(last, board)) {
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

        if (isPlayerWinner(board)) {
            win = Integer.MAX_VALUE;
        }

        nextTurn(board);

        return win;
    }

    private void nextTurn(State board) {
        board.turn++;
        board.turn %= startFields.length;
    }

    public State getState() {
        return new State(state);
    }

    public int getTurn() {
        return state.turn;
    }

    public static class AvailableMoveIterator implements Iterator<Integer> {
        private final int[] fields;

        private boolean nextSet = false;
        private int current;
        private int max;

        public static AvailableMoveIterator from(@NotNull Match match) {
            return from(match, match.getState());
        }

        public static AvailableMoveIterator from(@NotNull Match match, @NotNull State boardState) {
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
            r.put("state", State.Serializer.toJson(match.state));
            r.put("startFields", Util.toArray(match.startFields));
            r.put("endFields", Util.toArray(match.endFields));
            r.put("targetAmount", Util.toArray(match.targetAmount));
            r.put("target2Amount", Util.toArray(match.target2Amount));
            return r;
        }

        public static Match fromJSONObject(JSONObject object) {
            return new Match(
                    State.Serializer.fromJSONObject((JSONObject) object.get("state")),
                    Util.toArray((JSONArray) object.get("startFields")),
                    Util.toArray((JSONArray) object.get("endFields")),
                    Util.toArray((JSONArray) object.get("targetAmount")),
                    Util.toArray((JSONArray) object.get("target2Amount"))
            );
        }
    }
}
