package marblegame.gamemechanics;

import marblegame.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static marblegame.Util.getFileWriter;
import static org.junit.Assert.*;

public class MatchTest {

    private static final boolean ABSOLUTELY_CERTAIN_GAMEMECHANICS_WORK = false;

    private static List<Match> getAllMatchedWith3DistinctOutcomes(int maxFieldValue, int nFields) {
        List<Match> matches = getAllMatches(maxFieldValue, nFields);
        List<Match> result = new ArrayList<>(matches.size());
        for (int mi = 0; mi < matches.size(); mi++) {
            Match match = matches.get(mi);

            if (has3PointsOutcomes(match)) {
                result.add(match);
            }
        }
        return result;
    }

    private static boolean has3PointsOutcomes(Match match) {
        List<Integer> possibleMoves = match.getMoves();
        if (possibleMoves.size() != 3) {
            return false;
        }
        HashSet<Integer> points = new HashSet<>(3);
        for (Integer move : possibleMoves) {
            BoardState s = match.getBoardStateAfterMove(move);
            if (!points.add(s.points[0])) {
                return false;
            }
        }
        return true;
    }

    private static List<Match> getAllMatches(int maxFieldValue, int nFields) {
        List<Match> matches = new ArrayList<>();
        List<List<Integer>> combos = getallCombosOf(maxFieldValue + 1, nFields, 0);
        for (int mi = 0; mi < combos.size(); mi++) {
            List<Integer> fields = combos.get(mi);
            Match match = new MatchBuilder(2).setBoard(new BoardState(
                fields.stream().mapToInt(v -> v).toArray(), new int[]{0, 0}
            )).createMatch();
            matches.add(match);
        }
        return matches;
    }

    private static List<List<Integer>> getallCombosOf(int range, int size, int i) {
        List<List<Integer>> r = new ArrayList<>();
        if (i > size - 1) {
            // Do not add, return empty list
        } else if (i == size - 1) {
            for (int x = 0; x < range; x++) {
                r.add(Collections.singletonList(x));
            }
        } else {
            List<List<Integer>> tail = getallCombosOf(range, size, i + 1);
            for (List<Integer> t : tail) {
                for (int x = 0; x < range; x++) {
                    List<Integer> lx = new ArrayList<>(t);
                    lx.add(x);
                    r.add(lx);
                }
            }
        }
        return r;
    }

    private void testMoveResult(int[] fields, int move, int gain, int pointsA, int pointsB, int[] finalFields) {
        Match m = new MatchBuilder(2).setBoard(new BoardState(
                fields, new int[]{0, 0}
        )).createMatch();
        assertTrue("Cannot do the moveNow", m.canMove(move));
        assertEquals(gain, m.move(move));
        assertEquals(pointsA, m.getPoints(0));
        assertEquals(pointsB, m.getPoints(1));
        if (finalFields != null)
            assertArrayEquals(finalFields, m.boardState.fields);
    }

    private void testPointsToWin(int[] fields, int scoreA, int scoreB, int point2WinA, int point2WinB) {
        Match m = new MatchBuilder(2).setBoard(new BoardState(
            fields, new int[]{scoreA, scoreB}
        )).createMatch();
        assertEquals("A points to win differ", point2WinA, m.getPointsToWin(0));
        assertEquals("B points to win differ", point2WinB, m.getPointsToWin(1));
    }

    private void test(int firstField, int p0, int p1, int ex0, int ex1) {
        Match m = createMatch(firstField, p0, p1);
        assertEquals(ex0, m.getPointsToWin(0));
        assertEquals(ex1, m.getPointsToWin(1));
        assertEquals(ex0 == 0, m.hasWon(0));
        assertEquals(ex1 == 0, m.hasWon(1));
    }

    private Match createMatch(int firstField, int player0Points, int player1Points) {
        return new MatchBuilder(2).setBoard(new BoardState(new int[]{
            firstField, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
        }, new int[]{player0Points, player1Points})).createMatch();
    }

    @Test
    public void testP2W_Field() {
        testPointsToWin(new int[]{
            1, 1, 1, 1, 1, 0,
            0, 1, 1, 1, 1, 1,
        }, 8, 5, 4, 7);
    }

    @Test
    public void testSimplePointMove() {
        // single gain
        testMoveResult(new int[]{
                //       .>>>>
                4, 4, 4, 4, 4, 4,
                4, 2, 4, 4, 4, 4
        }, 3, 1, 3, 0, new int[]{
                4, 4, 4, 0, 5, 5,
                5, 0, 4, 4, 4, 4
        });

        // double gain
        testMoveResult(new int[]{
                //       .>>>>
                4, 4, 4, 4, 4, 4,
                1, 2, 4, 4, 4, 4
        }, 3, 2, 5, 0, new int[]{
                4, 4, 4, 0, 5, 5,
                0, 0, 4, 4, 4, 4
        });

        // single win
        testMoveResult(new int[]{
                0, 0, 0, 0, 0, 1,
                2, 0, 0, 0, 0, 0
        }, 5, Match.MOVE_RESULT_WIN, 3, 0, new int[]{
                0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0
        });

        // double win
        testMoveResult(new int[]{
                0, 0, 0, 0, 0, 2,
                1, 2, 0, 0, 0, 0
        }, 5, Match.MOVE_RESULT_WIN, 5, 0, new int[]{
                0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0
        });

        // single pad
        testMoveResult(new int[]{
                //    .>>>>
                4, 4, 4, 4, 4, 4,
                2, 0, 0, 0, 0, 0
        }, 2, 1, 3, 0, new int[]{
                4, 4, 0, 5, 5, 5,
                0, 0, 0, 0, 0, 0
        });

        // double pad
        testMoveResult(new int[]{
                //       .>>>>
                4, 4, 4, 4, 4, 4,
                1, 2, 0, 0, 0, 0
        }, 3, 2, 5, 0, new int[]{
                4, 4, 4, 0, 5, 5,
                0, 0, 0, 0, 0, 0
        });
    }

