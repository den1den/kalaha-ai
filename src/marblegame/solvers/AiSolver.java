package marblegame.solvers;

import marblegame.gamemechanics.BoardState;
import marblegame.gamemechanics.Match;
import marblegame.gamemechanics.PossibleMoveIterator;

import java.util.Iterator;

/**
 * Created by dennis on 2-3-17.
 */
public class AiSolver implements Solver {

    public static final int DEFAULT_SEARCH_DEPTH = 13;
    boolean running = false;
    boolean avoidPad = true;

    private int maxDepth;
    private Match match;

    public AiSolver() {
        this(DEFAULT_SEARCH_DEPTH);
    }

    public AiSolver(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    /**
     * use a-b-pruning
     */
    @Override
    public int solve(Match match) {
        this.match = match;
        return calcMove();
    }

    public int calcMove() {
        int bestMove = -1;
        int bestRating = Integer.MIN_VALUE;
        int depthFound = -1;
        int depth = 1;
        running = true;

        while (running && depth <= maxDepth) {
            //System.out.println("indexing depth " + depth);
            for (Iterator<Integer> it = PossibleMoveIterator.from(match, match.getBoardState()); it.hasNext(); ) { // newBS correct?
                BoardState newBs = match.getBoardState();
                int move = it.next();
                int gain = match.move(move, newBs);

                int r = AlphaBetaMin(newBs, depth, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
                if (r > bestRating) {
                    bestMove = move;
                    bestRating = r;
                    depthFound = depth;
                    System.out.println("bestMove=" + bestMove + " (r=" + bestRating + ", depth=" + depthFound + ")");
                }
            }
            depth++;
        }
        if (bestMove == -1) {
            System.out.println("No best move found");
        }
        System.out.println("bestMove=" + bestMove + ", gain = " + bestRating);
        return bestMove;
    }

    private Integer AlphaBetaMin(BoardState boardState, int depthLimit, int depth, int a, int b) {
        //System.out.println("AlphaBetaMin with Depth " + depth + ", and depth limit " + depthLimit);
        if (depth >= depthLimit) {
            return rating(boardState);
        }
        if (!running) {
            return rating(boardState);
        }

        PossibleMoveIterator possibleMoveIterator = PossibleMoveIterator.from(match, boardState);
        if (avoidPad && !possibleMoveIterator.hasNext()) {
            // pad -> no winner
            return Integer.MIN_VALUE;
        }
        while (possibleMoveIterator.hasNext()) {
            int next = possibleMoveIterator.next();

            BoardState newBoardState = new BoardState(boardState);
            int gain = match.move(next, newBoardState);
            if (gain != 0) {
                // Points have been scored
            }

            b = Math.min(b, AlphaBetaMax(newBoardState, depthLimit, depth + 1, a, b));

            //newBoardState.setNull();
            if (b <= a) {
                return a;
            }
        }
        return b;
    }

    private Integer AlphaBetaMax(BoardState boardState, int depthLimit, int depth, int a, int b) {
        int rating = rating(boardState);
        if (!running || depth >= depthLimit) {
            return rating;
        }
        if (rating == Integer.MAX_VALUE) {
            System.out.println("Sees a max");
            return Integer.MAX_VALUE;
        }

        PossibleMoveIterator moveIterator = PossibleMoveIterator.from(match, boardState);
        while (moveIterator.hasNext()) {
            Integer next = moveIterator.next();
            BoardState newBoardState = new BoardState(boardState);
            int gain = match.move(next, newBoardState);
            a = Math.max(a, AlphaBetaMin(newBoardState, depthLimit, depth + 1, a, b));
            if (b <= a) {
                return a;
            }
        }
        return a;
    }

    private int rating(BoardState boardState) {
        return boardState.getPointsOfPlayer();
    }

    public void setMaxDepth(int depth) {
        this.maxDepth = depth;
    }
}
