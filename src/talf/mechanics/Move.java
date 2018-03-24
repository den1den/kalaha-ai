package talf.mechanics;

import com.sun.istack.internal.NotNull;
import javafx.util.Pair;

import java.util.Iterator;

public class Move {
    private Coordinate source, target;

    public Move(@NotNull Coordinate source, @NotNull Coordinate target) {
        this.source = source;
        this.target = target;
    }

    public static Iterable<Move> asMove(Iterable<Pair<Coordinate, Coordinate>> pairs) {
        return () -> {
            Iterator<Pair<Coordinate, Coordinate>> iterator = pairs.iterator();
            return new Iterator<Move>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Move next() {
                    Pair<Coordinate, Coordinate> next = iterator.next();
                    return new Move(next.getKey(), next.getValue());
                }
            };
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Move move = (Move) o;

        return source.equals(move.source) && target.equals(move.target);
    }

    @Override
    public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + target.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Move{" + source + " -> " + target + '}';
    }

    public Coordinate getSource() {
        return source;
    }

    public Coordinate getTarget() {
        return target;
    }
}
