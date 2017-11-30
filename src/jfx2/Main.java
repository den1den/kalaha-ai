package jfx2;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import marblegame.BoardState;
import marblegame.Competition;
import marblegame.Match;
import marblegame.MatchBuilder;
import marblegame.players.NetworkPlayer;
import marblegame.players.Player;
import marblegame.players.SimplePlayer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    private static final ExecutorService networkWorkerExecutor = Executors.newSingleThreadExecutor();
    private static Paint defaultColor = Color.DODGERBLUE;
    private static Paint selectedColor = Color.BLUE;
    private static Paint selectedOpponentColor = Color.RED;
    private static String FIELD_INDEX_PROPERTY_KEY = "field index";
    private static long ANIMATION_TIMEOUT = 100;
    private final AtomicBoolean executing = new AtomicBoolean(false);
    public ChoiceBox<String> opponentSelector;
    public GridPane gridpane;
    public Text opponentText;
    public Text leftScoreText;
    public Text rightScoreText;
    private SimplePlayer humanPlayer;
    private List<Player> players;
    private Match match;
    private Competition competition;
    private SetupConnectionService setupConnectionService = new SetupConnectionService();
    private DoNetworkMoveService moveService = new DoNetworkMoveService();
    private AnimatorService animatorService = new AnimatorService();
    private SimpleIntegerProperty selectedMove = new SimpleIntegerProperty(-1);
    private SimpleIntegerProperty opponentSelectedMove = new SimpleIntegerProperty(-1);

    public Main() {
        setupConnectionService.setExecutor(networkWorkerExecutor);
        moveService.setExecutor(networkWorkerExecutor);

        humanPlayer = new SimplePlayer("Human");

        final Player[] players = new Player[2];
        players[0] = humanPlayer;
        this.players = Collections.synchronizedList(Arrays.asList(players));
        this.match = new MatchBuilder(players.length).createMatch();
        this.competition = new Competition(match, players);
    }

    public static void main(String[] args) {
        Main main = new Main();
    }

    @FXML
    protected void initialize() {
        // setup buttons
        ObservableList<Node> fields = gridpane.getChildren();
        int fieldIndex = 0;
        for (Node child : fields) {
            child.getProperties().put(FIELD_INDEX_PROPERTY_KEY, fieldIndex);
            fieldIndex++;
        }

        // setup opponent selector
        opponentSelector.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Cancel previous setup
            setupConnectionService.cancel();
            setupConnectionService.reset();

            // Do new setup
            final String host = newValue;
            setupConnectionService.setHost(host);
            setupConnectionService.setOnSucceeded(event -> {
                NetworkPlayer result = (NetworkPlayer) event.getSource().getValue();
                if (result.isConnected()) {
                    players.set(1, result);
                    opponentText.setText("Connected to " + host);
                } else {
                    opponentText.setText("Could not connect to " + host);
                }
            });
            setupConnectionService.start();
        });
        opponentSelector.getItems().addAll(
                "vandenbrand.eu",
                "localhost"
        );
        opponentSelector.getSelectionModel().selectFirst();

        // Set selection colorings
        selectedMove.addListener(new SelectionChangeListener(selectedColor));
        opponentSelectedMove.addListener(new SelectionChangeListener(selectedOpponentColor));

        // Set labels
        displayBoardState(competition.getState());
    }

    private void displayBoardState(BoardState boardState) {
        int[] fields = boardState.getFields();
        for (int i = 0; i < fields.length; i++) {
            setField(i, fields[i]);
        }
        int[] points = boardState.getAllPoints();
        leftScoreText.setText("Computer: " + points[1]);
        rightScoreText.setText("Human: " + points[0]);
    }

    private void setField(int index, int number) {
        System.out.println("index = [" + index + "], number = [" + number + "]");
        Pane selectedParent = ((Pane) this.gridpane.getChildren().get(index));
        Text shape = (Text) selectedParent.getChildren().get(1);
        shape.setText(String.valueOf(number));
    }

    private Shape getShape(int index) {
        Pane selectedParent = ((Pane) this.gridpane.getChildren().get(index));
        Circle shape = (Circle) selectedParent.getChildren().get(0);
        return shape;
    }

    public void clickedField(MouseEvent event) {
        Node source = (Node) event.getSource();
        if (this.gridpane.equals(source.getParent())) {
            int index = (int) source.getProperties().get(FIELD_INDEX_PROPERTY_KEY);
            System.out.println("clicked index = " + index);
            if (competition.isInRange(index)) { // competition.canSet
                clickMove(index);
            } else {
                unClickMove();
            }
        }
    }

    private void unClickMove() {
        selectedMove.set(-1);
    }

    private void clickMove(int index) {
        selectedMove.set(index);
    }

    public void doMove(ActionEvent actionEvent) {
        humanPlayer.setMove(selectedMove.get());
        executeMove();
    }

    private void executeMove() {
        if (!executing.compareAndSet(false, true)) {
            System.err.println("Still executing");
            return;
        }
        if (humanPlayer.getMove() == -1) {
            System.err.println("No move set");
            executing.set(false);
            return;
        }

        // Do human move
        final BoardState preHumanBoardState = new BoardState(competition.getState());
        int gain = competition.move();
        int move = competition.getLastMove();
        final BoardState preNetworkBoardState = new BoardState(competition.getState());

        // Start human animation
        animatorService.setTask(new MoveAnimationTask(selectedMove, move, false, preHumanBoardState));
        animatorService.cancel();
        animatorService.reset();
        animatorService.start();
        System.out.println("Started human animation");

        // Start network request
        moveService.cancel();
        moveService.reset();
        moveService.setOnSucceeded(event -> {
            opponentText.setText("Connection OK");

            final int networkMove = (int) event.getSource().getValue();

            // Set task to do when human animation is completed
            EventHandler<WorkerStateEvent> onHumanAnimationCompleted = event1 -> {
                competition.move(networkMove);
                animatorService.setTask(new MoveAnimationTask(opponentSelectedMove, networkMove, true, preNetworkBoardState));
                animatorService.reset();
                animatorService.setOnSucceeded(e -> {
                    executing.set(false);
                });
                animatorService.start();
            };
            // Check if task should be scheduled or executed now
            if (animatorService.isRunning()) {
                animatorService.setOnSucceeded(onHumanAnimationCompleted);
            } else {
                onHumanAnimationCompleted.handle(null);
            }
        });
        moveService.setOnFailed((event -> {
            opponentText.setText("Connection FAIL, trying again...");
            moveService.reset();
            moveService.start();
            synchronized (this) {
                try {
                    wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
        moveService.start();
    }

    private class SetupConnectionService extends RerunnableService<NetworkPlayer> {
        String host;

        private void setHost(String host) {
            this.host = host;
        }

        @Override
        protected Task<NetworkPlayer> createTask() {
            final String host = this.host;
            return new Task<NetworkPlayer>() {
                @Override
                protected NetworkPlayer call() throws Exception {
                    return new NetworkPlayer(host, match);
                }
            };
        }
    }

    private class DoNetworkMoveService extends RerunnableService<Integer> {
        @Override
        protected Task<Integer> createTask() {
            final Player player = players.get(1);
            return new Task<Integer>() {
                @Override
                protected Integer call() throws Exception {
                    int move = player.getMove();
                    if (move == -1) {
                        throw new Error("Could not get move of " + player);
                    }
                    return move;
                }
            };
        }
    }

    private class MoveAnimationTask extends Task {
        final boolean animateFirst;
        final int moveIndex;
        final BoardState state;
        final IntegerProperty selectedMove;

        MoveAnimationTask(IntegerProperty selectedMove, int moveIndex, boolean animateFirst, BoardState state) {
            this.selectedMove = selectedMove;
            this.animateFirst = animateFirst;
            this.moveIndex = moveIndex;
            this.state = state;
        }

        @Override
        protected Object call() throws Exception {
            try {
                int[] fields = state.getFields();
                int stones = fields[moveIndex];
                synchronized (this) {
                    setField(moveIndex, 0);
                    if (animateFirst) {
                        selectedMove.set(moveIndex);
                        wait(ANIMATION_TIMEOUT);
                    }
                    for (int i = moveIndex + 1; i <= moveIndex + stones; i++) {
                        int fieldIndex = i % fields.length;
                        setField(fieldIndex, fields[fieldIndex] + 1);
                        selectedMove.set(fieldIndex);
                        wait(ANIMATION_TIMEOUT);
                    }
                    displayBoardState(competition.getState());
                    selectedMove.set(-1);
                }
            } catch (Exception e) {
                System.err.println("Could not animate" + " isCancelled() = " + isCancelled()
                        + " isRunning() = " + isRunning()
                        + " isDone() = " + isDone());
                e.printStackTrace();
                throw e;
            }
            System.out.println("Animation task done");
            return null;
        }
    }

    private class AnimatorService extends RerunnableService {
        private Task task;

        void setTask(Task task) {
            this.task = task;
        }

        @Override
        protected Task createTask() {
            return task;
        }
    }

    private class SelectionChangeListener implements ChangeListener<Number> {
        final Paint selectedColor;

        public SelectionChangeListener(Paint selectedColor) {
            this.selectedColor = selectedColor;
        }

        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            if (oldValue != null && oldValue.intValue() != -1) {
                Main.this.getShape(oldValue.intValue()).setFill(defaultColor);
            }
            if (newValue.intValue() != -1) {
                Main.this.getShape(newValue.intValue()).setFill(selectedColor);
            }
        }
    }
}
