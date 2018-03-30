package marblegame.gamemechanics;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PossibleMoveIterator implements Iterator<Integer> {
    private final int[] fields;

    private boolean nextSet = false;
    private int current;
    private int max;

    PossibleMoveIterator(final int[] fields, int current, int max) {
        this.fields = fields;
        this.current = current;
        this.max = max;
        if (current < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (max >= fields.length) {
            throw new IndexOutOfBoundsException();
        }
    }

    public static PossibleMoveIterator from(Match match, int player) {
        int min = match.startFields[player];
        int max = match.endFields[player];
        return new PossibleMoveIterator(match.getBoardState().fields, min, max);
    }

    public static PossibleMoveIterator from(@NotNull Match match) {
        return from(match, match.getBoardState());
    }

    public static PossibleMoveIterator from(@NotNull Match match, @NotNull BoardState boardState) {
        int min = match.startFields[boardState.turn];
        int max = match.endFields[boardState.turn];
        return new PossibleMoveIterator(boardState.fields, min, max);
    }

    private boolean findNext() {
        while (true) {
            if (current > max) {
                nextSet = false;
                return false;
            }
            int stones = fields[current];
            if (stones != 0) {
                nextSet = true;
                return true;
            }
            current++;
        }
    }

    @Override
    public boolean hasNext() {
        return nextSet || findNext();
    }

    @Override
    public Integer next() {
        if (!this.nextSet && !findNext()) {
            throw new NoSuchElementException();
        } else {
            this.nextSet = false;
            int next = current;
            current++;
            return next;
        }
    }
}
