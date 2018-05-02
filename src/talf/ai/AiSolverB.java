package talf.ai;

import javafx.util.Pair;
import marblegame.Util;
import talf.mechanics.Coordinate;
import talf.mechanics.Match;
import talf.mechanics.Move;
import talf.mechanics.board.BoardModel;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.StreamSupport;

public class AiSolverB implements Solver {
    int maxDepth = 7;
    Move bestMove;
    double bestRating;
    SortedMap<Double, List<Match>> rating2Maps = new TreeMap<>();

    @Override
    public Move solve(Match match) {
        long t0 = System.currentTimeMillis();

        bestMove = null;
        bestRating = Double.MIN_VALUE;

        // Get all possible king moves
        int turn = match.getTurns();
        for (Move move1 : Util.pairIterableAsMove(match.getMoves())) {
            Match copy = match.copy();
            int gain = copy.move(move1);
            if (gain == Integer.MAX_VALUE) {
                return move1;
            }
            if (turn == match.getTurns()) {
                for (Move move2 :
                    Util.pairIterableAsMove(match.getMoves())) {
                    Match copy2 = copy.copy();
                    gain = copy2.move(move2);
                    if (gain == Integer.MAX_VALUE) {
                        return move1;
                    }

                    double r = abMin(copy, 1, Double.MIN_VALUE, Double.MAX_VALUE);

                    if (r == Double.MIN_VALUE || r > bestRating) {
                        bestMove = move1;
                        bestRating = r;
                    }
                    printStatistics();
                    rating2Maps.clear();
                }
            } else {
                double r = abMin(copy, 1, Double.MIN_VALUE, Double.MAX_VALUE);

                if (r == Double.MIN_VALUE || r > bestRating) {
                    bestMove = move1;
                    bestRating = r;
                }
                printStatistics();
                rating2Maps.clear();
            }
        }

        long t1 = System.currentTimeMillis();
        System.out.println(
            "currentRating=" + rating(match.board)
                + " bestRating=" + bestRating
                + " move=" + bestMove
                + " time=" + (t1 - t0)
        );
        return bestMove;
    }

    private void printStatistics() {
        if (rating2Maps.size() == 0) {
            return;
        }
        Iterable<Pair<Double, Match>> leafs = Util.flatten(rating2Maps);
        System.out.println(
            "LOW=" + rating2Maps.firstKey()
                + " HIGH=" + rating2Maps.lastKey()
                + " AVG=" + StreamSupport.stream(leafs.spliterator(), false).mapToDouble
                (Pair::getKey).average().getAsDouble()
                + " DISTINCT=" + rating2Maps.size()
                + " CNT=" + rating2Maps.keySet().size()
        );
    }

    private double abMin(Match match, int depth, double a, double b) {
        if (depth >= maxDepth) {
            return onDepthReached(match);
        }

        int turn = match.getTurns();
        Map<Coordinate, List<Coordinate>> allMoves = match.getMoves();
        if (allMoves.isEmpty()) {
            // Opponent pad
            System.out.println("Opponent pad");
            return Double.MIN_VALUE; // win
        }
        for (Move move : Util.pairIterableAsMove(allMoves)) {
            Match copy = match.copy();
            int gain = copy.move(move);
            if (gain == Integer.MAX_VALUE) {
                System.out.println("Opponent win, in one");
                return Double.MAX_VALUE;
            }

            if (turn == match.getTurns()) {
                for (Move move2 : Util.pairIterableAsMove(match.getMoves())) {
                    Match copy2 = copy.copy();
                    gain = copy2.move(move2);
                    if (gain == Integer.MAX_VALUE) {
                        System.out.println("Opponent win, in two");
                        return Double.MAX_VALUE;
                    }
                    double abm = abMax(copy2, depth + 1, a, b);
                    if (b > abm) {
                        b = abm;
                    }
                    if (b <= a) {
                        return a;
                    }
                }
            } else {
                double abm = abMax(copy, depth + 1, a, b);
                if (b > abm) {
                    b = abm;
                }
                if (b <= a) {
                    return a;
                }
            }
        }
        return b;
    }

    private double abMax(Match match, int depth, double a, double b) {
        if (depth >= maxDepth) {
            return onDepthReached(match);
        }

        int turn = match.getTurns();
        Map<Coordinate, List<Coordinate>> allMoves = match.getMoves();
        if (allMoves.isEmpty()) {
            // Self pad
            System.out.println("Self pad seen");
            return Double.MIN_VALUE; // win
        }
        for (Move move : Util.pairIterableAsMove(allMoves)) {
            Match copy = match.copy();
            int gain = copy.move(move);
            if (gain == Integer.MAX_VALUE) {
                return Double.MAX_VALUE;
            }
            if (turn == match.getTurns()) {
                for (Move move2 : Util.pairIterableAsMove(allMoves)) {
                    Match copy2 = match.copy();
                    gain = copy2.move(move2);
                    if (gain == Integer.MAX_VALUE) {
                        return Double.MAX_VALUE;
                    }
                    double abm = abMin(copy, depth + 1, a, b);
                    if (a < abm) {
                        a = abm;
                    }
                    if (b <= a) {
                        return a;
                    }
                }
            }
            double abm = abMin(copy, depth + 1, a, b);
            if (a < abm) {
                a = abm;
            }
            if (b <= a) {
                return a;
            }
        }
        return a;
    }

    private double onDepthReached(Match match) {
        double r = rating(match.board);
        if (rating2Maps.size() > 0) {
            Double higest = rating2Maps.lastKey();
            if (r > higest) {
                System.out.println("r = " + higest + " -> " + r +
                    ", turns = " + match.getTurns());
                System.out.println(match.board);
            }
        }
        Util.putSorted(rating2Maps, r, match.copy());
        return r;
    }

    private double heuristic(BoardModel board) {
        // assume player is gold
        // higher rating, is better play for gold
        if (!board.hasKing()) {
            return -10;
        }
        if (board.canHitKing()) {
            return -5;
        }
        double distanceToBorderKing =
            (double) board.getDistanceToBorderKing() / board.maxDistToBorder();

        int gold = board.countGold();
        double pieces = (double) gold / board.getMaxGoldPieces();
        double covered = (double) board.countGoldCovered() / gold;
        double canhit = (double) board.countGoldCanHit() / gold;
        double rating = 1
            + 9 * (1 - distanceToBorderKing)
            + 3 * canhit
            + covered;
        assert rating >= 0;
        return rating;
    }

    private double rating(BoardModel board) {
        if (!board.hasKing()) {
            return -10;
        }
        double distanceToBorderKing =
            (double) board.getDistanceToBorderKing() / board.maxDistToBorder();
        double gold = (double) board.countGold() / board.getMaxGoldPieces();
        double silver = (double) board.countSilver() / board.getMaxSilverPieces();
        return
            4 * board.countGold()
                - 2 * silver
                + distanceToBorderKing;
    }
}
