package antijoy;

import marblegame.Util;

import java.util.*;

public class Board {

    private static final int NOT_POSSIBLE = PieceType.values().length;
    Piece[][] fieldss; // fieldss[x][y] == null => empty
    int[][] owner; // owner[x][y] == -1 => gia, -2 => disabled
    int players;

    int[] money;

    List<Set<Piece>> pieces;

    public Board(Piece[][] fieldss, int[][] owner, int[] money, int players) {
        this.fieldss = fieldss;
        this.owner = owner;
        this.money = money;
        this.players = players;
        pieces = new ArrayList<>(players);
    }

    public Board(Board b) {
        this.fieldss = Piece.deepCopyPiece(b.fieldss);
        this.owner = Util.deepCopy(b.owner);
        this.money = Arrays.copyOf(b.money, b.money.length);
        this.players = b.players;
        pieces = new ArrayList<>(players);
        for (int i = 0; i < players; i++) {
            pieces.add(new HashSet<>(b.pieces.get(i)));
        }
    }

    int[] get(int x, int y, BoardDirection d) {
        assert x >= 0 && x < fieldss.length;
        assert y >= 0 && y < fieldss[x].length;
        switch (d) {
//            case Up:
//                return y - 1 >= 0 ? new int[]{x, y - 1} : null;
//            case Down:
//                return y + 1 < fieldss[x].length ? new int[]{x, y + 1} : null;
            case LeftUp:
                if (x % 2 == 0) {
                    // this is up
                    return y - 1 >= 0 && x - 1 >= 0 ? new int[]{x - 1, y - 1} : null;
                } else {
                    // this is down
                    return x - 1 >= 0 ? new int[]{x - 1, y} : null;
                }
            case RightUp:
                if (x % 2 == 0) {
                    return y - 1 >= 0 && x + 1 < fieldss.length ? new int[]{x + 1, y - 1} : null;
                } else {
                    return x + 1 < fieldss.length ? new int[]{x + 1, y} : null;
                }
            case LeftDown:
                if (x % 2 == 0) {
                    return x - 1 >= 0 ? new int[]{x - 1, y} : null;
                } else {
                    return x - 1 >= 0 && y + 1 < fieldss[x - 1].length ? new int[]{x - 1, y + 1} : null;
                }
            case RightDown:
                if (x % 2 == 0) {
                    return x + 1 < fieldss.length ? new int[]{x + 1, y} : null;
                } else {
                    return x + 1 < fieldss.length && y + 1 < fieldss[x + 1].length
                            ? new int[]{x + 1, y + 1} : null;
                }
                //   0   0
                // 0   0   0
                //   0   0
            case Left:
                return x - 2 >= 0 ? new int[]{x - 2, y} : null;
            case Right:
                return x + 2 <= fieldss.length ? new int[]{x + 2, y} : null;
        }
        throw new Error();
    }

    PieceType getPieceType(int x, int y, BoardDirection d) {
        int[] loc = get(x, y, d);
        if (loc == null) return null;
        return getPieceType(loc[0], loc[1]);
    }

    PieceType getPieceType(int x, int y) {
        return fieldss[x][y].type;
    }

    int owner(int x, int y) {
        return owner[x][y];
    }

    List<Move> getMoves(int player) {
        List<Move> moves = new ArrayList<>();
        int[][] moveablePieces = getMoveablePieces(player);
        MoveScheme ms = new MoveScheme(player);
        for (int i = 0; i < moveablePieces.length; i++) {
            int px = moveablePieces[i][0], py = moveablePieces[i][1];
            int pieceLevel = getPieceType(px, py).getLevel();
            List<int[]> possibleLocations = ms.getPossibleLocations(pieceLevel);
            for (int[] loc :
                    possibleLocations) {
                moves.add(new Move(px, py, loc[0], loc[1]));
            }
        }
        return moves;
    }

    @Override
    public String toString() {
        return new BoardPrinter(this).toString();
    }

    public void set(int x, int y, int owner, PieceType pieceType) {
        this.fieldss[x][y] = new Piece(x, y, pieceType);
        this.owner[x][y] = owner;
    }

    public void move(int player, int x, int y, int toX, int toY) {
        assert canMove(player, x, y, toX, toY);
        throw new UnsupportedOperationException();
    }

    private boolean canMove(int player, int x, int y, int toX, int toY) {
        PieceType p = this.fieldss[x][y].type;
        if (this.owner[x][y] != player) return false;
        if (!p.canMove()) return false;

        if (this.owner[toX][toY] != player) {
            int[][] defend = getMoveablePieces(player);
            if (defend[toX][toY] < p.getLevel()) return false;
        }
        throw new UnsupportedOperationException();
    }

