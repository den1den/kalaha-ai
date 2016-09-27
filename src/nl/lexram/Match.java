package nl.lexram;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by dennis on 26-9-16.
 */
public class Match {
    State state;
    Player[] players;
    Rules rules;
    private int turn = 0;
    private int player = 0;

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
                new Player.Default("a"),
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

    public Player getPlayer() {
        return getPlayer(this.player);
    }

    public Player getPlayer(int index) {
        return this.players[index];
    }

    public int getTurn() {
        return turn;
    }

    public int players() {
        return this.players.length;
    }

    public int[] getBoard() {
        return Arrays.copyOf(this.state.board, this.state.board.length);
    }

    public State.Move doNextMove() {
        final State preState = new State(this.state);
        int moveIndex = getPlayer().getMove(player, this);
        final State interState = new State(this.state);
        State.Move move = this.state.new Move(moveIndex);
        System.out.println("Turn " + turn + ": Move " + move.moveIndex);
        move.doMove(player, rules);
        final State postState = new State(this.state);
        System.out.println("Turn " + turn + ": pre   " + preState.toSimpleString());
        System.out.println("Turn " + turn + ": inter " + interState.toSimpleString());
        System.out.println("Turn " + turn + ": post  " + postState.toSimpleString());
        turn++;
        player = (player + 1) % players.length;
        return move;
    }

    public Iterator<State.Move> avaliableMoves() {
        return new AvaliableMoveIterator();
    }

    public State getState() {
        return new State(state);
    }

    private class AvaliableMoveIterator implements Iterator<State.Move> {
        int i;
        State.Move next;
        final int iEnd;

        public AvaliableMoveIterator() {
            i = rules.fieldsPerPlayer * player - 1;
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
            if (next == null) {
                throw new NoSuchElementException();
            }
            this.next = findNext();
            return next;
        }
    }
}
