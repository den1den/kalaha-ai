package antijoy;

public enum PieceType {
    P1, P2, P3, P4,
    HOUSE, TOWER1, TOWER2, BASE, TREE,
    EMPTY;
    public int getPrice() {
        switch (this){
            case P1: return 10;
            case P2: return 20;
            case P3: return 30;
            case P4: return 40;
            case TOWER1: return 15;
            case TOWER2: return 35;
            default: return -1;
        }
    }
}
