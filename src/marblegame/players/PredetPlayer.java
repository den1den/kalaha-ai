package marblegame.players;

import java.util.NoSuchElementException;

/**
 * Created by dennis on 19-9-17.
 */
public class PredetPlayer extends NamedPlayer {
    int currentMove = 0;
    final int[] moves;

    public PredetPlayer(String name, int[] moves) {
        super(name);
        this.moves = moves;
    }

    @Override
    public int getMove() {
        if (currentMove < moves.length) {
            return moves[currentMove++];
        }
        throw new NoSuchElementException(String.format("Only %d moves specified for this %s", moves.length, this.getClass()));
    }
}
