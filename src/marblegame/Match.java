package marblegame;

import com.sun.istack.internal.NotNull;
import marblegame.players.Player;
import marblegame.players.RecordedPlayer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by dennis on 2-3-17.
 */
public class Match {
    BoardState board;
    final int[] startFields;
    final int[] endFields;
    final int[] targetAmount;
    final int[] target2Amount;
    Player[] players;
    private boolean onlyWinOnOtherTeritory = true;
    private int lastMove = -1;

    public Match(BoardState board, int[] startFields, int[] endFields, int[] targetAmount, int[] target2Amount, Player[] players) {
        this.board = board;
        this.startFields = startFields;
        this.endFields = endFields;
        this.targetAmount = targetAmount;
        this.target2Amount = target2Amount;
        this.players = players;
    }

    public void setPlayers(Player... players) {
        assert this.getPlayers().length == players.length;
        this.players = players;
    }

    public void setPlayer(int i, Player p) {
        this.getPlayers()[i] = p;
    }

    public boolean isInRange(int move, BoardState board) {
        int min = startFields[board.turn];
        int max = endFields[board.turn];
        return move >= min && move <= max && getBoard().fields[move] > 0;
    }

    public boolean isInRange(int move) {
        return isInRange(move, board);
    }

    int isFinished() {
        return isFinished(board);
    }

    /**
     * @param board
     * @return -1 iff not finished, the winner index otherwise
     */
    int isFinished(BoardState board) {
        if (isPlayerWinner(board)) {
            return board.turn;
        }
        int min = startFields[board.turn];
        int max = endFields[board.turn];
        for (int i = min; i <= max; i++) {
            if (getBoard().fields[i] > 0) {
                return -1;
            }
        }
        return 1 - board.turn;
    }

    private static boolean isPlayerWinner(BoardState board) {
        return board.getPlayerPoints() > board.getMaxOtherPlayerPoints() + board.remainingPoints();
    }

    public Iterator<Integer> getPossibleMoves() {
        return AvailableMoveIterator.from(this);
    }

    /**
     * Do a move by the current player.
     *
     * @return move that is done
     */
    public int move() {
        Player p = players[board.turn];
        lastMove = p.getMove();
        int gain = move(lastMove);
        return gain;
    }

    @Override
    public String toString() {
        return toString(board, true, true);
    }

    public String toString(BoardState board) {
        return toString(board, true, true);
    }

    public String toString(BoardState board, boolean showTurn, boolean showIndices) {
        int prevTurn = board.turn == 0 ? getPlayers().length - 1 : board.turn - 1;
        String s = "";
        for (int i = 0; i < getPlayers().length; i++) {
            Player p = getPlayers()[i];

            if (showIndices && i == 0) {
                s += String.format("                %s\n", board.toStringPlayerIndices(i));
            }

            String boardString;
            if (showTurn && i == prevTurn && p instanceof RecordedPlayer) {
                boardString = board.toStringPlayer(i, ((RecordedPlayer) p).getLastMove());
            } else {
                boardString = board.toStringPlayer(i);
            }
            s += String.format("%15s %s\n", p.getName(), boardString);
        }
        return s;
    }

