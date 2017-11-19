package marblegame;


import marblegame.players.AiPlayer;
import marblegame.players.HumanPlayer;
import marblegame.players.RecordedPlayer;
import marblegame.players.SimplePlayer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;

/**
 * Created by dennis on 2-3-17.
 */
public class Main {

    Match m;

    public static void main(String[] args) throws FileNotFoundException {
        Main m = new Main();

        PrintStream out = System.out;
        System.setOut(new PrintStream("log"));
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        }));

        int MAX = 16;

        for (int a = MAX; a > 0; a--) {
            for (int b = MAX; b > 0; b--) {
                int winner = m.aiMatch(a, b);
                out.print(winner + "\t");
            }
            out.println();
        }
    }

    void unsetMatch() {
        m = new MatchBuilder().setPlayers(2).createMatch();
        RecordedPlayer<HumanPlayer> human = new RecordedPlayer<>(new HumanPlayer("Human"));
        RecordedPlayer<AiPlayer> ai = new RecordedPlayer<>(new AiPlayer("Computer", m));
        m.setPlayers(human, ai);

        int gain;
        System.out.println(m);
        while (m.isFinished() == -1) {
            gain = m.move();
            System.out.println("Player " + human + " does " + human.getLastMove() + " (gain " + gain + ")");

            gain = m.move();
            System.out.println("Player " + ai + " does " + ai.getLastMove() + " (gain " + gain + ")");
            System.out.println(m.toString());
        }
    }

    void detMatch() {
        m = new MatchBuilder().setPlayers(2).createMatch();
        SimplePlayer naive = new SimplePlayer("Naive player");
        RecordedPlayer<AiPlayer> ai = new RecordedPlayer<>(new AiPlayer("Computer", m));
        m.setPlayers(naive, ai);

        while (m.isFinished() == -1) {

            Iterator<Integer> possibleMoves = m.getPossibleMoves();
            if (!possibleMoves.hasNext()) {
                System.err.println("Naive lost");
                break;
            }
            naive.setMove(possibleMoves.next());

            int gain = m.move();
            System.out.println("Player " + naive + " does " + naive.getMove() + " (gain " + gain + ")");
            System.out.println(m);

            gain = m.move();
            System.out.println("Player " + ai + " does " + ai.getMove() + " (gain " + gain + ")");
            System.out.println(m);
        }
    }


    int aiMatch(int depth1, int depth2) {
        m = new MatchBuilder().setPlayers(2).createMatch();
        RecordedPlayer<AiPlayer> ai1 = new RecordedPlayer<>(new AiPlayer("-- 1 --", m, depth1));
        RecordedPlayer<AiPlayer> ai2 = new RecordedPlayer<>(new AiPlayer("-- 2 --", m, depth2));
        m.setPlayers(ai1, ai2);

        int gain, finished;
        System.out.println(m);
        while (true) {
            if ((finished = m.isFinished()) != -1) {
                break;
            }
            gain = m.move();
            System.out.println("Player " + ai1 + " does " + ai1.getLastMove() + " (gain " + gain + ")");

            if ((finished = m.isFinished()) != -1) {
                break;
            }
            gain = m.move();
            System.out.println("Player " + ai2 + " does " + ai2.getLastMove() + " (gain " + gain + ")");
            System.out.println(m.toString());
        }
        System.out.println("Player " + m.getPlayers()[finished] + " wins");
        return finished;
    }
}
