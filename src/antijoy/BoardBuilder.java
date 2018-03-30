package antijoy;

public class BoardBuilder {
    Type[][] boardSpec;

    public BoardBuilder(Type[][] boardSpec) {
        // 1-digit = player+1, 2-digit = type
        this.boardSpec = boardSpec;
    }

    public Board toBoard() {
        int maxX = 0,
            maxY = boardSpec.length / 2 + boardSpec.length % 2;
        for (int y = 0; y < boardSpec.length; y += 2) {
            maxX = Math.max(maxX, boardSpec[y].length * 2 - 1);
            if (y + 1 < boardSpec.length) {
                maxX = Math.max(maxX, 1 + boardSpec[y].length * 2 - 1);
            }
        }
        Type[][] F = new Type[maxY][];
        for (int x = 0; x < F.length; x++) {
            F[x] = new Type[maxX];
        }

        for (int y = 0; y < boardSpec.length; y += 2) {
            Type[] y0s = boardSpec[y];
            Type[] y1s = y + 1 < boardSpec.length ? boardSpec[y + 1] : new Type[0];

            for (int x = 0; x < Math.max(y0s.length, y1s.length); x++) {
                if (x < y0s.length) {
                    // can be placed
                    int x0 = x * 2;
                    F[y / 2][x0] = y0s[x];
                }
                if (x < y1s.length) {
                    int x0 = x * 2 + 1;
                    F[y / 2][x0] = y1s[x];
                }
            }
        }
        maxX = -1;
        maxY = -1;//TODO

        int players = 0;
        int[][] owner = new int[maxX][];
        Board.PieceType[][] fieldss = new Board.PieceType[maxX][];
        for (int x = 0; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                if (y == 0) {
                    owner[x] = new int[maxY];
                    fieldss[x] = new Board.PieceType[maxY];
                }
                Type f = F[x][y];
                owner[x][y] = f.getPlayer();
                fieldss[x][y] = f.getPieceType();
            }
        }
        return new Board(fieldss, owner, players);
    }

    public enum Type {
        P00, P10, P20, P30,
        H_0, B_0, T10, T20, TR0,
        P01, P11, P21, P31,
        H_1, B_1, T11, T21, TR1,
        P02, P12, P22, P32,
        H_2, B_2, T12, T22, TR2,
        P03, P13, P23, P33,
        H_3, B_3, T13, T23, TR3,
        XXX, ___, __0, __1, __2, __3;

        public Board.PieceType getPieceType() {
            Board.PieceType[] pt = Board.PieceType.values();
            if (this.ordinal() < 9 * 4)
                return pt[this.ordinal() % pt.length];
            return null;
        }

        public int getPlayer() {
            if (this.ordinal() < 9 * 4)
                return this.ordinal() / Board.PieceType.values().length;
            return this.ordinal() - 9 * 4 - 2;
        }
    }
}
