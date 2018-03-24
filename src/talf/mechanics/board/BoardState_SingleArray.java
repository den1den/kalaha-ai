package talf.mechanics.board;

public class BoardState_SingleArray {
//    final private int maxWidth;
//    final private short[] fields;
//    final private static short EMPTY = 0;
//    final private static short ATTACK = 1;
//    final private static short DEFEND = 2;
//    final private static short KING = 3;
//
//    public BoardState_SingleArray(int maxWidth, short[] fields) {
//        this.maxWidth = maxWidth;
//        this.fields = fields;
//    }
//
//    int toI(short x, short y){
//        return x * maxWidth + y;
//    }
//
//    short toX(int i){
//        return (short) (i / maxWidth);
//    }
//
//    short toY(int i){
//        return (short) (i % maxWidth);
//    }
//
//    public BoardState_SingleArray(short[][] fields) {
//        this.maxWidth = (short) fields.length;
//        this.fields = new short[this.maxWidth * this.maxWidth];
//        for (int i = 0; i < fields.length; i++) {
//            System.arraycopy(fields[i], 0, this.fields, i * maxWidth, maxWidth);
//        }
//    }
//
//    public static BoardState breakthru() {
//        short[][] fields = new short[11][];
//        for (int i = 0; i < fields.length; i++) {
//            fields[i] = new short[11];
//        }
//
//        for (int i = 3; i <= 7; i++) {
//            fields[1][i] = ATTACK;
//            fields[9][i] = ATTACK;
//            fields[i][1] = ATTACK;
//            fields[i][9] = ATTACK;
//        }
//        for (int i = 4; i <= 6; i++) {
//            fields[3][i] = DEFEND;
//            fields[7][i] = DEFEND;
//            fields[i][3] = DEFEND;
//            fields[i][7] = DEFEND;
//        }
//        fields[5][5] = KING;
//        return new BoardState(fields);
//    }
//
//    public ShortBuffer findMoves(int i) {
//        ShortBuffer buffer = ShortBuffer.allocate(2*maxFields());
//        findMoves(toX(i), toY(i), buffer);
//        return buffer;
//    }
//
//    private int maxFields() {
//        return getWidth() + getHeight();
//    }
//
//    public void findMoves(int i, ShortBuffer buffer) {
//        findMoveMoves(i, buffer);
//        findAttackMoves(i, buffer);
//    }
//
//    public void findMoveMoves(int i, ShortBuffer buffer) {
//        short piece = fields[i];
//        assert piece != EMPTY;
//        int x = toX(i);
//        int y = toY(i);
//        // right
//        for (short i = (short) (x + 1); i < fields.length; i++) {
//            if (fields[i * maxWidth + y] == EMPTY) {
//                buffer.put(i);
//                buffer.put(y);
//            } else {
//                break;
//            }
//            if (piece == KING) break;
//        }
//        // down
//        for (short i = (short) (y + 1); i < fields.length; i++) {
//            if (fields[x * maxWidth + i] == EMPTY) {
//                buffer.put(x);
//                buffer.put(i);
//            } else {
//                break;
//            }
//            if (piece == KING) break;
//        }
//        // left
//        for (short i = (short) (x - 1); i >= 0; i--) {
//            if (fields[i * maxWidth + y] == EMPTY) {
//                buffer.put(i);
//                buffer.put(y);
//            } else {
//                break;
//            }
//            if (piece == KING) break;
//        }
//        // up
//        for (short i = (short) (y - 1); i >= 0; i--) {
//            if (fields[x * maxWidth + i] == EMPTY) {
//                buffer.put(x);
//                buffer.put(i);
//            } else {
//                break;
//            }
//            if (piece == KING) break;
//        }
//    }
//
//    public void findAttackMoves(short x, short y, ShortBuffer buffer) {
//        short piece = fields[x * maxWidth + y];
//        assert piece != EMPTY;
//        // hits
//        for (short i = (short) (x - 1); i >= 0 && i < fields.length && i <= x + 1; i += 2) {
//            for (short j = (short) (y - 1); j >= 0 && j < fields.length && j <= y + 1; j += 2) {
//                if (fields[i * maxWidth + j] == EMPTY) continue;
//                if (piece == ATTACK) {
//                    if (fields[i * maxWidth + j] != ATTACK){
//                        buffer.put(i);
//                        buffer.put(j);
//                    }
//                } else {
//                    if (fields[i * maxWidth + j] == ATTACK){
//                        buffer.put(i);
//                        buffer.put(j);
//                    }
//                }
//            }
//        }
//    }
//
//    @Override
//    public String toString() {
//        String s = "";
//        for (int i = 0; i < fields.length; i++) {
//            s += fields[i];
//            if((i+1) % maxWidth == 0){
//                s += '\n';
//            }
//        }
//        return s;
//    }
//
//    public static void main(String[] args) {
//        BoardState test = breakthru();
//        System.out.println("test = " + test);
//    }
//
//    public boolean isInCenter(short x, short y) {
//        return x >= 3 && x < fields.length - 3
//            && y >= 3 && y < fields.length - 3;
//    }
//
//    public boolean canMove(short sourceX, short sourceY, short targetX, short targetY) {
//        short piece = fields[sourceX*maxWidth+sourceY];
//        assert piece != EMPTY;
//        assert !(sourceX == targetX && sourceY == targetY);
//        if (sourceX != targetX && sourceY == targetY) {
//            // vertical move
//            int i = sourceX;
//            int fi = targetX;
//            int di = fi - i;
//            if (piece == KING && Math.abs(di) > 1) return false;
//            di = di > 0 ? 1 : -1;
//            do {
//                i += di;
//                if (fields[i*maxWidth+sourceY] != EMPTY) {
//                    return false;
//                }
//            } while (i != fi);
//            return true;
//        }
//        if (sourceX == targetX) {
//            // horizontal move
//            int i = sourceY;
//            int fi = targetY;
//            int di = fi - i;
//            if (piece == KING && Math.abs(di) > 1) return false;
//            di = di > 0 ? 1 : -1;
//            do {
//                i += di;
//                if (fields[sourceX*maxWidth+i] != EMPTY) {
//                    return false;
//                }
//            } while (i != fi);
//            return true;
//        }
//        // diagonal move
//        int dx = targetX - sourceX;
//        int dy = targetY - sourceY;
//        if (Math.abs(dx) > 1 || Math.abs(dy) > 1) {
//            return false;
//        }
//        if (fields[targetX*maxWidth+targetY] == EMPTY) {
//            return false;
//        }
//        if (piece == ATTACK) {
//            return fields[targetX*maxWidth+targetY] != ATTACK;
//        } else {
//            // source is defending piece
//            return fields[targetX*maxWidth+targetY] == ATTACK;
//        }
//    }
//
//    public int move(short sourceX, short sourceY, short targetX, short targetY) {
//        assert canMove(sourceX, sourceY, targetX, targetY);
//        short sourcePiece = fields[sourceX*maxWidth+sourceY];
//        short targetPiece = fields[targetX*maxWidth+targetY];
//        fields[sourceX*maxWidth+sourceY] = EMPTY;
//        fields[targetX*maxWidth+targetY] = sourcePiece;
//
//        if (sourcePiece == KING) {
//            if (targetX == 0 || targetX == getWidth() - 1
//                || targetY == 0 || targetY == getHeight() - 1) {
//                return Integer.MAX_VALUE;
//            }
//        }
//        if (targetPiece == KING) {
//            return Integer.MAX_VALUE;
//        }
//        if (targetPiece != EMPTY) {
//            return 1;
//        }
//        return 0;
//    }
//
//    public int getWidth() {
//        return fields.length;
//    }
//
//    public int getHeight() {
//        return fields.length;
//    }
//
//    public short getPiece(short i) {
//        return fields[i];
//    }
//    public short getPiece(short x, short y) {
//        return fields[x*maxWidth+y];
//    }
//
//    public boolean isKing(short x, short y) {
//        return getPiece(x, y) == KING;
//    }
//
//    public boolean isSilverPiece(short x, short y) {
//        return getPiece(x, y) == ATTACK;
//    }
//
//    public Iterator<Short> silverPieces() {
//        return new PiecesIterator(DEFEND);
//    }
//
//    public Iterator<Short> goldSmallPieces() {
//        return new PiecesIterator(ATTACK);
//    }
//
//    public short king() {
//        for (short i = 0; i < fields.length; i++) {
//            if(fields[i] == KING){
//                return i;
//            }
//        }
//        return -1;
//    }
//
//    public boolean isGoldPiece(short piece) {
//        short p = getPiece(piece);
//        return p == DEFEND || p == KING;
//    }
//
//    public boolean isSmallGoldPiece(short piece) {
//        return getPiece(piece) == DEFEND;
//    }
//
//    public void reset() {
//        BoardState b = breakthru();
//        System.arraycopy(b.fields, 0, fields, 0, b.fields.length);
//    }
//
//    public ShortBuffer findMoveMoves(Short i) {
//        return findMoveMoves(toX(i), toY(i), );
//    }
//
//    class PiecesIterator implements Iterator<Short> {
//        private final int piece;
//        private short i = -1, next;
//
//        public PiecesIterator(int piece) {
//            this.piece = piece;
//            findNext();
//        }
//
//        private void findNext() {
//            while (++i < fields.length){
//                if(fields[i] == piece){
//                    next = i;
//                    return;
//                }
//            }
//            next = -1;
//        }
//
//        @Override
//        public boolean hasNext() {
//            return next != -1;
//        }
//
//        @Override
//        public Short next() {
//            short next = this.next;
//            findNext();
//            return next;
//        }
//    }
}
