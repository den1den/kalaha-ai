package antijoy;

import java.util.Objects;

public class Piece {
    int x, y;
    PieceType type;
    boolean moved = false;

    public Piece(int x, int y, PieceType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public Piece(Piece piece) {
        this.x = piece.x;
        this.y = piece.y;
        this.type = piece.type;
        this.moved = piece.moved;
    }

    public static Piece[][] deepCopyPiece(Piece[][] fieldss) {
        Piece[][] arr = new Piece[fieldss.length][];
        for (int i = 0; i < fieldss.length; i++) {
            arr[i] = new Piece[fieldss[i].length];
            for (int j = 0; j < arr[i].length; j++) {
                arr[i][j] = new Piece(fieldss[i][j]);
            }
        }
        return arr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Piece piece = (Piece) o;
        return x == piece.x &&
                y == piece.y &&
                moved == piece.moved &&
                type == piece.type;
    }

    @Override
    public int hashCode() {

        return Objects.hash(x, y, type, moved);
    }
}
