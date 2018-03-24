package jfx3.opponent;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import marblegame.net.PlayServer;
import marblegame.solvers.Solver;

import java.io.IOException;

public class ServerManager extends ConnectionService<PlayServer> {
    public final IntegerProperty difficulty = new SimpleIntegerProperty();
    private Solver solver = null;

    public void setSolver(Solver solver) {
        this.solver = solver;
    }

    public void setPort(int port) {
        cancel();
        PlayServer playServer = new PlayServer(port);
        setNextObj(playServer);
        restart();
    }

    @Override
    void onUpdate(PlayServer newObject) {
        newObject.setSolver(solver);
    }

    @Override
    protected Task createTaskImpl() {
        return new Task() {
            @Override
            protected Result<PlayServer> callImpl(PlayServer object) throws IOException {
                if (solver == null) {
                    object.listen();
                } else {
                    object.setSolver(solver);
                    object.runBlocking();
                }
                return null;
            }
        };
    }
}
