package talf.mechanics.board;

import talf.mechanics.Coordinate;

public class SimpleBoardModel extends BoardModel {

    SimpleBoardModel(byte[][] fields, int kingX, int kingY,
                     int center, int totalSilver, int totalGold) {
        super(fields, kingX, kingY, center, totalGold, totalSilver);
    }

    @Override
    boolean isKingOnEdge() {
        return kingX == fields.length - 1;
    }

    @Override
    public boolean isInCenter(Coordinate c) {
        return c.x <= center;
    }

    @Override
    public int getDistanceToBorderKing() {
        if (!hasKing()) {
            return -1;
        }
        return getWidth() - kingX;
    }

    @Override
    public int maxDistToBorder() {
        return getWidth();
    }

    @Override
    public int getMaxGoldPieces() {
        return totalGold;
    }

    @Override
    public int getMaxSilverPieces() {
        return totalSilver;
    }
}
