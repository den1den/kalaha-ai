package nl.lexram;

import java.util.Iterator;

/**
 * Created by dennis on 26-9-16.
 */
public abstract class Player {
    String name;

    public Player(String name) {
        this.name = name;
    }

    public abstract int getMove(int color, Match match);

    static class AIPlayer extends Player {
        boolean running;

        public AIPlayer(String name) {
            super(name);
        }

        @Override
        public int getMove(int color, Match match) {
            int bestMove = -1;
            int bestRating = Integer.MIN_VALUE;
            int depthFound = -1;
            this.running = true;
            int depth = 1;
            while (running && depth <= 10) {
                //System.out.println("indexing depth "+depth);
                for (Iterator<State.Move> it = match.avaliableMoves(); it.hasNext(); ) {
                    State.Move m = it.next();
                    m.doMove(color, match.rules);
                    int r = AlphaBetaMax(match, color, depth, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    if (r > bestRating) {
                        bestMove = m.moveIndex;
                        bestRating = r;
                        depthFound = depth;
                    }
                    m.undo(color, match.rules);
                }
                depth++;
            }
            //System.out.println("Found best rating " + bestRating + " at depth " + depthFound);
            if (bestMove != -1) {
                return bestMove;
            }
            Iterator<? extends State.Move> iterator = match.avaliableMoves();
            if (!iterator.hasNext()) {
                System.out.println("Deadlock");
                System.out.println(match.state);
                return -1;
            } else {
                System.out.println("No best move found");
                System.out.println(match.state);
            }
            return iterator.next().moveIndex;
        }

        private Integer AlphaBetaMax(Match match, int color, int depthLimit, int depth, int a, int b) {
            Integer rating = rating(color, match.state);
            if (rating == Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            if (depth >= depthLimit) {
                return rating;
            }
            if (!running) {
                return rating;
            }

            for (Iterator<State.Move> it = match.avaliableMoves(); it.hasNext(); ) {
                State.Move m = it.next();
                m.doMove(color, match.rules);
                a = Math.max(a, AlphaBetaMin(match, color, depthLimit, depth + 1, a, b));
                m.undo(color, match.rules);
                if (b <= a) {
                    return a;
                }
            }
            return a;
        }

        private int rating(int color, State state) {
            return state.points[color];
        }

        private Integer AlphaBetaMin(Match match, int color, int depthLimit, int depth, int a, int b) {
            //System.out.println("AlphaBetaMin with Depth " + depth + ", and depth limit " + depthLimit);
            if (depth >= depthLimit) {
                return rating(color, match.state);
            }
            if (!running) {
                return rating(color, match.state);
            }
            for (Iterator<State.Move> it = match.avaliableMoves(); it.hasNext(); ) {
                State.Move m = it.next();
                m.doMove(color, match.rules);
                b = Math.min(b, AlphaBetaMax(match, color, depthLimit, depth + 1, a, b));
                m.undo(color, match.rules);
                if (b <= a) {
                    return a;
                }
            }
            return b;
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public static class Default extends Player {
        int move = -1;

        public Default(String name) {
            super(name);
        }

        @Override
        public int getMove(int color, Match match) {
            return move;
        }

        public void setMove(int move) {
            System.out.println("setMove(" + move + ")");
            this.move = move;
        }

        public int selectedMove() {
            return move;
        }
    }
}
