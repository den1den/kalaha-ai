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
import marblegame.Competition;
import marblegame.Match;
import marblegame.MatchBuilder;
import marblegame.players.Player;
import marblegame.players.SimplePlayer;
import net.PlayClient;

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
    public Text statusTextLeft;
    private SimplePlayer humanPlayer;
    private PlayClient networkPlayer;
    private Competition competition;
    private SetupConnectionService setupConnectionService = new SetupConnectionService();
    private DoNetworkMoveService moveService = new DoNetworkMoveService();
    private AnimatorService animatorService = new AnimatorService();
    private SimpleIntegerProperty selectedMove = new SimpleIntegerProperty(-1);
    private SimpleIntegerProperty opponentSelectedMove = new SimpleIntegerProperty(-1);

    public Main() {
        Match match;
        final Player[] players;
        setupConnectionService.setExecutor(networkWorkerExecutor);
        moveService.setExecutor(networkWorkerExecutor);

        humanPlayer = new SimplePlayer("Human");

        players = new Player[2];
        players[0] = humanPlayer;
        match = new MatchBuilder(players.length).createMatch();
        match = new MatchBuilder(players.length).createComputerWinMatch();
        match = new MatchBuilder(players.length).createHumanWinMatch();
        // match = new MatchBuilder(players.length).createHumanWonMatch();
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
                PlayClient result = (PlayClient) event.getSource().getValue();
                if (result.isConnected()) {
                    networkPlayer = result;
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
        displayBoardState();
    }

    private void displayBoardState() {
        int[] fields = competition.getFields();
        for (int i = 0; i < fields.length; i++) {
            setField(i, fields[i]);
        }
        int[] points = competition.getPoints();
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
        final int[] preHumanBoardState = competition.getFields();
        int gain = competition.move();
        int move = competition.getLastMove();
        final int[] preNetworkBoardState = competition.getFields();

        // Start human animation
        animatorService.setTask(new MoveAnimationTask(selectedMove, move, false, preHumanBoardState));
        animatorService.cancel();
        animatorService.reset();
        animatorService.start();
        System.out.println("Started human animation");

        int winner;
        winner = competition.calcWinner();
        if (winner != -1) {
            statusTextLeft.setText("Winner is: " + (winner == 0 ? "Human" : "Computer"));
            return;
        }

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
        if (competition.canPlay())
            moveService.start();
    }

    private class SetupConnectionService extends RerunnableService<PlayClient> {
        String host;

        private void setHost(String host) {
            this.host = host;
        }

        @Override
        protected Task<PlayClient> createTask() {
            final String host = this.host;
            return new Task<PlayClient>() {
                @Override
                protected PlayClient call() throws Exception {
                    return new PlayClient(host);
                }
            };
        }
    }

    private class DoNetworkMoveService extends RerunnableService<Integer> {
        @Override
        protected Task<Integer> createTask() {
            final PlayClient player = networkPlayer;
            final Match match = competition.getMatch();
            return new Task<Integer>() {
                @Override
                protected Integer call() throws Exception {
                    int move = player.getResponse(match);
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
        final int[] fields;
        final IntegerProperty selectedMove;

        MoveAnimationTask(IntegerProperty selectedMove, int moveIndex, boolean animateFirst, int[] fields) {
            this.selectedMove = selectedMove;
            this.animateFirst = animateFirst;
            this.moveIndex = moveIndex;
            this.fields = fields;
        }

        @Override
        protected Object call() throws Exception {
            try {
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
                    displayBoardState();
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
