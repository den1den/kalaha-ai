package marblegame.players;

import java.util.Scanner;

/**
 * Created by dennis on 2-3-17.
 */
public class HumanPlayer implements Player {
    private String name;
    private Scanner scanner = new Scanner(System.in);

    public HumanPlayer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getMove() {
        System.out.print("Enter a moveNow\n");
        return scanner.nextInt();
    }
}