    int move(int moveIndex) {
        return move(moveIndex, board);
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

    private void nextTurn(BoardState board) {
        board.turn++;
        board.turn %= players.length;
    }

    public BoardState getBoard() {
        return new BoardState(board);
    }

    public Player[] getPlayers() {
        return players;
    }

    public int getLastMove() {
        return lastMove;
    }

    public int getTurn() {
        return board.turn;
    }

    /**
     * Created by dennis on 2-3-17.
     */
    public static class BoardState {
        int[] fields;
        int[] points;
        int turn;

        BoardState(int[] fields, int[] points) {
            this(fields, points, 0);
        }

        BoardState(int[] fields, int[] points, int turn) {
            this.fields = fields;
            this.points = points;
            this.turn = turn;
        }

        public int getMaxOtherPlayerPoints() {
            int max = -1;
            for (int i = 0; i < points.length; i++) {
                if (i != turn) {
                    if (points[i] > max) {
                        max = points[i];
                    }
                }
            }
            return max;
        }

        public int remainingPoints() {
            int sum = 0;
            for (int i = 0; i < fields.length; i++) {
                sum += fields[i];
            }
            return sum;
        }

        public static class TestBoardState extends BoardState {
            public TestBoardState(int[] fields, int[] points, int turn) {
                super(fields, points, turn);
            }
        }

        public BoardState(BoardState boardState) {
            this(
                    Arrays.copyOf(boardState.fields, boardState.fields.length),
                    Arrays.copyOf(boardState.points, boardState.points.length),
                    boardState.turn
            );
        }

        String printLine(int fieldsLow, int fieldsHigh) {
            String r = "";
            for (int i = fieldsLow; i < fieldsHigh; i++) {
                r += "------";
                if (i + 1 < fieldsHigh) r += " ";
            }
            for (int i = fieldsLow; i < fieldsHigh; i++) {
                r += "      ";
                if (i + 1 < fieldsHigh) r += " ";
            }
            for (int i = fieldsLow; i < fieldsHigh; i++) {
                r += String.format("| %2d |", this.fields[i]);
                if (i + 1 < fieldsHigh) r += " ";
            }
            for (int i = fieldsLow; i < fieldsHigh; i++) {
                r += "      ";
                if (i + 1 < fieldsHigh) r += " ";
            }
            for (int i = fieldsLow; i < fieldsHigh; i++) {
                r += "------";
                if (i + 1 < fieldsHigh) r += " ";
            }
            return r;
        }

        String toStringPlayer(int player, int move) {
            String r = "";
            int fieldsPerPlayer = fields.length / this.points.length;
            for (int i = player * fieldsPerPlayer; i < (player + 1) * fieldsPerPlayer; i++) {
                if (i == move) {
                    r += String.format("_%2d_, ", this.fields[i]);
                } else {
                    r += String.format("<%2d>, ", this.fields[i]);
                }
            }
            r += "{" + this.points[player] + "}";
            return r;
        }

        public String toStringPlayerIndices(int player) {
            String r = "";
            int fieldsPerPlayer = fields.length / this.points.length;
            for (int i = player * fieldsPerPlayer; i < (player + 1) * fieldsPerPlayer; i++) {
                r += String.format("<%2d>, ", i);
            }
            return r;
        }

        public String toStringPlayer(int player) {
            return toStringPlayer(player, -1);
        }

        @Override
        public String toString() {
            String r = "Board: " + toStringPlayer(0);
            for (int i = 1; i < this.points.length; i++) {
                r += System.lineSeparator() + "       " + toStringPlayer(i);
            }
            return r;
        }

        public void setNull() {
            this.fields = null;
            this.points = null;
        }

        public int[] getFields() {
            return fields;
        }

        public int getPoints() {
            return points[turn];
        }

        private int getPlayerPoints() {
            return points[turn];
        }
    }

    public static class AvailableMoveIterator implements Iterator<Integer> {
        private final int[] fields;

        private boolean nextSet = false;
        private int current;
        private int max;

        public static AvailableMoveIterator from(@NotNull Match match) {
            return from(match, match.getBoard());
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
    /*

    *//**
     * Created by dennis on 2-3-17.
     *//*
    public static class Rules {
        private int targetAmount = 3;
        private int targetPoints = 3;
        private int target2Amount = 2;
        private int target2Points = 2;
        private int initStones = 4;

        public int[] getInitFields() {
            int[] fields = new int[12];
            Arrays.fill(fields, initStones);
            return fields;
        }

        public int[] getInitPoints() {
            return new int[2];
        }

        public int getFieldsPerPlayer() {
            return 6;
        }

        public int getTargetAmount() {
            return targetAmount;
        }

        public int getTarget2Amount() {
            return target2Amount;
        }

        public int getTargetPoints() {
            return targetPoints;
        }

        public int getTarget2Points() {
            return target2Points;
        }

        public BoardState createBoardState(Player[] players) {
            return new BoardState(players, getInitFields(), getInitPoints());
        }
    }*/
}
