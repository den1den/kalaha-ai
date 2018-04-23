package antijoy;

public class BoardPrinter {
    public static final String RESET_COLOR = "\u001B[0m";
    private static final String[] COLORS = new String[]{
            "\u001B[32m",
            "\u001B[37m",
            "\u001B[31m",
            "\u001B[35m",
            "\u001B[33m",
            "\u001B[34m",
            "\u001B[36m"
    };
    private final Board board;

    public BoardPrinter(Board board) {
        this.board = board;
    }

    public static String getColor(int player) {
        return COLORS[(player + 2) % COLORS.length];
    }

    @Override
    public String toString() {

        // 0 2 4
        //  1 3 5
        // 0 2 4
        //  1 3

        //  /---\    /---\
        // | 0_0 |--| 0_2 |
        //  \---/    \   /
        //     |  0_1 |-|
        //
        //  x 0 1 2 3 4
        //y0 AAA CCC EEE
        //     BBB DDD FFF
        //y1 000 222 444
        //     111 333
        //

        // 0 2 4
        //  1 3 5
        // 0 2 4
        //  1 3

        //  /---\    /---\
        // | 0_0 |--| 0_2 |
        //  \---/    \   /
        //     |  0_1 |-|
        //
        //  x 0 1 2 3 4
        //y0 AAA CCC EEE
        //     BBB DDD FFF
        //y1 000 222 444
        //     111 333
        //
        StringBuilder s = new StringBuilder();
        int maxY = 0, maxX = board.owner.length;
        for (int x = 0; x < board.fieldss.length; x++) {
            maxY = Math.max(maxY, board.fieldss[x].length);
        }
        for (int y = 0; y < maxY; y++) {
//            for (int x = 0; x < maxX; x+=2) {
//                s.append(" / - \\ ");
//            }
            for (int x = 0; x < maxX; x += 2) {
                if (x >= board.owner.length || y >= board.owner[x].length) continue;
                toStringField(s, x, y);
            }
            s.append(System.lineSeparator());
            s.append("  ");
            for (int x = 1; x < maxX; x += 2) {
                if (x >= board.owner.length || y >= board.owner[x].length) continue;
                toStringField(s, x, y);
            }
            s.append(System.lineSeparator());
        }
        return s.toString();
    }

    private void toStringFieldXY(StringBuilder s, int x, int y) {
        s.append(String.format("%2d%2d ", x, y));
    }

    private void toStringField(StringBuilder s, int x, int y) {
        PieceType pt = board.getPieceType(x, y);
        int player = board.owner[x][y];
        if (player == -2) {
            s.append("   ");
        } else {
            s.append(getColor(player));
            if (pt != null)
                s.append(pt.toString()).append("_").append(player);
            else
                s.append(String.format(" o%2d", player));
            s.append(RESET_COLOR);
        }
        s.append(" ");
    }

    public String toStringXY() {
        StringBuilder s = new StringBuilder();
        int maxY = 0, maxX = board.owner.length;
        for (int x = 0; x < board.fieldss.length; x++) {
            maxY = Math.max(maxY, board.fieldss[x].length);
        }
        for (int y = 0; y < maxY; y++) {
            for (int x = 0; x < maxX; x += 2) {
                if (x >= board.owner.length || y >= board.owner[x].length) continue;
                s.append('(');
                toStringField(s, x, y);
                s.append(")");
            }
            s.append(System.lineSeparator());

            for (int x = 0; x < maxX; x += 2) {
                if (x >= board.owner.length || y >= board.owner[x].length) continue;
                s.append("(");
                toStringFieldXY(s, x, y);
                s.append(")");
            }
            s.append(System.lineSeparator());

            s.append("  ");
            for (int x = 1; x < maxX; x += 2) {
                if (x >= board.owner.length || y >= board.owner[x].length) continue;
                s.append("(");
                toStringField(s, x, y);
                s.append(")");
            }
            s.append(System.lineSeparator());

            s.append("  ");
            for (int x = 1; x < maxX; x += 2) {
                if (x >= board.owner.length || y >= board.owner[x].length) continue;
                s.append("(");
                toStringFieldXY(s, x, y);
                s.append(")");
            }
            s.append(System.lineSeparator());
        }
        return s.toString();
    }
}
