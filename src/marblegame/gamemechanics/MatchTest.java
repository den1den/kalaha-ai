package marblegame.gamemechanics;

import org.junit.Test;

import static org.junit.Assert.*;

public class MatchTest {

    private void testPoints(int[] fields, int move, int gain, int pointsA, int pointsB, int[] finalFields) {
        Match m = new MatchBuilder(2).setBoard(new BoardState(
                fields, new int[]{0, 0}
        )).createMatch();
        assertTrue("Cannot do the move", m.canMove(move));
        assertEquals(gain, m.move(move));
        assertEquals(pointsA, m.getPoints(0));
        assertEquals(pointsB, m.getPoints(1));
        if (finalFields != null)
            assertArrayEquals(finalFields, m.boardState.fields);
    }

    @Test
    public void testSimplePointMove() {
        // single gain
        testPoints(new int[]{
                //       .>>>>
                4, 4, 4, 4, 4, 4,
                4, 2, 4, 4, 4, 4
        }, 3, 1, 3, 0, new int[]{
                4, 4, 4, 0, 5, 5,
                5, 0, 4, 4, 4, 4
        });

        // double gain
        testPoints(new int[]{
                //       .>>>>
                4, 4, 4, 4, 4, 4,
                1, 2, 4, 4, 4, 4
        }, 3, 2, 5, 0, new int[]{
                4, 4, 4, 0, 5, 5,
                0, 0, 4, 4, 4, 4
        });

        // single win
        testPoints(new int[]{
                0, 0, 0, 0, 0, 1,
                2, 0, 0, 0, 0, 0
        }, 5, Match.MOVE_RESULT_WIN, 3, 0, new int[]{
                0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0
        });

        // double win
        testPoints(new int[]{
                0, 0, 0, 0, 0, 2,
                1, 2, 0, 0, 0, 0
        }, 5, Match.MOVE_RESULT_WIN, 5, 0, new int[]{
                0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0
        });

        // single pad
        testPoints(new int[]{
                //    .>>>>
                4, 4, 4, 4, 4, 4,
                2, 0, 0, 0, 0, 0
        }, 2, 1, 3, 0, new int[]{
                4, 4, 0, 5, 5, 5,
                0, 0, 0, 0, 0, 0
        });

        // double pad
        testPoints(new int[]{
                //       .>>>>
                4, 4, 4, 4, 4, 4,
                1, 2, 0, 0, 0, 0
        }, 3, 2, 5, 0, new int[]{
                4, 4, 4, 0, 5, 5,
                0, 0, 0, 0, 0, 0
        });
    }

    private void test(int t, int p0, int p1, int ex0, int ex1) {
        Match m = getMatch(t, p0, p1);
        assertEquals(ex0, m.getPointsToWin(0));
        assertEquals(ex1, m.getPointsToWin(1));
        assertEquals(ex0 == 0, m.hasWon(0));
        assertEquals(ex1 == 0, m.hasWon(1));
    }

    @Test
    public void getPointsToWinAllEq0() throws Exception {
        test(0, 0, 0, -1, -1);
        test(1, 0, 0, 1, 1);
        test(2, 0, 0, 2, 2);
        test(3, 0, 0, 2, 2);
        test(4, 0, 0, 3, 3);
    }

    @Test
    public void getPointsToWinAllEq1() throws Exception {
        test(0, 1, 1, -1, -1);
        test(1, 1, 1, 1, 1);
        test(2, 1, 1, 2, 2);
        test(3, 1, 1, 2, 2);
        test(4, 1, 1, 3, 3);
    }

    @Test
    public void getPointsToWinAllDiff() throws Exception {
        test(0, 1, 0, 0, -1);
        test(1, 1, 0, 1, -1);
        test(2, 1, 0, 1, 2);
        test(3, 1, 0, 2, 3);
    }

    @Test
    public void getPointsToWinSimple() throws Exception {
        Match m = getMatch(1, 1, 1);
        assertEquals(1, m.getPointsToWin(0));
        assertEquals(1, m.getPointsToWin(1));
    }

    @Test
    public void getPointsNoWinner() throws Exception {
        Match m = getMatch(0, 1, 1);
        assertEquals(-1, m.getPointsToWin(0));
        assertEquals(-1, m.getPointsToWin(1));
    }

    @Test
    public void getPointsAlreadyWinner() throws Exception {
        Match m = getMatch(0, 1, 0);
        assertEquals(0, m.getPointsToWin(0));
        assertEquals(-1, m.getPointsToWin(1));
    }

    @Test
    public void getPointsToWin0() throws Exception {
        Match m = getMatch(20, 5, 5);
        assertEquals(11, m.getPointsToWin(0));
        assertEquals(m.getPointsToWin(0), m.getPointsToWin(1));
    }

    @Test
    public void getPointsToWin1() throws Exception {
        Match m = getMatch(0, 1, 0);
        assertEquals(0, m.getPointsToWin(0));
        assertEquals(-1, m.getPointsToWin(1));
    }

    @Test
    public void getPointsToWin20() throws Exception {
        test(20, 0, 20, -1, 1);
        test(20, 1, 20, 20, 1);
        test(20, 2, 20, 20, 2);

        test(20, 5, 25, -1, 1);
    }

    private Match getMatch(int boardPoints, int player0Points, int player1Points) {
        return new MatchBuilder(2).setBoard(new BoardState(new int[]{
                boardPoints, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
        }, new int[]{player0Points, player1Points})).createMatch();
    }

}