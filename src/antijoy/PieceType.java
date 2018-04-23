package antijoy;

public enum PieceType {
    P0, P1, P2, P3,
    HOUSE, BASE, TOWER1, TOWER2, TREE;

    public int getPrice() {
        switch (this) {
            case P0:
                return 10;
            case P1:
                return 20;
            case P2:
                return 30;
            case P3:
                return 40;
            case TOWER1:
                return 15;
            case TOWER2:
                return 35;
            default:
                return -1;
        }
    }

    public boolean canHit11(PieceType type) {
        assert canMove();
        switch (type) {
            case TREE:
            case HOUSE:
                return true;
            case BASE:
                return ordinal() >= 1;
            case TOWER1:
                return ordinal() >= 3;
            case TOWER2:
                return ordinal() >= 4;
            case P3:
                return this == P3;
            default:
                assert type.canMove();
                return type.ordinal() < ordinal();
        }
    }

    boolean canMove() {
        switch (this) {
            case P0:
            case P1:
            case P2:
            case P3:
                return true;
            default:
                return false;
        }
    }

    boolean isBuilding() {
        switch (this) {
            case TOWER2:
            case TOWER1:
            case HOUSE:
            case BASE:
                return true;
            default:
                return false;
        }
    }

    public boolean hasInfluence() {
        switch (this) {
            case BASE:
            case TOWER1:
            case TOWER2:
            case P3:
            case P2:
            case P1:
            case P0:
                return true;
            default:
                return false;
        }
    }

    public int pointsNeededToHit() {
        switch (this) {
            case BASE:
            case P0:
                return 1; // P1+
            case P1:
            case TOWER1:
                return 2;
            case TOWER2:
            case P2:
            case P3:
                return 3;
            default:
                return 0;
        }
    }

    public int getLevel() {
        int points = ordinal();
        assert points <= 3;
        return points;
    }

    public boolean isBlocking() {
        switch (this) {
            case TREE:
                return false;
            default:
                return true;
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case TREE:
                return "TR";
            case TOWER1:
                return "t_";
            case TOWER2:
                return "T_";
            case BASE:
                return "B_";
            case HOUSE:
                return "H_";
            default:
                return super.toString();
        }
    }
}
