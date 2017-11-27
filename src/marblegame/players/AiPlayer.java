package marblegame.players;

import marblegame.Match;
import marblegame.State;

import java.util.Iterator;

/**
 * Created by dennis on 2-3-17.
 */
public class AiPlayer extends NamedPlayer {

    private static final int DEFAULT_SEARCH_DEPTH = 13;

    private boolean running = false;
    private final Match match;
    private int maxDepth;

    public AiPlayer(String name, Match match, int maxDepth) {
        super(name);
        this.match = match;
        this.maxDepth = maxDepth;
    }

    public AiPlayer(String name, Match match) {
        this(name, match, DEFAULT_SEARCH_DEPTH);
    }

    public int calcMove(int maxDepth) {
        int bestMove = -1;
        int bestRating = Integer.MIN_VALUE;
        int depthFound = -1;
        int depth = 1;
        running = true;

        while (running && depth <= maxDepth) {
            //System.out.println("indexing depth " + depth);
            for (Iterator<Integer> it = Match.AvailableMoveIterator.from(match, match.getState()); it.hasNext(); ) {// newBS correct?
                State newBs = match.getState();
                int move = it.next();
                int win = match.move(move, newBs);

                int r = AlphaBetaMin(newBs, depth, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
                if (r > bestRating) {
                    bestMove = move;
                    bestRating = r;
                    depthFound = depth;
                    System.out.println("bestMove=" + move + " (r=" + bestRating + ", depth=" + depthFound + ")");
                }
            }
            depth++;
        }
        if (bestMove == -1) {
            System.out.println("No best move found");
        }
        return bestMove;
    }

    /**
     * use a-b-pruning
     */
    private int calcMove() {
        return calcMove(maxDepth);
    }

    private Integer AlphaBetaMin(State boardState, int depthLimit, int depth, int a, int b) {
        //System.out.println("AlphaBetaMin with Depth " + depth + ", and depth limit " + depthLimit);
        if (depth >= depthLimit) {
            return rating(boardState);
        }
        if (!running) {
            return rating(boardState);
        }

        Match.AvailableMoveIterator availableMoveIterator = Match.AvailableMoveIterator.from(match, boardState);
        while (availableMoveIterator.hasNext()) {
            int next = availableMoveIterator.next();

            State newBoardState = new State(boardState);
            int move = match.move(next, newBoardState);
            if (move != 0) {
                // Someone won
                // System.err.println("Someone won");
            }

            b = Math.min(b, AlphaBetaMax(newBoardState, depthLimit, depth + 1, a, b));

            //newBoardState.setNull();
            if (b <= a) {
                return a;
            }
        }
        return b;
    }

    private Integer AlphaBetaMax(State boardState, int depthLimit, int depth, int a, int b) {
        int rating = rating(boardState);
        if (!running || depth >= depthLimit) {
            return rating;
        }
        if (rating == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        Match.AvailableMoveIterator moveIterator = Match.AvailableMoveIterator.from(match, boardState);
        while (moveIterator.hasNext()) {
            Integer next = moveIterator.next();
            State newBoardState = new State(boardState);
            int move = match.move(next, newBoardState);
            a = Math.max(a, AlphaBetaMin(newBoardState, depthLimit, depth + 1, a, b));
            if (b <= a) {
                return a;
            }
        }
        return a;
    }

    private int rating(State boardState) {
        return boardState.getPoints();
    }

    @Override
    public int getMove() {
        return calcMove();
    }
}
