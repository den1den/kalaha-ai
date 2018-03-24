package jfx3.opponent;

public class Result {
    public final int move;

    public Result(int move) {
        this.move = move;
    }

    public boolean isGiveUp() {
        return move == -1;
    }
}
