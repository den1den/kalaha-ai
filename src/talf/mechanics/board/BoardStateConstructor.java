package talf.mechanics.board;

import talf.mechanics.Coordinate;

import static talf.mechanics.board.BoardModel.GOLD;
import static talf.mechanics.board.BoardModel.SILVER;

public class BoardStateConstructor {

    private byte[][] fields;
    private int center = -1;
    private int kingX = -1;
    private int kingY;

    public BoardStateConstructor(int width, int height, int center) {
        this.fields = new byte[width][];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = new byte[height];
        }
        this.center = center;
    }

    public BoardStateConstructor(byte[][] fields, int center) {
        this.fields = fields;
        findKing:
        {
            for (int x = 0; x < fields.length; x++) {
                for (int y = 0; y < fields[x].length; y++) {
                    if (fields[x][y] == GOLD + 1) {
                        fields[x][y] = GOLD;
                        setKing(x, y);
                        break findKing;
                    }
                }
            }
        }
        this.center = center;
    }

    public static BoardModel verySimpleBoardState() {
        return new BoardStateConstructor(5, 2, 2)
            .setKing(0, 0)
            .setLineGold(2)
            .setLineSilver(4)
            .toBoard(true);
    }

    public static BoardModel simpleBoardState() {
        int WIDTH = 8, HEIGHT = 3;
        return new BoardStateConstructor(WIDTH, HEIGHT, WIDTH / 2)
            .setKing(0, HEIGHT / 2)
            .setLineGold(2)
            .setLineSilver(-2)
            .setSilver(-2, 0)
            .setSilver(-2, -1)
            .toBoard(true);
    }

    public static BoardModel breakthru() {
        byte[][] fields = new byte[11][];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = new byte[11];
        }

        for (int i = 3; i <= 7; i++) {
            fields[1][i] = SILVER;
            fields[9][i] = SILVER;
            fields[i][1] = SILVER;
            fields[i][9] = SILVER;
        }
        for (int i = 4; i <= 6; i++) {
            fields[3][i] = GOLD;
            fields[7][i] = GOLD;
            fields[i][3] = GOLD;
            fields[i][7] = GOLD;
        }
        fields[5][5] = GOLD + 1;
        return createOfFields(fields, 3, false);
    }

    private static BoardModel createOfFields(byte[][] fields, int center, boolean simple
    ) {
        return new BoardStateConstructor(fields, center).toBoard(simple);
    }

    public BoardStateConstructor setCenter(int center) {
        this.center = center;
        return this;
    }

    private void setPiece(int x, int y, byte p) {
        if (x < 0) {
            x += fields.length;
        }
        if (y < 0) {
            y += fields[x].length;
        }
        fields[x][y] = p;
    }

    public BoardStateConstructor setSilver(int x, int y) {
        setPiece(x, y, SILVER);
        return this;
    }

    public BoardStateConstructor setKing(int x, int y) {
        setPiece(x, y, GOLD);
        if (x < 0) {
            x += fields.length;
        }
        if (y < 0) {
            y += fields[x].length;
        }
        kingX = x;
        kingY = y;
        return this;
    }

    public BoardStateConstructor setGold(int x, int y) {
        setPiece(x, y, GOLD);
        return this;
    }

    public BoardStateConstructor setLineSilver(int x) {
        return setLine(x, SILVER);
    }

    public BoardStateConstructor setLineGold(int x) {
        return setLine(x, GOLD);
    }

    public BoardStateConstructor setLine(int x, byte color) {
        if (x < 0) {
            x += fields.length;
        }
        for (int i = 0; i < fields[x].length; i++) {
            setPiece(x, i, color);
        }
        return this;
    }

    public BoardModel toBoard() {
        return toBoard(false);
    }

    public BoardModel toBoard(boolean simple) {
        int[] sg = countSilverGold();
        BoardModel bs;
        if (simple) {
            bs = new SimpleBoardModel(fields, kingX, kingY, center, sg[0], sg[1]);
        } else {
            bs = new BoardModel(fields, kingX, kingY, center, sg[0], sg[1]);
        }
        for (Coordinate silver : bs.silverPieces()) {
            assert !bs.isInCenter(silver);
        }
        for (Coordinate gold : bs.goldPieces()) {
            assert bs.isInCenter(gold);
        }
        return bs;
    }

    private int[] countSilverGold() {
        int silver = 0, gold = 0;
        for (int x = 0; x < fields.length; x++) {
            for (int y = 0; y < fields[x].length; y++) {
                if (fields[x][y] == SILVER) silver++;
                if (fields[x][y] == GOLD) gold++;
            }
        }
        return new int[]{silver, gold};
    }
}
