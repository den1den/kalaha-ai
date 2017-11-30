package marblegame.players;

import marblegame.Match;

/**
 * Created by dennis on 17-11-17.
 */
public class NaivePlayer extends AutomaticPlayer {
    public NaivePlayer(String name, Match match) {
        super(name, match);
    }

    @Override
    protected int calcMove() {
        return match.getPossibleMoves().next();
    }
}
