package marblegame.players;

import marblegame.Match;

import java.util.Iterator;

/**
 * Created by dennis on 2-3-17.
 */
public class AiPlayer extends NamedPlayer {

    public static final int DEFAULT_SEARCH_DEPTH = 13;

    private boolean running = false;
    final Match match;
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
            for (Iterator<Integer> it = Match.AvailableMoveIterator.from(match, match.getBoard()); it.hasNext(); ) {// newBS correct?
                Match.BoardState newBs = match.getBoard();
                int move = it.next();
                int win = match.move(move, newBs);

                int r = AlphaBetaMin(newBs, depth, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
                if (r > bestRating) {
                    bestMove = move;
                    bestRating = r;
                    depthFound = depth;
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
    public int calcMove() {
        return calcMove(maxDepth);
    }

    private Integer AlphaBetaMin(Match.BoardState boardState, int depthLimit, int depth, int a, int b) {
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

            Match.BoardState newBoardState = new Match.BoardState(boardState);
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

    private Integer AlphaBetaMax(Match.BoardState boardState, int depthLimit, int depth, int a, int b) {
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
            Match.BoardState newBoardState = new Match.BoardState(boardState);
            int move = match.move(next, newBoardState);
            a = Math.max(a, AlphaBetaMin(newBoardState, depthLimit, depth + 1, a, b));
            if (b <= a) {
                return a;
            }
        }
        return a;
    }

    private int rating(Match.BoardState boardState) {
        return boardState.getPoints();
    }

    @Override
    public int getMove() {
        return calcMove();
    }
}
