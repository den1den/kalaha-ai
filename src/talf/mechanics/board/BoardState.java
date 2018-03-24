package talf.mechanics.board;

import marblegame.Util;
import talf.mechanics.Coordinate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class BoardState {
    final protected static byte EMPTY = 0;
    final protected static byte SILVER = 1;
    final protected static byte GOLD = 2;
    // Fields
    protected final byte[][] fields;
    // Characteristics
    protected final int center;
    protected final int totalSilver;
    protected final int totalGold;
    protected int kingX;
    protected int kingY;

    public BoardState(byte[][] fields, int kingX, int kingY,
                      int center, int totalSilver, int totalGold) {
        this.fields = fields;
        this.kingX = kingX;
        this.kingY = kingY;
        this.center = center;
        this.totalSilver = totalSilver;
        this.totalGold = totalGold;
    }

    public BoardState copy() {
        return new BoardState(Util.deepCopy(fields),
            kingX, kingY,
            center,
            totalGold, totalSilver
        );
    }

    public int getWidth() {
        return fields.length;
    }

    public int getHeight() {
        return fields[0].length;
    }

    public boolean isInCenter(Coordinate c) {
        int x2 = getWidth() / 2;
        int y2 = getHeight() / 2;
        return Math.abs(c.x - x2) < center
            && Math.abs(c.y - y2) < center;
    }


    public int getMaxSilverPieces() {
        return totalSilver;
    }

    /**
     * Including the king
     */
    public int getMaxGoldPieces() {
        return totalGold;
    }

    public ArrayList<Coordinate> create() {
        return new ArrayList<>(2 * (fields.length - 1));
    }

    public ArrayList<Coordinate> findMoves(Coordinate c) {
        ArrayList<Coordinate> moves = create();
        findMoves(moves, c);
        return moves;
    }

    private void findMoves(ArrayList<Coordinate> result, Coordinate c) {
        findMoveMoves(result, c);
        findAttackMoves(result, c);
    }

    public void findMoveMoves(ArrayList<Coordinate> result, Coordinate c) {
        if (c == null || fields == null || fields[c.x] == null) {
            return;
        }
        byte piece = fields[c.x][c.y];
        boolean king = isKing(c);
        assert piece != EMPTY;
        // right
        int x = c.x;
        int y = c.y;
        for (int i = x + 1; i < getWidth(); i++) {
            if (fields[i][y] == EMPTY) {
                result.add(new Coordinate(i, y));
            } else {
                break;
            }
            if (king) break;
        }
        // down
        for (int i = y + 1; i < getHeight(); i++) {
            if (fields[x][i] == EMPTY) {
                result.add(new Coordinate(x, i));
            } else {
                break;
            }
            if (king) break;
        }
        // left
        for (int i = x - 1; i >= 0; i--) {
            if (fields[i][y] == EMPTY) {
                result.add(new Coordinate(i, y));
            } else {
                break;
            }
            if (king) break;
        }
        // up
        for (int i = y - 1; i >= 0; i--) {
            if (fields[x][i] == EMPTY) {
                result.add(new Coordinate(x, i));
            } else {
                break;
            }
            if (king) break;
        }
    }


    public void findAttackMoves(List<Coordinate> result, Coordinate c) {
        byte piece = fields[c.x][c.y];
        assert piece != EMPTY;
        int x = c.x;
        int y = c.y;
        // hits
        for (int i = x - 1; i >= 0 && i < getWidth() && i <= x + 1; i += 2) {
            for (int j = y - 1; j >= 0 && j < getHeight() && j <= y + 1; j += 2) {
                if (fields[i][j] == EMPTY) continue;
                if (piece == SILVER) {
                    if (fields[i][j] != SILVER)
                        result.add(new Coordinate(i, j));
                } else {
                    if (fields[i][j] == SILVER)
                        result.add(new Coordinate(i, j));
                }
            }
        }
    }


    public boolean canMove(Coordinate source, Coordinate target) {
        byte piece = fields[source.x][source.y];
        boolean king = isKing(source);
        assert piece != EMPTY;
        assert !(source.x == target.x && source.y == target.y);
        if (source.x != target.x && source.y == target.y) {
            // vertical move
            int i = source.x;
            int fi = target.x;
            int di = fi - i;
            if (king && Math.abs(di) > 1) return false;
            di = di > 0 ? 1 : -1;
            do {
                i += di;
                if (fields[i][source.y] != EMPTY) {
                    return false;
                }
            } while (i != fi);
            return true;
        }
        if (source.x == target.x) {
            // horizontal move
            int i = source.y;
            int fi = target.y;
            int di = fi - i;
            if (king && Math.abs(di) > 1) return false;
            di = di > 0 ? 1 : -1;
            do {
                i += di;
                if (fields[source.x][i] != EMPTY) {
                    return false;
                }
            } while (i != fi);
            return true;
        }
        // diagonal move
        int dx = target.x - source.x;
        int dy = target.y - source.y;
        if (Math.abs(dx) > 1 || Math.abs(dy) > 1) {
            return false;
        }
        if (fields[target.x][target.y] == EMPTY) {
            return false;
        }
        if (piece == SILVER) {
            return fields[target.x][target.y] != SILVER;
        } else {
            // source is defending piece
            return fields[target.x][target.y] == SILVER;
        }
    }

    public int move(Coordinate source, Coordinate target) {
        assert canMove(source, target);
        boolean king = isKing(source);
        boolean hitKing = isKing(target);

        byte sourcePiece = fields[source.x][source.y];
        byte targetPiece = fields[target.x][target.y];
        fields[source.x][source.y] = EMPTY;
        fields[target.x][target.y] = sourcePiece;

        if (king) {
            kingX = target.x;
            kingY = target.y;
            if (isKingOnEdge())
                return Integer.MAX_VALUE;
        }
        if (hitKing) {
            kingX = -1;
            return Integer.MAX_VALUE;
        }
        if (targetPiece != EMPTY) {
            return 1;
        }
        return 0;
    }

    boolean isKingOnEdge() {
        return kingX == 0 || kingX == fields.length - 1
            || kingY == 0 || kingY == fields[kingX].length - 1;
    }


    private byte getPiece(Coordinate coordinate) {
        return fields[coordinate.x][coordinate.y];
    }


    public boolean isKing(Coordinate coordinate) {
        return coordinate.x == kingX && coordinate.y == kingY;
    }


    public boolean isSilverPiece(Coordinate coordinate) {
        return getPiece(coordinate) == SILVER;
    }


    public Iterable<Coordinate> silverPieces() {
        return () -> new PiecesIterator(SILVER);
    }


    public Iterable<Coordinate> goldSmallPieces() {
        return () -> new PiecesIterator(GOLD);
    }

    public Iterable<Coordinate> goldPieces() {
        return () -> Util.chain(new PiecesIterator(GOLD), Arrays.asList(king()).iterator());
    }


    public Coordinate king() {
        if (hasKing())
            return new Coordinate(kingX, kingY);
        else
            return null;
    }


    public boolean isGoldPiece(Coordinate piece) {
        return getPiece(piece) == GOLD;
    }


    public boolean isSmallGoldPiece(Coordinate piece) {
        return !isKing(piece) && getPiece(piece) == GOLD;
    }


    public boolean isEmpty(Coordinate piece) {
        return getPiece(piece) == EMPTY;
    }


    public int countGoldCovered() {
        return countDiagonals(GOLD, GOLD);
    }


    public int countGoldCanHit() {
        return countDiagonals(GOLD, SILVER);
    }


    public int countSilver() {
        return countPieces(SILVER);
    }


    public int countGold() {
        return countPieces(GOLD);
    }

    private int countPieces(byte target) {
        int c = 0;
        for (byte[] field : fields) {
            for (byte aField : field) {
                if (aField == target) {
                    c++;
                }
            }
        }
        return c;
    }

    private int countDiagonals(byte sourceType, byte targetType) {
        int c = 0;
        for (int x = 0; x < fields.length; x++) {
            for (int y = 0; y < fields[x].length; y++) {
                if (fields[x][y] == sourceType) {
                    if (canHit(targetType, x, y) > 0) {
                        c++;
                    }
                }
            }
        }
        return c;
    }

    private int canHit(byte targetType, int x, int y) {
        int hits = 0;
        if (x - 1 >= 0) {
            if (y - 1 >= 0) {
                if (fields[x - 1][y - 1] == targetType) {
                    hits++;
                }
            }
            if (y + 1 < fields[x].length) {
                if (fields[x - 1][y + 1] == targetType) {
                    hits++;
                }
            }
        }
        if (x + 1 < fields.length) {
            if (y - 1 >= 0) {
                if (fields[x + 1][y - 1] == targetType) {
                    hits++;
                }
            }
            if (y + 1 < fields[x].length) {
                if (fields[x + 1][y + 1] == targetType) {
                    hits++;
                }
            }
        }
        return hits;
    }


    public boolean hasKing() {
        return kingX >= 0;
    }


    public boolean canHitKing() {
        return hasKing() && canHit(SILVER, kingX, kingY) > 0;
    }


    public int maxDistToBorder() {
        return Math.max(getWidth() / 2, getHeight() / 2);
    }


    public int distanceToBorderKing() {
        if (!hasKing()) {
            return -1;
        }
        return Math.min(
            Math.min(kingX, fields.length - kingX),
            Math.min(kingY, fields[kingX].length - kingY)
        );
    }


    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int y = 0; y < fields[0].length; y++) {
            for (int x = 0; x < fields.length; x++) {
                byte p = fields[x][y];
                if (p == 0) {
                    s.append('_');
                } else if (p == GOLD && x == kingX && y == kingY) {
                    s.append('X');
                } else if (p == GOLD) {
                    s.append('+');
                } else {
                    s.append('-');
                }
                s.append(' ');
            }
            s.append("\n");
        }
        return s.toString();
    }

    class FieldsIterator implements Iterator<Coordinate> {
        int x = -1;
        int y = 0;
        Coordinate next = null;

        FieldsIterator() {
            findNext();
        }

        private void findNext() {
            x++;
            for (; y < getHeight(); y++) {
                if (x < getWidth()) {
                    next = new Coordinate(x, y);
                    return;
                }
                x = 0;
            }
            next = null;
        }


        public boolean hasNext() {
            return next != null;
        }


        public Coordinate next() {
            Coordinate next = this.next;
            findNext();
            return next;
        }
    }

    class PiecesIterator extends FieldsIterator {
        private final byte piece;

        PiecesIterator(byte piece) {
            this.piece = piece;
            findNext();
        }

        private void findNext() {
            x++;
            for (; y < getHeight(); y++) {
                for (; x < getWidth(); x++) {
                    if (fields[x][y] == piece) {
                        next = new Coordinate(x, y);
                        return;
                    }
                }
                x = 0;
            }
            next = null;
        }


        public boolean hasNext() {
            return next != null;
        }


        public Coordinate next() {
            Coordinate next = this.next;
            findNext();
            return next;
        }
    }
}
