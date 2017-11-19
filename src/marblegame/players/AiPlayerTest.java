package marblegame.players;

import marblegame.Match;
import marblegame.MatchBuilder;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by dennis on 10-11-17.
 */
public class AiPlayerTest {

    Match match;
    Match.BoardState board = null;
    AiPlayer aiPlayer = null;
    Player player = new SimplePlayer("Opponent");

    public void setup() {
        match = new MatchBuilder().setPlayers(null, player).setBoard(board).createMatch();
        aiPlayer = new AiPlayer("Test Player", match);
        match.setPlayer(0, aiPlayer);
    }

    @Test
    public void calcMove0() throws Exception {
        board = new Match.BoardState.TestBoardState(
                new int[]{0, 0, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0},
                new int[]{0, 0},
                0
        );
        setup();
        checkBestMove5();
    }

    @Test
    public void calcMove1() throws Exception {
        board = new Match.BoardState.TestBoardState(
                new int[]{0, 0, 2, 0, 0, 2, 0, 2, 0, 0, 0, 0},
                new int[]{0, 0},
                0
        );
        setup();
        checkBestMove5();
    }

    @Test
    public void calcMoveTerminates() throws Exception {
        board = new Match.BoardState.TestBoardState(
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0},
                0
        );
        setup();
        checkBestMove5();
    }

    @Test
    public void calcMoveTerminates2() throws Exception {
        board = new Match.BoardState.TestBoardState(
                new int[]{0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0},
                0
        );
        setup();
        int move = aiPlayer.calcMove(1000);
    }

    private void checkBestMove5() {
        int move = aiPlayer.calcMove(2);
        Assert.assertEquals(5, move);
        match.move(move, board); //  needed?
    }

}