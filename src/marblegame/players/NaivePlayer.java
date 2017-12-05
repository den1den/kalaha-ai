package marblegame.players;

import marblegame.gamemechanics.Match;
import marblegame.gamemechanics.PossibleMoveIterator;

/**
 * Created by dennis on 17-11-17.
 */
public class NaivePlayer extends AutomaticPlayer {
    public NaivePlayer(String name, Match match) {
        super(name, match);
    }

    @Override
    protected int calcMove() {
        PossibleMoveIterator iterator = PossibleMoveIterator.from(match);
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return -1;
        }
    }
}