    @Test
    public void getPointsToWinAllEq0() {
        test(0, 0, 0, -1, -1);
        test(1, 0, 0, 1, 1);
        test(2, 0, 0, 2, 2);
        test(3, 0, 0, 2, 2);
        test(4, 0, 0, 3, 3);
    }

    @Test
    public void getPointsToWinAllEq1() {
        test(0, 1, 1, -1, -1);
        test(1, 1, 1, 1, 1);
        test(2, 1, 1, 2, 2);
        test(3, 1, 1, 2, 2);
        test(4, 1, 1, 3, 3);
    }

    @Test
    public void getPointsToWinAllDiff() {
        test(0, 1, 0, 0, -1);
        test(1, 1, 0, 1, -1);
        test(2, 1, 0, 1, 2);
        test(3, 1, 0, 2, 3);
    }

    @Test
    public void getPointsToWinSimple() {
        Match m = createMatch(1, 1, 1);
        assertEquals(1, m.getPointsToWin(0));
        assertEquals(1, m.getPointsToWin(1));
    }

    @Test
    public void getPointsNoWinner() {
        Match m = createMatch(0, 1, 1);
        assertEquals(-1, m.getPointsToWin(0));
        assertEquals(-1, m.getPointsToWin(1));
    }

    @Test
    public void getPointsAlreadyWinner() {
        Match m = createMatch(0, 1, 0);
        assertEquals(0, m.getPointsToWin(0));
        assertEquals(-1, m.getPointsToWin(1));
    }

    @Test
    public void getPointsToWin0() {
        Match m = createMatch(20, 5, 5);
        assertEquals(11, m.getPointsToWin(0));
        assertEquals(m.getPointsToWin(0), m.getPointsToWin(1));
    }

    @Test
    public void getPointsToWin1() {
        Match m = createMatch(0, 1, 0);
        assertEquals(0, m.getPointsToWin(0));
        assertEquals(-1, m.getPointsToWin(1));
    }

    @Test
    public void getPointsToWin20() {
        test(20, 0, 20, -1, 1);
        test(20, 1, 20, 20, 1);
        test(20, 2, 20, 20, 2);

        test(20, 5, 25, -1, 1);
    }

    @Test
    public void writeBoardPoints2TestFile() throws IOException {
        if (!ABSOLUTELY_CERTAIN_GAMEMECHANICS_WORK) {
            return;
        }
        JSONArray matchesArr = new JSONArray();
        List<Match> getMatches;

        getMatches = getAllMatches(5, 6);
        //getMatches = getAllMatchedWith3DistinctOutcomes(5, 6);

        for (Match match : getMatches) {
            JSONObject matchObj = new JSONObject();
            matchObj.put("match", Match.Serializer.toJson(match));

            JSONArray movesArr = new JSONArray();
            List<Integer> moves = match.getMoves();
            for (Integer move : moves) {
                JSONObject moveObj = new JSONObject();
                movesArr.add(Util.JsonObj.o()
                    .o("moveNow", move)
                    .o("result", match.getBoardStateAfterMove(move)));
            }
            matchObj.put("moves", movesArr);

            matchesArr.add(matchObj);
        }


        JSONObject outObj = new JSONObject();
        outObj.put("matches", matchesArr);
        outObj.writeJSONString(getFileWriter("tests/all_possible_matches.json"));
    }

    @Test
    public void printAll3OptionsComboMatches() {
        int maxFieldValue = 5; // upto 7
        int totalNFields = 6;
        //List<MatchController> ms = getAllMatchedWith3DistinctOutcomes(4, 6);
        //List<MatchController> ms = getAllMatchedWith3DistinctOutcomes(5, 6);
        List<Match> ms = getAllMatchedWith3DistinctOutcomes(maxFieldValue, totalNFields);
        for (Match match : ms) {
            System.out.println(match.getBoardState());
        }
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        int printed = 0;
        for (Match match : ms) {
            BoardState bs = match.getBoardState();

            boolean containsMAX = false;
            for (int fi = 0; fi < bs.fields.length; fi++) {
                containsMAX |= bs.fields[fi] == maxFieldValue;
            }
            if (!containsMAX) {
                continue;
            }

            System.out.println(bs);
            List<Integer> possibleMoves = match.getMoves();
            assertEquals(3, possibleMoves.size());
            for (int i = 0; i < possibleMoves.size(); i++) {
                int move = possibleMoves.get(i);
                BoardState newBs = match.getBoardStateAfterMove(move);
                System.out.println("moveNow=" + move + " points=" + Arrays.toString(newBs.points) + " newboard=\n" + newBs + "");
            }
            System.out.println();
            printed++;
        }
        System.out.println("printed = " + printed);
    }

    @Test
    public void getAllCombosOf2Test() {
        // Check game mechanics
        for (int maxfieldValue = 0; maxfieldValue <= 7; maxfieldValue++) {
            for (int fpp = 3; fpp <= 3; fpp++) {
                int fields = fpp * 2;
                System.out.println("getAllMatchedWith3DistinctOutcomes(" + maxfieldValue + ", " + fields + ").size() = "
                    + getAllMatchedWith3DistinctOutcomes(maxfieldValue, fields).size());
            }
        }
    }
}