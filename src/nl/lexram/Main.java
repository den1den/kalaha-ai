package nl.lexram;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Main {

    static class Turn {
        int color;
        State state;

        public Turn(int color, State state) {
            this.color = color;
            this.state = state;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Turn turn = (Turn) o;

            if (color != turn.color) return false;
            return state.equals(turn.state);

        }

        @Override
        public int hashCode() {
            int result = color;
            result = 31 * result + state.hashCode();
            return result;
        }
    }

    public static void main(String[] args) {
        Match match = Match.construct();
        Player one = match.players[0];
        Player two = match.players[1];
        Set<Turn> seen = new HashSet<>();
        while (!match.won()) {
            assert seen.add(new Turn(0, match.state));
            int move = one.getMove(0, match);
            match.state.new Move(move).doMove(0, match.rules);
            System.out.println("Player " + one + " does " + move);
            System.out.println(match.state);
            System.out.println();

            assert seen.add(new Turn(1, match.state));
            move = two.getMove(1, match);
            match.state.new Move(move).doMove(0, match.rules);
            System.out.println("Player " + two + " does " + move);
            System.out.println(match.state);
            System.out.println();
            System.out.println();
        }
        System.out.println("Game finished. Ranking: " + Arrays.toString(match.ranking()));
    }
}
