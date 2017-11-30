package marblegame.players;

import marblegame.BoardState;
import marblegame.Competition;
import marblegame.Match;
import marblegame.MatchBuilder;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by dennis on 10-11-17.
 */
public class AiPlayerTest {

    private Match match;
    private Competition competition;
    private BoardState board = null;
    private AiPlayer aiPlayer = null;
    private Player player = new SimplePlayer("Opponent");

    private void setup() {
        match = new MatchBuilder().setPlayers(null, player).setBoard(board).createMatch();
        aiPlayer = new AiPlayer("Test Player", match);
        competition = new Competition(match, aiPlayer);
    }

    @Test
    public void calcMove0() throws Exception {
        board = new BoardState(
                new int[]{0, 0, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0},
                new int[]{0, 0},
                0
        );
        setup();
        checkBestMove5();
    }

    @Test
    public void calcMove1() throws Exception {
        board = new BoardState(
                new int[]{0, 0, 2, 0, 0, 2, 0, 2, 0, 0, 0, 0},
                new int[]{0, 0},
                0
        );
        setup();
        checkBestMove5();
    }

    @Test
    public void calcMoveTerminates() throws Exception {
        board = new BoardState(
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0},
                0
        );
        setup();
        checkBestMove5();
    }

    @Test
    public void calcMoveTerminates2() throws Exception {
        board = new BoardState(
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