package marblegame.players;

/**
 * Created by dennis on 3-10-17.
 */
public class SimplePlayer extends NamedPlayer {
    int move;

    public SimplePlayer(String name) {
        super(name);
    }

    public void setMove(int move) {
        this.move = move;
    }

    @Override
    public int getMove() {
        return move;
    }
}
