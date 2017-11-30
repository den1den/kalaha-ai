package marblegame;


import marblegame.players.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;

/**
 * Created by dennis on 2-3-17.
 */
public class Main {

    Competition c;

    public static void main(String[] args) throws IOException {
        PrintStream outStream = null;
        long t0 = System.currentTimeMillis();
        Main m = new Main();

        //outStream = m.recordedAiBattle();
        m.manualInputMatch();

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

    void unsetMatch() {
        Match match = new MatchBuilder().setPlayers(2).createMatch();
        RecordedPlayer<HumanPlayer> human = new RecordedPlayer<>(new HumanPlayer("Human"));
        RecordedPlayer<AiPlayer> ai = new RecordedPlayer<>(new AiPlayer("Computer", match));
        c = new Competition(match, human, ai);

        int gain;
        System.out.println(match);
        while (c.isFinished() == -1) {
            gain = c.move();
            System.out.println("Player " + human + " does " + c.getLastMove() + " (gain " + gain + ")");

            gain = c.move();
            System.out.println("Player " + ai + " does " + c.getLastMove() + " (gain " + gain + ")");
            System.out.println(match.toString());
        }

    }

    void manualInputMatch() {
        Match match = new MatchBuilder().setPlayers(2).createMatch();
        //RecordedPlayer<HumanPlayer> human = new RecordedPlayer<>(new HumanPlayer("Human"));
        RecordedPlayer<NaivePlayer> human = new RecordedPlayer<>(new NaivePlayer("First move", match));
        String host;
        host = "vandenbrand.eu";
        host = "localhost";
        NetworkPlayer ai = new NetworkPlayer(host, match);
        c = new Competition(match, human, ai);

        while (c.isFinished() == -1) {
            System.out.println(c.getMatch());
            // Ask human move
            int gain = c.move();
            System.out.println("gain = " + gain);

            gain = c.move();
            System.out.println("gain = " + gain);
        }
    }

    void detMatch() {
        Match match = new MatchBuilder().setPlayers(2).createMatch();
        SimplePlayer naive = new SimplePlayer("Naive player");
        RecordedPlayer<AiPlayer> ai = new RecordedPlayer<>(new AiPlayer("Computer", match));
        c = new Competition(match, naive, ai);

        while (c.isFinished() == -1) {

            Iterator<Integer> possibleMoves = c.getMatch().getPossibleMoves();
            if (!possibleMoves.hasNext()) {
                System.err.println("Naive lost");
                break;
            }
            naive.setMove(possibleMoves.next());

            int gain = c.move();
            System.out.println("Player " + naive + " does " + naive.getMove() + " (gain " + gain + ")");
            System.out.println(c.getMatch());

            gain = c.move();
            System.out.println("Player " + ai + " does " + ai.getMove() + " (gain " + gain + ")");
            System.out.println(c.getMatch());
        }
    }

    int aiMatch(int depth1, int depth2) {
        Match match = new MatchBuilder().setPlayers(2).createMatch();
        RecordedPlayer<AiPlayer> ai1 = new RecordedPlayer<>(new AiPlayer("-- 1 --", match, depth1));
        RecordedPlayer<AiPlayer> ai2 = new RecordedPlayer<>(new AiPlayer("-- 2 --", match, depth2));
        c = new Competition(match, ai1, ai2);

        int gain, finished;
        System.out.println(c.getMatch());
        while (true) {
            if ((finished = c.isFinished()) != -1) {
                break;
            }
            gain = c.move();
            System.out.println("Player " + ai1 + " does " + ai1.getLastMove() + " (gain " + gain + ")");

            if ((finished = c.isFinished()) != -1) {
                break;
            }
            gain = c.move();
            System.out.println("Player " + ai2 + " does " + ai2.getLastMove() + " (gain " + gain + ")");
            System.out.println(c.getMatch());
        }
        System.out.println("Player " + c.getPlayers()[finished] + " wins");
        return finished;
    }
}
