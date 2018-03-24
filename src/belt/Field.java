package belt;

import marblegame.Util;

import java.util.Arrays;

/**
 * Assembly line with its fields
 */
public class Field {
    Piece[][] fields;
    boolean[][] availiable;

    public Field(Piece[][] fields, boolean[][] availiable) {
        this.fields = fields;
        this.availiable = availiable;
    }

    public Field(Field field) {
        this(Util.deepCopy(field.fields), Util.deepCopy(field.availiable));
    }

    public static Field emptyField(int width, int height) {
        Piece[][] board = new Piece[width][];
        for (int i = 0; i < board.length; i++) {
            board[i] = new Piece[height];
        }
        return new Field(board, allTrue(width, height));
    }

    public static Field defaultField() {
        Piece[][] board = new Piece[7][];
        for (int i = 0; i < board.length; i++) {
            board[i] = new Piece[9];
        }
        boolean[][] aviliable = new boolean[board.length][];
        for (int x = 0; x < aviliable.length; x++) {
            aviliable[x] = new boolean[board[x].length];
            for (int y = 0; y < aviliable[x].length; y++) {
                aviliable[x][y] = ((x < 6 && y < 3) || y >= 3);
            }
        }
        return new Field(board, aviliable);
    }

    private static boolean[][] allTrue(int width, int height) {
        boolean[][] r = new boolean[width][];
        for (int i = 0; i < r.length; i++) {
            r[i] = new boolean[height];
            Arrays.fill(r, true);
        }
        return r;
    }

    public void set(int x, int y, Piece block) {
        assert availiable[x][y];
        fields[x][y] = block;
    }

    public boolean canPlace(int x, int y) {
        if (x < 0 || y < 0) return false;
        if (x >= availiable.length || y >= availiable[x].length) return false;
        return availiable[x][y] && fields[x][y] == null;
    }


    public void place(int x, int y, Piece piece) {
        assert canPlace(x, y);
        fields[x][y] = piece;
    }

    public int width() {
        return fields.length;
    }

    public int height() {
        return fields[0].length;
    }
}
