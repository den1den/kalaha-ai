package marblegame.players;

/**
 * Created by dennis on 2-3-17.
 */
public abstract class NamedPlayer implements Player {
    String name;

    public NamedPlayer(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public String getName() {
        return name;
    }
}
