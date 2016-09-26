package nl.lexram;

import java.util.Arrays;

/**
 * Created by dennis on 26-9-16.
 */
public class State {
    final int[] board;
    final int[] points;

    public State(int[] board, int[] points) {
        this.board = board;
        this.points = points;
    }

    public static State construct(Rules rules) {
        int[] board = new int[rules.fields()];
        Arrays.fill(board, rules.initStonesPerFields);
        return new State(board, new int[rules.players]);
    }

    public int stones() {
        int total = 0;
        for (int i = 0; i < board.length; i++) {
            total += board[i];
        }
        return total;
    }

    @Override
    public String toString() {
        String r = "Board: " + toStringPlayer(0);
        for (int i = 1; i < this.points.length; i++) {
            r += System.lineSeparator() + "       " + toStringPlayer(i);
        }
        return r;
    }

    public String toStringPlayer(int player) {
        String r = "";
        int fieldsPerPlayer = this.board.length / this.points.length;
        for (int i = player * fieldsPerPlayer; i < (player + 1) * fieldsPerPlayer; i++) {
            r += "[" + this.board[i] + "], ";
        }
        r += "{" + this.points[player] + "}";
        return r;
    }

    public class Move {
        int moveIndex;
        int stones = -1;
        int win = -1;

        public Move(int index) {
            this.moveIndex = index;
        }

        public void undo(int color, Rules rules) {
            if (moveIndex == -1) {
                return;
            }
            if (win >= 2) {
                points[color] -= rules.target2Points;
                board[(moveIndex + stones + 2) % board.length] = rules.target2Amount;
            }
            if (win >= 1) {
                points[color] -= rules.targetPoints;
                board[(moveIndex + stones + 1) % board.length] = rules.targetAmount;
            }
            for (int i = moveIndex + stones; i > moveIndex; i--) {
                board[i % board.length]--;
            }
            board[moveIndex] = stones;
        }

        public void doMove(int color, Rules rules) {
            if (moveIndex == -1) {
                return;
            }
            stones = board[moveIndex];
            if (stones == 0) {
                throw new Error("Illegal move");
            }
            board[moveIndex] = 0;
            int i;
            for (i = moveIndex + 1; i <= moveIndex + stones; i++) {
                board[i % board.length]++;
            }
            int last = i % board.length;
            int stonesLast = board[last];
            if (stonesLast == rules.targetAmount) {
                points[color] += rules.targetPoints;
                board[last] = 0;
                win = 1;

                last = (last + 1) % board.length;
                stonesLast = board[last];
                if (stonesLast == rules.target2Amount) {
                    points[color] += rules.target2Points;
                    board[last] = 0;
                    win = 2;
                }
            }
        }

        @Override
        public String toString() {
            if (this.win > 0) {
                return this.moveIndex + " (+" + this.win + ")";
            } else {
                return this.moveIndex + "";
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        State state = (State) o;

        if (!Arrays.equals(board, state.board)) return false;
        return Arrays.equals(points, state.points);

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(board);
        result = 31 * result + Arrays.hashCode(points);
        return result;
    }
}
