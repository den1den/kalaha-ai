package marblegame;


import marblegame.gamemechanics.Match;
import marblegame.gamemechanics.MatchBuilder;
import marblegame.players.RecordedPlayer;
import marblegame.solvers.AiSolver;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by dennis on 2-3-17.
 */
public class Main {

    PlayerCompetition c;

    public static void main(String[] args) throws IOException {
        PrintStream outStream = null;
        long t0 = System.currentTimeMillis();
        Main m = new Main();

        outStream = m.recordedAiBattle();

        String endResult = "Running time: " + (System.currentTimeMillis() - t0) + " ms";
        if (outStream != null)
            outStream.println(endResult);
        System.out.println(endResult);
    }

    private PrintStream recordedAiBattle() throws IOException {
        Main m = this;
        File outputFile = new File("ai-self-play-winners.txt");
        File output2File = new File("ai-self-play-moves.txt");
        // if(outputFile.exists()) throw new IOException("File still exists");
        outputFile.createNewFile();
        output2File.createNewFile();
        PrintStream outStream = new PrintStream(outputFile);
        PrintStream out2Stream = new PrintStream(output2File);

        int MAX;
        MAX = 17;
        MAX = 14;

        for (int a = MAX; a > 0; a--) {
            for (int b = MAX; b > 0; b--) {
                int winner = m.aiMatch(a, b);
                outStream.print(winner + "\t");
                out2Stream.print(m.c.getMoves() + "\t");
            }
            outStream.println();
            out2Stream.println();
        }

        outStream.close();
        out2Stream.close();
        return outStream;
    }

    int aiMatch(int depth1, int depth2) {
        Match match = new MatchBuilder().setPlayers(2).createMatch();
        RecordedPlayer ai1 = RecordedPlayer.recordedAi(match, new AiSolver(depth1));
        RecordedPlayer ai2 = RecordedPlayer.recordedAi(match, new AiSolver(depth2));
        c = new PlayerCompetition(match, ai1, ai2);

        int gain, finished;
        System.out.println(c.getMatch());
        do {
            gain = c.move();
            System.out.println("Player " + ai1 + " does " + ai1.getLastMove() + " (gain " + gain + ")");

            gain = c.move();
            System.out.println("Player " + ai2 + " does " + ai2.getLastMove() + " (gain " + gain + ")");
            System.out.println(c.getMatch());
        } while (true);
        // System.out.println("Player " + c.getPlayers()[finished] + " wins");
        // return finished;
    }
}
