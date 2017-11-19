package marblegame.players;

import java.util.Scanner;

/**
 * Created by dennis on 2-3-17.
 */
public class HumanPlayer extends NamedPlayer {
    Scanner scanner = new Scanner(System.in);

    public HumanPlayer(String name) {
        super(name);
    }

    @Override
    public int getMove() {
        System.out.print("Enter a move\n");
        return scanner.nextInt();
    }
}