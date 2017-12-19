package marblegame.players;

public class SimplePlayer implements Player {
    int move;

    public void setMove(int move) {
        this.move = move;
    }

    @Override
    public int getMove() {
        return move;
    }
}
