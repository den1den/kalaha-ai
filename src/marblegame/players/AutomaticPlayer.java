package marblegame.players;

import marblegame.gamemechanics.Match;

public abstract class AutomaticPlayer extends NamedPlayer {
    protected final Match match;
    protected boolean running = false;

    public AutomaticPlayer(String name, Match match) {
        super(name);
        this.match = match;
    }

    protected abstract int calcMove();

    @Override
    public int getMove() {
        return calcMove();
    }
}
