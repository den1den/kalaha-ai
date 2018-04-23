package antijoy;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static antijoy.BoardBuilder.Type.*;

public class BoardTest {
    private Board board;

    @Test
    public void toStringTest() {
        board = new BoardBuilder(new BoardBuilder.Type[][]{
                new BoardBuilder.Type[]{XXX, _0_, _0_, ___, ___, ___, ___},
                new BoardBuilder.Type[]{XXXXX, P30, _0_, B_0, ___, ___, ___},
                new BoardBuilder.Type[]{XXX, _0_, _0_, _0_, P00, ___, ___},
                new BoardBuilder.Type[]{_____, ___, ___, ___, ___, ___, ___},
                new BoardBuilder.Type[]{___, ___, ___, ___, _1_, _1_, ___},
                new BoardBuilder.Type[]{_____, ___, ___, _1_, B_1, ___, ___},
                new BoardBuilder.Type[]{___, ___, ___, ___, ___, ___, ___},
                new BoardBuilder.Type[]{_____, ___, ___, ___, ___, ___, ___}
        }).toBoard();
        System.out.println("board = \n" + board);
    }

    @Test
    void directions() {
        board = new BoardBuilder(new BoardBuilder.Type[][]{
                new BoardBuilder.Type[]{XXX, XXX, XXX, XXX, XXX},
                new BoardBuilder.Type[]{XXXXX, ___, ___, XXX},
                new BoardBuilder.Type[]{XXX, ___, TR0, ___, XXX},
                new BoardBuilder.Type[]{XXXXX, ___, ___, XXX},
                new BoardBuilder.Type[]{XXX, XXX, XXX, XXX, XXX},
        }).toBoard();
        System.out.println("board = \n" + new BoardPrinter(board).toStringXY());
        assert board.getPieceType(4, 1) == PieceType.TREE;
        int i = 0;
        for (BoardDirection d :
                BoardDirection.values()) {
            int[] loc = board.get(4, 1, d);
            assert loc != null;
            board.set(loc[0], loc[1], ++i, null);
        }
        System.out.println("board = \n" + new BoardPrinter(board).toStringXY());
    }

    @Test
    public void getMoves() {
        board = new BoardBuilder(new BoardBuilder.Type[][]{
                new BoardBuilder.Type[]{XXX, ___, _0_, _0_},
                new BoardBuilder.Type[]{XXXXX, _0_, P00, _0_},
                new BoardBuilder.Type[]{XXX, ___, _0_, _0_},
        }).toBoard();
//        showBoardWithMoves(board);
        board = new BoardBuilder(new BoardBuilder.Type[][]{
                new BoardBuilder.Type[]{XXX, _1_, _0_},
                new BoardBuilder.Type[]{XXXXX, T21, P20},
                new BoardBuilder.Type[]{XXX, _1_, _0_},
                new BoardBuilder.Type[]{XXXXX, _1_, _0_},
        }).toBoard();
        showBoardWithMoves();
    }

    private void showBoardWithMoves() {
        System.out.println("board = \n" + new BoardPrinter(board).toStringXY());
        List<int[]> moves = board.getMoves(0);
        for (int[] m : moves) {
            System.out.println("m = " + m[0] + "," + m[1] + " -> " + m[2] + "," + m[3]);
        }
    }

    @Test
    public void getMovesTest1() {
        board = new BoardBuilder(new BoardBuilder.Type[][]{
                new BoardBuilder.Type[]{___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___},
                new BoardBuilder.Type[]{_____, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___},
                new BoardBuilder.Type[]{___, ___, ___, ___, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___, ___},
                new BoardBuilder.Type[]{_____, ___, ___, _0_, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___, ___},
                new BoardBuilder.Type[]{___, ___, ___, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___},
                new BoardBuilder.Type[]{_____, ___, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___},
                new BoardBuilder.Type[]{___, ___, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, ___},
                new BoardBuilder.Type[]{_____, _0_, _0_, _0_, _0_, _0_, P10, _0_, _0_, _0_, _0_, _0_, ___},
                new BoardBuilder.Type[]{___, ___, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, ___},
                new BoardBuilder.Type[]{_____, ___, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___},
                new BoardBuilder.Type[]{___, ___, ___, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___},
                new BoardBuilder.Type[]{_____, ___, ___, _0_, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___, ___},
                new BoardBuilder.Type[]{___, ___, ___, ___, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___, ___},
                new BoardBuilder.Type[]{_____, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___},
                new BoardBuilder.Type[]{___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___},
        }).toBoard();
        showGetMovesTest();
    }

