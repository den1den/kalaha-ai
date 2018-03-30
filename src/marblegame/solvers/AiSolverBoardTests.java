package marblegame.solvers;

import marblegame.gamemechanics.BoardState;
import marblegame.gamemechanics.Match;
import marblegame.gamemechanics.MatchBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class AiSolverBoardTests {
    AiSolver2 solver2;

    public void testBoardRatings(int[] fields) {
        Match m = new MatchBuilder(2).setBoard(new BoardState(
            fields, new int[]{0, 0}
        )).createMatch();
        solver2.rating(m.getBoardState());

    }

    @Before
    public void setUp() {
        solver2 = new AiSolver2();
    }

    @Test
    public void testTwoStepWinningOptionMove() {
        testBoard(new int[]{
                3, 0, 0, 0, 1,
                4, 0, 0, 0, 0,
            }, new int[]{0, 0},
            5, 3);
    }

    @Test
    public void testSimpleWinningOptionMove() {
        testBoard(new int[]{
                0, 0, 0, 0, 1, 1,
                2, 0, 0, 0, 0, 0,
            }, new int[]{0, 0},
            5, 2);
    }

    @Test
    public void testSimpleNoOptionMove() {
        testBoard(new int[]{
                0, 0, 0, 0, 0, 1,
                2, 0, 0, 0, 0, 0,
            }, new int[]{0, 0},
            5, 2);
    }

    @Test
    public void testSimpleGoodMove() {
        testBoard(new int[]{
                2, 3, 1,
                2, 3, 3,
            }, new int[]{0, 0},
            2, 2);
    }

    @Test
    public void testDefenseMove() {
        testBoardNot(new int[]{
                1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 2,
            }, new int[]{0, 0},
            0, 2);
    }

    private void testBoard(int[] board, int[] points, int expMove, int depth) {
        Match match = new MatchBuilder(2).setBoard(new BoardState(board, points)).createMatch();
        System.out.println(match);
        solver2.setDepth(depth);
        int move = solver2.solve(match);
        assertEquals("Wrong moveNow", expMove, move);
    }

    private void testBoardNot(int[] board, int[] points, int expNotMove, int depth) {
        Match match = new MatchBuilder(2).setBoard(new BoardState(board, points)).createMatch();
        System.out.println(match);
        solver2.setDepth(depth);
        int move = solver2.solve(match);
        assertNotEquals(expNotMove, move, "Wrong moveNow");
    }
}