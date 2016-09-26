package nl.lexram;

/**
 * Created by dennis on 26-9-16.
 */
public class Rules {
    final public int targetAmount;
    final public int target2Amount;
    final public int players;
    final public int initStonesPerFields;
    final public int fieldsPerPlayer;
    final public int targetPoints;
    final public int target2Points;

    public Rules() {
        this.targetAmount = 3;
        this.targetPoints = targetAmount;
        this.target2Amount = 2;
        this.target2Points = target2Amount;
        this.players = 2;
        this.initStonesPerFields = 4;
        this.fieldsPerPlayer = 6;
    }

    public int stones() {
        return players * initStonesPerFields * fieldsPerPlayer;
    }

    public int fields() {
        return players * fieldsPerPlayer;
    }

    public int getMovesStartIndex(int player) {
        return player * fieldsPerPlayer;
    }

    public int getMovesLength() {
        return fieldsPerPlayer;
    }
}