    @Test
    public void getMovesTest2() {
        board = new BoardBuilder(new BoardBuilder.Type[][]{
                new BoardBuilder.Type[]{___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___},
                new BoardBuilder.Type[]{_____, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___},
                new BoardBuilder.Type[]{___, ___, ___, ___, P10, _0_, _0_, _0_, _0_, _0_, ___, ___, ___},
                new BoardBuilder.Type[]{_____, ___, ___, P20, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___, ___},
                new BoardBuilder.Type[]{___, ___, ___, P30, _0_, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___},
                new BoardBuilder.Type[]{_____, ___, TR1, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___},
                new BoardBuilder.Type[]{___, ___, B_0, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, ___},
                new BoardBuilder.Type[]{_____, H_0, _0_, _0_, _0_, _0_, P10, _0_, _0_, _0_, _0_, _0_, ___},
                new BoardBuilder.Type[]{___, ___, T10, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, ___},
                new BoardBuilder.Type[]{_____, ___, T20, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___},
                new BoardBuilder.Type[]{___, ___, ___, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___},
                new BoardBuilder.Type[]{_____, ___, ___, _0_, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___, ___},
                new BoardBuilder.Type[]{___, ___, ___, ___, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___, ___},
                new BoardBuilder.Type[]{_____, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___},
                new BoardBuilder.Type[]{___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___},
        }).toBoard();
        showGetMovesTest();
    }

    @Test
    public void getMovesTest3() {
        board = new BoardBuilder(new BoardBuilder.Type[][]{
                new BoardBuilder.Type[]{___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___},
                new BoardBuilder.Type[]{_____, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___},
                new BoardBuilder.Type[]{___, ___, ___, ___, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___, ___},
                new BoardBuilder.Type[]{_____, ___, ___, _0_, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___, ___},
                new BoardBuilder.Type[]{___, ___, ___, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___},
                new BoardBuilder.Type[]{_____, ___, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___},
                new BoardBuilder.Type[]{___, ___, _0_, _0_, T11, T11, _0_, _0_, _0_, _0_, _0_, _0_, ___},
                new BoardBuilder.Type[]{_____, _0_, _0_, T11, _0_, T11, P10, _0_, _0_, _0_, _0_, _0_, ___},
                new BoardBuilder.Type[]{___, ___, _0_, _0_, T11, T11, _0_, _0_, _0_, _0_, _0_, _0_, ___},
                new BoardBuilder.Type[]{_____, ___, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___},
                new BoardBuilder.Type[]{___, ___, ___, _0_, _0_, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___},
                new BoardBuilder.Type[]{_____, ___, ___, _0_, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___, ___},
                new BoardBuilder.Type[]{___, ___, ___, ___, _0_, _0_, _0_, _0_, _0_, _0_, ___, ___, ___},
                new BoardBuilder.Type[]{_____, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___},
                new BoardBuilder.Type[]{___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___, ___},
        }).toBoard();
        showGetMovesTest();
    }

    private void showGetMovesTest() {
        List<int[]> moves = board.getMoves(13, 3);

        BoardPrinter bp = new BoardPrinter(board);

        System.out.println(bp.toStringXY());
        for (int i = 0; i < moves.size(); i++) {
            int[] m = moves.get(i);
            System.out.println(Arrays.toString(m));

            board.set(m[2], m[3], 2, PieceType.P3);
        }

        System.out.println(bp.toStringXY());
        System.out.println(board);
    }
}