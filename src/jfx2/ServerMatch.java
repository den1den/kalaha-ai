package jfx2;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import marblegame.gamemechanics.Match;
import marblegame.net.PlayServer;
import marblegame.solvers.AiSolver;
import marblegame.solvers.Solver;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServerMatch extends AnimatedTwoPlayerVis implements Solver {

    final Server server = new Server();
    public CheckBox automaticCheckbox;
    public ChoiceBox<Integer> maxDepthChoiceBox;

    BooleanProperty commit = new SimpleBooleanProperty(false);
    private Match m = null;

    public ServerMatch() {
    }

    @Override
    void initialize() {
        super.initialize();
        server.start();

        ChangeListener<Boolean> serverListener = (observable, oldValue, newValue) -> {
            if (newValue) {
                synchronized (server) {
                    server.notifyAll();
                }
            }
        };
        commit.addListener(serverListener);
        automaticCheckbox.selectedProperty().addListener(serverListener);
        maxDepthChoiceBox.getItems().addAll(
                IntStream.range(1, 14).boxed().collect(Collectors.toList())
        );
        maxDepthChoiceBox.getSelectionModel().select(1);
    }

    @Override
    void clickedField(int move) {
        if (m == null) {
            System.err.println("No match received yet");
            selectedMove.set(-1);
        } else if (m.canMove(move)) {
            selectedMove.set(move);
        }
        System.out.println("this.move = " + selectedMove);
    }

    @Override
    public int solve(Match m) {
        synchronized (server) {
            this.m = m;
            setFields(m);

            if (automaticCheckbox.isSelected()) {
                // AI move
                selectedMove.set(doAiMove(m));
            } else {
                selectedMove.set(m.nextPossibleMove());
                while (!commit.get() || !m.canMove(selectedMove.get())) {
                    try {
                        server.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (automaticCheckbox.isSelected()) {
                        // AI move
                        selectedMove.set(doAiMove(m));
                        break;
                    }
                }
            }
            commit.set(false);
            return selectedMove.get();
        }
    }

    private int doAiMove(Match m) {
        maxDepthChoiceBox.setDisable(true);
        int maxDepth = maxDepthChoiceBox.getValue();
        AiSolver aip = new AiSolver(maxDepth);
        int calcMove = aip.solve(m);
        maxDepthChoiceBox.setDisable(false);
        return calcMove;
    }

    public void commitMove(ActionEvent actionEvent) {
        commit.setValue(true);
    }

    @Override
    public void close() {
        super.close();
        server.cancel();
    }

    private class Server extends Service<Void> {

        PlayServer server;

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    server = PlayServer.getSimplePlayServer(new AiSolver());
                    server.setSolver(ServerMatch.this);
                    server.run(); // Never returns
                    return null;
                }
            };
        }

        @Override
        public boolean cancel() {
            try {
                server.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return super.cancel();
        }
    }
}
