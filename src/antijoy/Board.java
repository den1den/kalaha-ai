package antijoy;

public class Board {
    PieceType[][] pieces;

    PieceType get(int x, int y, Direction d) {
        assert x >= 0 && x < pieces.length;
        assert y >= 0 && y < pieces[x].length;
        switch (d) {
            case Up:
                return y - 1 >= 0 ? pieces[x][y - 1] : null;
            case Down:
                return y + 1 < pieces[x].length ? pieces[x][y + 1] : null;
            case LeftUp:
                if (x % 2 == 0) {
                    // this is up
                    return y - 1 >= 0 && x - 1 >= 0 ? pieces[x - 1][y - 1] : null;
                } else {
                    // this is down
                    return x - 1 >= 0 ? pieces[x - 1][y] : null;
                }
            case RightUp:
                if (x % 2 == 0) {
                    return y - 1 >= 0 && x + 1 < pieces.length ? pieces[x + 1][y - 1] : null;
                } else {
                    return x + 1 < pieces.length ? pieces[x + 1][y] : null;
                }
            case LeftDown:
                if (x % 2 == 0) {
                    return x - 1 >= 0 ? pieces[x - 1][y] : null;
                } else {
                    return x - 1 >= 0 && y + 1 < pieces[x - 1].length ? pieces[x - 1][y + 1] : null;
                }
            case RightDown:
                if (x % 2 == 0) {
                    return x + 1 < pieces.length ? pieces[x + 1][y] : null;
                } else {
                    return x + 1 < pieces.length && y + 1 < pieces[x + 1].length
                        ? pieces[x + 1][y + 1] : null;
                }
        }
        throw new Error();
    }

    public enum PieceType {
        P1, P2, P3, P4,
        HOUSE, TOWER1, TOWER2, BASE, TREE,
        EMPTY;

        public int getPrice() {
            switch (this) {
                case P1:
                    return 10;
                case P2:
                    return 20;
                case P3:
                    return 30;
                case P4:
                    return 40;
                case TOWER1:
                    return 15;
                case TOWER2:
                    return 35;
                default:
                    return -1;
            }
        }
    }

    public enum Direction {
        RightDown, Down, LeftDown, LeftUp, Up, RightUp
    }
}
