package talf.mechanics;

import java.util.Objects;

public final class Coordinate implements Comparable<Coordinate> {

    public final int x;
    public final int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate coordinate = (Coordinate) o;
        return x == coordinate.x &&
            y == coordinate.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public int compareTo(Coordinate o) {
        int cmp = Integer.compare(y, o.y);
        if (cmp == 0) {
            cmp = Integer.compare(x, o.x);
        }
        return cmp;
    }
}
