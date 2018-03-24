package jfx2;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

    @FXML
    void initialize() {
        server.start();
        System.out.println("server.isRunning() = " + server.isRunning());

        ChangeListener<Boolean> serverListener = (observable, oldValue, newValue) -> {
            if (newValue) {
                synchronized (server) {
                    System.out.println("Server wake up!!!!");
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
        System.out.println("this.moveNow = " + selectedMove);
    }

    @Override
    public int solve(Match m) {
        synchronized (server) {
            this.m = m;
            setFields(m);

            if (automaticCheckbox.isSelected()) {
                // AI moveNow
                selectedMove.set(doAiMove(m));
            } else {
                selectedMove.set(m.nextPossibleMove());
                while (!commit.get() || !m.canMove(selectedMove.get())) {
                    try {
                        System.out.println("Server is goin to sleep... " + Thread.currentThread());
                        server.wait();
                        System.out.println("What what? I just woke up");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (automaticCheckbox.isSelected()) {
                        // AI moveNow
                        System.out.println("Doing automatic moveNow after wakeup");
                        selectedMove.set(doAiMove(m));
                        break;
                    }
                }
            }
            System.out.println("Doing custom moveNow after wakeup");
            resetField();
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
        System.out.println("Press the Do Move button");
        commit.setValue(true);
    }

    @Override
    public void close() {
        super.close();
        server.cancel();
    }

    private class Server extends Service<Void> {

        volatile PlayServer server;

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() {
                    System.out.println("called task");
                    try {
                        server = new PlayServer(6022);
                        server.setSolver(ServerMatch.this);
                        server.runBlocking(); // Never returns
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        }

        @Override
        public boolean cancel() {
            try {
                if (server == null) {
                    return true;
                }
                server.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return super.cancel();
        }
    }
}
