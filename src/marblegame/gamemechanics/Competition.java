package marblegame.gamemechanics;

/**
 * Binds the players to a match.
 */
public class Competition {
    protected final Match match;
    protected int moves = 0;
    protected int lastMove = -1;

    public Competition(Match match) {
        this.match = match;
    }

    public Match getMatch() {
        return match;
    }

    /**
     * @return total number of moves done on this match
     */
    public int getMoves() {
        return moves;
    }

    public int getLastMove() {
        return lastMove;
    }

    /**
     * Do an moveNow on this match
     *
     * @param move the index of the moveNow to do
     * @return the gain of the moveNow
     */
    public int move(int move) {
        int gain = match.move(move);
        this.lastMove = move;
        this.moves++;
        return gain;
    }

    public int getTurn() {
        return match.boardState.turn;
    }

    public boolean canMove(int moveIndex) {
        return match.canMove(moveIndex);
    }

    public int[] getFields() {
        return match.boardState.getFieldsCopy();
    }

    public int[] getPoints() {
        return match.boardState.getPointsCopy();
    }

    public boolean canPlay() {
        return PossibleMoveIterator.from(match).hasNext();
    }
}
