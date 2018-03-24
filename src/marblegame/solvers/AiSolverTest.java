package marblegame.solvers;

import marblegame.PlayerCompetition;
import marblegame.gamemechanics.BoardState;
import marblegame.gamemechanics.Competition;
import marblegame.gamemechanics.Match;
import marblegame.gamemechanics.MatchBuilder;
import marblegame.players.AutomaticPlayer;
import marblegame.players.Player;
import marblegame.players.SimplePlayer;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by dennis on 10-11-17.
 */
public class AiSolverTest {

    private Match match;
    private Competition competition;
    private BoardState board = null;
    private AiSolver aiSolverPlayer = null;
    private Player player = new SimplePlayer();

    private void setup() {
        aiSolverPlayer = new AiSolver2();
        match = new MatchBuilder().setPlayers(2).setBoard(board).createMatch();
        competition = new PlayerCompetition(match, new AutomaticPlayer(aiSolverPlayer));
    }

    @Test
    public void calcMove0() {
        board = new BoardState(
                new int[]{0, 0, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0},
                new int[]{0, 0},
                0
        );
        setup();
        checkBestMove5();
    }

    @Test
    public void calcMove1() {
        board = new BoardState(
                new int[]{0, 0, 2, 0, 0, 2, 0, 2, 0, 0, 0, 0},
                new int[]{0, 0},
                0
        );
        setup();
        checkBestMove5();
    }

    @Test
    public void calcMoveTerminates() {
        board = new BoardState(
            new int[]{0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0},
                0
        );
        setup();
        checkBestMove5();
    }

    @Test
    public void calcMoveTerminates2() {
        board = new BoardState(
                new int[]{0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0},
                0
        );
        setup();
        aiSolverPlayer.setDepth(1000);
        int move = aiSolverPlayer.solve(match);
    }

    private void checkBestMove5() {
        aiSolverPlayer.setDepth(2);
        int move = aiSolverPlayer.solve(match);
        Assert.assertEquals(5, move);
        match.move(move, board); //  needed?
    }

}