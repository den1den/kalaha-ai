package antijoy;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private static final int NOT_POSSIBLE = PieceType.values().length;
    PieceType[][] fieldss; // fieldss[x][y] == null => empty
    int[][] owner; // owner[x][y] == -1 => gia, -2 => disabled
    int players;

    public Board(PieceType[][] fieldss, int[][] owner, int players) {
        this.fieldss = fieldss;
        this.owner = owner;
        this.players = players;
    }

    int[] get(int x, int y, Direction d) {
        assert x >= 0 && x < fieldss.length;
        assert y >= 0 && y < fieldss[x].length;
        switch (d) {
            case Up:
                return y - 1 >= 0 ? new int[]{x, y - 1} : null;
            case Down:
                return y + 1 < fieldss[x].length ? new int[]{x, y + 1} : null;
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
        }
        throw new Error();
    }

    PieceType getPieceType(int x, int y, Direction d) {
        int[] loc = get(x, y, d);
        if (loc == null) return null;
        return getPieceType(loc[0], loc[1]);
    }

    PieceType getPieceType(int x, int y) {
        return fieldss[x][y];
    }

    int owner(int x, int y) {
        return owner[x][y];
    }

    List<int[]> getMoves(int player) {
        List<int[]> moves = new ArrayList<>();
        MoveablePieces moveablePieces = new MoveablePieces(player);
        MoveScheme ms = new MoveScheme(player);
        for (int i = 0; i < moveablePieces.locations.length; i++) {
            int px = moveablePieces.locations[i][0], py = moveablePieces.locations[i][1];
            int pieceLevel = getPieceType(px, py).getLevel();
            List<int[]> possibleLocations = ms.getPossibleLocations(pieceLevel);
            for (int[] loc :
                possibleLocations) {
                moves.add(new int[]{px, py, loc[0], loc[1]});
            }
        }
        return moves;
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
        StringBuilder s = new StringBuilder();
        int maxY = 0, maxX = owner.length;
        for (int x = 0; x < fieldss.length; x++) {
            maxY = Math.max(maxY, fieldss[x].length);
        }
        for (int y = 0; y < maxY; y++) {
//            for (int x = 0; x < maxX; x+=2) {
//                s.append(" / - \\ ");
//            }
            for (int x = 0; x < maxX; x += 2) {
                if (x >= owner.length || y >= owner[x].length) continue;
                PieceType pt = getPieceType(x, y);
                if (pt != null)
                    s.append(owner[x][y]).append(pt.toString().substring(0, 2));
                else
                    s.append(" - ");
                s.append(" ");
            }
            s.append(System.lineSeparator());
            s.append("  ");
            for (int x = 1; x < maxX; x += 2) {
                if (x >= owner.length || y >= owner[x].length) continue;
                PieceType pt = getPieceType(x, y);
                if (pt != null)
                    s.append(owner[x][y]).append(pt.toString().substring(0, 2));
                else
                    s.append(" - ");
                s.append(" ");
            }
            s.append(System.lineSeparator());
        }
        return s.toString();
    }

    public enum PieceType {
        P0, P1, P2, P3,
        HOUSE, TOWER1, TOWER2, BASE, TREE;

        public int getPrice() {
            switch (this) {
                case P0:
                    return 10;
                case P1:
                    return 20;
                case P2:
                    return 30;
                case P3:
                    return 40;
                case TOWER1:
                    return 15;
                case TOWER2:
                    return 35;
                default:
                    return -1;
            }
        }

        public boolean canHit11(PieceType type) {
            assert canMove();
            switch (type) {
                case TREE:
                case HOUSE:
                    return true;
                case BASE:
                    return ordinal() >= 1;
                case TOWER1:
                    return ordinal() >= 3;
                case TOWER2:
                    return ordinal() >= 4;
                case P3:
                    return this == P3;
                default:
                    assert type.canMove();
                    return type.ordinal() < ordinal();
            }
        }

        private boolean canMove() {
            switch (this) {
                case P0:
                case P1:
                case P2:
                case P3:
                    return true;
                default:
                    return false;
            }
        }

        public boolean isBuilding() {
            switch (this) {
                case TOWER2:
                case TOWER1:
                case HOUSE:
                case BASE:
                    return true;
                default:
                    return false;
            }
        }

        public boolean hasInfluence() {
            switch (this) {
                case BASE:
                case TOWER1:
                case TOWER2:
                case P3:
                case P2:
                case P1:
                case P0:
                    return true;
                default:
                    return false;
            }
        }

        public int pointsNeededToHit() {
            switch (this) {
                case BASE:
                case P0:
                    return 1;
                case TOWER1:
                case P1:
                    return 2;
                case TOWER2:
                case P2:
                case P3:
                    return 3;
                default:
                    return 0;
            }
        }

        public int getLevel() {
            int points = ordinal();
            assert points <= 3;
            return points;
        }
    }

    private class MoveablePieces {
        int[][] locations;

        public MoveablePieces(int player) {
            ArrayList<int[]> locations = new ArrayList<>();
            for (int x = 0; x < owner.length; x++) {
                for (int y = 0; y < owner[x].length; y++) {
                    if (owner[x][y] == player) {
                        if (fieldss[x][y].canMove()) {
                            locations.add(new int[]{x, y});
                        }
                    }
                }
            }
            this.locations = (int[][]) locations.toArray();
        }
    }

    public enum Direction {
        RightDown, Down, LeftDown, LeftUp, Up, RightUp
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
                        // no fieldss
                        continue;
                    } else if (owner == player) {
                        // your own fieldss
                        partialValue = piece.isBuilding() ? NOT_POSSIBLE : 0;
                    } else {
                        // Opponent or gaea
                        if (piece == null) continue;
                        else if (piece.hasInfluence()) {
                            partialValue = piece.pointsNeededToHit();
                            // Update all surrounding fields
                            for (Direction d : Direction.values()) {
                                int[] newLoc = get(x, y, d);
                                if (newLoc == null) continue;
                                int newLocX = newLoc[0], newLocY = newLoc[1];
                                obstacles[newLocX][newLocY] = Math.max
                                    (obstacles[newLocX][newLocY], partialValue);
                            }
                        } else {
                            // no influence => no points needed to hit
                            assert piece.pointsNeededToHit() == 0;
                            continue;
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
