package marblegame.solvers;

import marblegame.gamemechanics.BoardState;
import marblegame.gamemechanics.Match;
import marblegame.gamemechanics.PossibleMoveIterator;

import java.util.Iterator;

/**
 * Created by dennis on 2-3-17.
 */
public class AiSolver2 extends AiSolver {

    private static final int DEFAULT_SEARCH_DEPTH = 13;
    boolean avoidPad = true;

    private int depth = DEFAULT_SEARCH_DEPTH;
    private Match match;

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
        double bestRating = Double.MIN_VALUE;

        for (Iterator<Integer> it = PossibleMoveIterator.from(match, match.getBoardState()); it.hasNext(); ) {
            int move = it.next();

            BoardState newBs = match.getBoardState();
            int gain = match.move(move, newBs);

            double r = AlphaBetaMin(newBs, depth, 1, Double.MIN_VALUE, Double.MAX_VALUE);
            if (r == Double.MIN_VALUE || r > bestRating) {
                bestMove = move;
                bestRating = r;
            }
            System.out.println("moveNow=" + move + ", gain=" + gain + ", rating=" + r);
        }
        System.out.println("Best moveNow @depth=" + depth + " is " + bestMove + "\n");

        // Retrace from here
        BoardState bestBS = match.getBoardState();
        match.move(bestMove, bestBS);
        AlphaBetaMin(bestBS, depth, 1, Double.MIN_VALUE, Double.MAX_VALUE);

        return bestMove;
    }

    private Double AlphaBetaMin(BoardState boardState, int depthLimit, int depth, double a, double b) {
        if (depth >= depthLimit) {
            return rating(boardState);
        }

        PossibleMoveIterator possibleMoveIterator = PossibleMoveIterator.from(match, boardState);
        if (avoidPad && !possibleMoveIterator.hasNext()) {
            // pad -> no winner
            System.out.println("Pad detected for opponent");
            return Double.MIN_VALUE;
        }
        while (possibleMoveIterator.hasNext()) {
            int opponentMove = possibleMoveIterator.next();

            BoardState newBoardState = new BoardState(boardState);
            int gain = match.move(opponentMove, newBoardState);

            if (gain != 0) {
                // Points have been scored
            }
            double abm = AlphaBetaMax(newBoardState, depthLimit, depth + 1, a, b);
            if (b > abm) {
                b = abm;
            }
            if (b <= a) {
                return a;
            }
        }
        return b;
    }

    private Double AlphaBetaMax(BoardState boardState, int depthLimit, int depth, double a, double b) {
        double rating = rating(boardState);
        if (depth >= depthLimit) {
            return rating;
        }

        PossibleMoveIterator moveIterator = PossibleMoveIterator.from(match, boardState);
        if (!moveIterator.hasNext()) {
            // pad -> lose
            System.out.println("Pad detected for ai");
            return Double.MIN_VALUE;
        }
        while (moveIterator.hasNext()) {
            Integer next = moveIterator.next();

            BoardState newBoardState = new BoardState(boardState);
            int gain = match.move(next, newBoardState);

            double abm = AlphaBetaMin(newBoardState, depthLimit, depth + 1, a, b);
            if (a < abm) {
                a = abm;
            }
            if (b <= a) {
                return a;
            }
        }

        return a;
    }

    double rating(BoardState boardState) {
        int points = boardState.getPlayerPoints(0);
        int opponetPoints = boardState.getPlayerPoints(1) + 1;
        double rating = (Math.round((points + 1. / opponetPoints) * 1000) + .0) / 1000 - 1;
        //System.out.println("rating="+rating+", points="+points+"/"+opponetPoints+" for board:\n"+boardState.toStringWithTurn()+"\n");
        return rating;
    }

    private int rating2(BoardState boardState) {
        int pointsNeeded = match.getPointsToWin(boardState.getPrevTurn(), boardState);
        if (pointsNeeded == 0) {
            return Integer.MAX_VALUE;
        }
        int rating = boardState.getOverallMaximalPoints() - pointsNeeded;
        System.out.println("rating=" + rating + ", pointsNeeded=" + pointsNeeded + " for board:\n" + boardState.toStringWithTurn() + "\n");
        return rating;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }


}
