package nl.lexram;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by dennis on 26-9-16.
 */
public class Match {
    State state;
    Player[] players;
    Rules rules;

    public Match(State state, Rules rules, Player... players) {
        this.state = state;
        this.players = players;
        this.rules = rules;
    }

    public static Match construct() {
        Rules rules = new Rules();
        return new Match(
                State.construct(rules),
                rules,
                new Player.AIPlayer("a"),
                new Player.AIPlayer("b")
        );
    }

    public boolean won() {
        return state.stones() == 0;
    }

    public Player[] ranking() {
        Player[] players = Arrays.copyOf(this.players, this.players.length);
        Arrays.sort(players, (o1, o2) -> {
            int i1 = Arrays.asList(this.players).indexOf(o1);
            int i2 = Arrays.asList(this.players).indexOf(o2);
            return this.state.points[i1] - this.state.points[i2];
        });
        return players;
    }

    public Iterable<? extends State.Move> moves(int color) {
        return new Iterable<State.Move>() {
            @Override
            public Iterator<State.Move> iterator() {
                return new MoveIterator(color);
            }
        };
    }

    public State.Move doMove(Player player, int color) {
        int moveIndex = player.getMove(color, this);
        State.Move move = this.state.new Move(moveIndex);
        move.doMove(color, rules);
        return move;
    }

    public Player getPlayer(int index) {
        return this.players[index];
    }

    public int players() {
        return this.players.length;
    }

    private class MoveIterator implements Iterator<State.Move> {
        int i;
        State.Move next;
        final int iEnd;

        public MoveIterator(int color) {
            i = rules.fieldsPerPlayer * color - 1;
            iEnd = i + rules.fieldsPerPlayer;
            this.next = findNext();
        }

        private State.Move findNext() {
            do {
                i++;
                if (i > iEnd) {
                    return null;
                }
                int stones = state.board[i];
                if (stones != 0) {
                    return state.new Move(i);
                }
            } while (true);
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public State.Move next() {
            State.Move next = this.next;
            this.next = findNext();
            return next;
        }
    }
}