    public List<int[]> getMoves(final int x, final int y) {
        // 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
        //  0 0 0 - - - - - - 0 0 0 0 0 0 0 0 0
        // 0 0 0 | 0 0 0 0 0 | 0 0 0 0 0 0 0 0
        //  0 0 | 0 0 0 0 0 0 | 0 0 0 0 0 0 0 0
        // 0 0 | 0 0 0 0 0 0 0 | 0 0 0 0 0 0 0
        //  0 | X X 0 0 0 0 0 0 | 0 0 0 0 0 0 0
        // 0 | X 0 0 X - 0 0 0 0 | 0 0 0 0 0 0
        //  0 | X X 0 0 0 0 0 0 | 0 0 0 0 0 0 0
        // 0 0 | 0 0 0 0 0 0 0 | 0 0 0 0 0 0 0
        List<int[]> moves = new ArrayList<>();

        int owner = owner(x, y);
        int attack = getPieceType(x, y).getLevel();
        MoveScheme defend = new MoveScheme(owner);
        int[][] walkDistance = Util.deepCopy(this.owner, -1);

        ArrayList<int[]> frontier = new ArrayList<>();
        frontier.add(new int[]{x, y});
        walkDistance[x][y] = 0;

        for (int step = 0; step < 5; step++) {
            ArrayList<int[]> newFrontier = new ArrayList<>();
            int frontierX, frontierY;
            for (int f = 0; f < frontier.size(); f++) {
                frontierX = frontier.get(f)[0];
                frontierY = frontier.get(f)[1];
                for (BoardDirection direction :
                        BoardDirection.values()) {
                    int[] newLoc = get(frontierX, frontierY, direction);
                    if (newLoc == null) continue;
                    int newX = newLoc[0];
                    int newY = newLoc[1];
                    if (defend.obstacles[newX][newY] > attack) continue;
                    // Dont barge into enemy territoy
                    if (walkDistance[newX][newY] != 0 && walkDistance[newX][newY] < step) {
                        newFrontier.add(new int[]{newX, newY});
                    }
                }
            }
            moves.addAll(newFrontier);
            frontier = newFrontier;
        }

        // remove duplicates
        HashSet<Move> movesSet = new HashSet<>();
        for (int i = 0; i < moves.size(); i++) {
            int[] m = moves.get(i);
            movesSet.add(new Move(x, y, m[0], m[1]));
        }
        moves.clear();
        for (Move m :
                movesSet) {
            moves.add(new int[]{m.startx, m.starty, m.endx, m.endy});
        }
        return moves;
    }

    int[][] getMoveablePieces(int player) {
        ArrayList<int[]> locations = new ArrayList<>();
        for (int x = 0; x < owner.length; x++) {
            for (int y = 0; y < owner[x].length; y++) {
                if (owner[x][y] == player) {
                    PieceType piece = fieldss[x][y].type;
                    if (piece != null && piece.canMove()) {
                        locations.add(new int[]{x, y});
                    }
                }
            }
        }
        return Util.listToArray(locations);
    }

    public void move(int player, Move move) {
        assert getMoves(player).contains(move);
        Piece piece = fieldss[move.startx][move.starty];
        Piece targetPiece = fieldss[move.endx][move.endy];

        fieldss[move.startx][move.starty] = null;
        fieldss[move.endx][move.endy] = piece;
        piece.x = move.endx;
        piece.y = move.endy;
        owner[move.endy][move.endy] = player;

        targetPiece.x = -1;
        targetPiece.y = -1;

        if (owner(move.endx, move.endy) == player) {
            if (targetPiece.type == PieceType.TREE) {
                money[player] += Settings.TREE_GAIN;
            }
        }

    }

    private class MoveScheme {
        // -1: not possible, otherwise minimal level needed
        int[][] obstacles = new int[fieldss.length][];

        {
            for (int x = 0; x < fieldss.length; x++) {
                obstacles[x] = new int[fieldss[x].length];
            }
        }

        public MoveScheme(int player) {
            for (int x = 0; x < fieldss.length; x++) {
                for (int y = 0; y < fieldss[x].length; y++) {
                    int partialValue;
                    int owner = owner(x, y);
                    PieceType piece = getPieceType(x, y);

                    if (owner == -2) {
                        // no field
                        obstacles[x][y] = NOT_POSSIBLE;
                        continue;
                    } else if (owner == player) {
                        // your own fieldss
                        if (piece == null) partialValue = 0;
                        else partialValue = piece.isBlocking() ? NOT_POSSIBLE : 0;
                    } else {
                        // Opponent or gaea
                        if (piece == null) {
                            // base case
                            partialValue = 0;
                        } else if (piece.hasInfluence()) {
                            partialValue = piece.pointsNeededToHit();
                            // Update all surrounding fields
                            for (BoardDirection d : BoardDirection.values()) {
                                int[] newLoc = get(x, y, d);
                                if (newLoc == null) continue;
                                int newLocX = newLoc[0], newLocY = newLoc[1];
                                if (owner(newLocX, newLocY) != owner) continue;
                                obstacles[newLocX][newLocY] = Math.max
                                        (obstacles[newLocX][newLocY], partialValue);
                            }
                        } else {
                            // no influence => no points needed to hit
                            assert piece.pointsNeededToHit() == 0;
                            partialValue = 0;
                        }
                    }
                    obstacles[x][y] = Math.max(obstacles[x][y], partialValue);
                }
            }
        }

        List<int[]> getPossibleLocations(int level) {
            List<int[]> locations = new ArrayList<>();
            for (int x = 0; x < fieldss.length; x++) {
                for (int y = 0; y < fieldss[x].length; y++) {
                    if (obstacles[x][y] <= level) {
                        locations.add(new int[]{x, y});
                    }
                }
            }
            return locations;
        }
    }
}
