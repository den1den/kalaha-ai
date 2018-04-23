package antijoy;

import java.util.Objects;

public final class Move {
    final int startx, starty, endx, endy;

    public Move(int startx, int starty, int endx, int endy) {
        this.startx = startx;
        this.starty = starty;
        this.endx = endx;
        this.endy = endy;
        if (startx == endx && starty == endy)
            System.out.println("WARNING: Move of no steps created: " + this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return startx == move.startx &&
                starty == move.starty &&
                endx == move.endx &&
                endy == move.endy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startx, starty, endx, endy);
    }

    @Override
    public String toString() {
        return "Move{" +
                "startx=" + startx +
                ", starty=" + starty +
                ", endx=" + endx +
                ", endy=" + endy +
                '}';
    }
}
