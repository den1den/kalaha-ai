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
        for (int y = 0; y < boardSpec.length; y++) {
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
                int x0 = x * 2;
                if (x < y0s.length && x0 < F[y / 2].length) {
                    // can be placed
                    F[y / 2][x0] = y0s[x];
                }
                x0 += 1;
                if (x < y1s.length && x0 < F[y / 2].length) {
                    F[y / 2][x0] = y1s[x];
                }
            }
        }

        int players = 0;
        int[][] owner = new int[maxX][];
        PieceType[][] fieldss = new PieceType[maxX][];
        for (int x = 0; x < maxX; x++) {
            owner[x] = new int[maxY];
            fieldss[x] = new PieceType[maxY];
            for (int y = 0; y < maxY; y++) {
                Type f = F[y][x];
                if (f == null) f = Type.XXX;
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
        XXXXX, XXX, _____, ___, _0_, _1_, _2_, _3_;

        private static final Type LAST_NORMAL = TR3;

        public PieceType getPieceType() {
            PieceType[] pt = PieceType.values();
            if (this.ordinal() <= LAST_NORMAL.ordinal())
                return pt[this.ordinal() % pt.length];
            return null;
        }

        public int getPlayer() {
            int lastNormal = LAST_NORMAL.ordinal();
            if (this.ordinal() <= lastNormal)
                return this.ordinal() / PieceType.values().length;
            if (this == XXXXX || this == XXX) return -2;
            if (this == _____ || this == ___) return -1;
            return this.ordinal() - _0_.ordinal();
        }
    }
}
