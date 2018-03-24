package jfx3.main;

import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.util.Callback;
import jfx3.App;
import jfx3.components.PaneManager;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;


public class ChoosePlayer extends PaneManager.PaneTab {
    private static final int LOADING_NONE = -1;
    private static final int LOADING_OPPONENT_NETWORK = 0;
    private static final int LOADING_OPPONENT_HOSTING = 1;
    private final Timer textInputDelayer = new Timer();
    private final IntegerProperty loadingOpponent = new SimpleIntegerProperty(LOADING_NONE);
    public Text errorText;
    public TextField playerName;
    public Button startButton;
    public GridPane opponentGrid;
    public ChoiceBox<App.NetworkOpponent> networkOption;
    public ProgressIndicator networkLoading;
    public Text networkStatusText;
    public TextField hostPortText;
    public TextField localOpponentName;
    public Text statusLocalOpponentName;
    public ProgressIndicator hostLoading;
    public Text hostStatusText;
    public ToggleGroup opponentToggle = new ToggleGroup();
    public RadioButton opponentTypeComputer;
    public RadioButton opponentTypeNetwork;
    public RadioButton opponentTypeHost;
    public RadioButton opponentTypeLocal;
    TimerTask prev = null;
    private StringIntegerProperty hostPort = new StringIntegerProperty();
    private ObservableValue<App.OpponentType> opponentTypeProperty;

    @FXML
    void initialize() {
        // Setup the values and UI
        errorText.setVisible(false);
        networkLoading.setVisible(false);
        hostLoading.setVisible(false);
        networkOption.getItems().addAll(
            new App.NetworkOpponent("Dennis thuis server", "vandenbrand.eu"),
            new App.NetworkOpponent("Lokaal", "localhost"),
            new App.NetworkOpponent("Test (null)", null)
        );

        opponentTypeComputer.setToggleGroup(opponentToggle);
        opponentTypeLocal.setToggleGroup(opponentToggle);
        opponentTypeNetwork.setToggleGroup(opponentToggle);
        opponentTypeHost.setToggleGroup(opponentToggle);
        opponentTypeProperty = new ObjectBinding<App.OpponentType>() {
            {
                bind(opponentToggle.selectedToggleProperty());
            }

            @Override
            protected App.OpponentType computeValue() {
                Toggle radioButton = opponentToggle.getSelectedToggle();
                if (radioButton == null) return null;
                if (radioButton == opponentTypeComputer) return App.OpponentType.COMPUTER;
                if (radioButton == opponentTypeLocal) return App.OpponentType.LOCAL;
                if (radioButton == opponentTypeNetwork) return App.OpponentType.CLIENT;
                if (radioButton == opponentTypeHost) return App.OpponentType.HOST;
                throw new UnsupportedOperationException();
            }
        };

        // bind click listeners to selection boxes
        Callback<ToggleButton, EventHandler<Event>> makeRadioSelector =
            (ToggleButton t) -> ((Event e) -> t.setSelected(true));
        EventHandler<Event> onClickedLocal = makeRadioSelector.call(opponentTypeLocal);
        List<EventHandler<Event>> radioClickHandlers = Arrays.asList(
            makeRadioSelector.call(opponentTypeComputer),
            onClickedLocal,
            makeRadioSelector.call(opponentTypeNetwork),
            makeRadioSelector.call(opponentTypeHost)
        );
        for (Node child : opponentGrid.getChildren()) {
            Integer rowIndex = GridPane.getColumnIndex(child);
            if (rowIndex == null) rowIndex = 0;
            child.setOnMouseClicked(radioClickHandlers.get(rowIndex));
        }

        localOpponentName.setOnMouseClicked(onClickedLocal);
        localOpponentName.setOnAction(this::clickedStart);
        statusLocalOpponentName.setText("");

        // Add Loading animation
        loadingOpponent.addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() == LOADING_OPPONENT_HOSTING) {
                hostLoading.setVisible(true);
            } else if (oldValue.intValue() == LOADING_OPPONENT_HOSTING) {
                hostLoading.setVisible(false);
            }

            if (newValue.intValue() == LOADING_OPPONENT_NETWORK) {
                networkLoading.setVisible(true);
            } else if (oldValue.intValue() == LOADING_OPPONENT_NETWORK) {
                networkLoading.setVisible(false);
            }
        });
        networkStatusText.setText("");

        hostPortText.textProperty().addListener(new DelayedChangeListener<>(
            (String s) -> ChoosePlayer.this.hostPort.set(s)));
        hostPortText.setOnAction(new DirectChangeListener(() ->
            hostPort.set(hostPortText.getText())));
        hostStatusText.setText("");

        System.out.println(getClass() + " initialized");
    }

    private void tryConnectNetwork() {
        networkStatusText.setText("Connecting");
        loadingOpponent.set(LOADING_OPPONENT_NETWORK);
        App.NetworkOpponent opponent = networkOption.getSelectionModel().selectedItemProperty().get();
        app.clientManager.setOpponent(opponent);
    }

    private void tryConnectHost() {
        int port = hostPort.get();
        if (port <= 1024 || port >= 10000) {
            hostStatusText.setText("Invalid port name");
            app.hostingManager.cancel();
            loadingOpponent.set(LOADING_NONE);
            return;
        }
        loadingOpponent.set(LOADING_OPPONENT_HOSTING);
        hostStatusText.setText("Creating server on " + port);
        app.hostingManager.setPort(port);
    }

    @Override
    public void activeImpl() {
        // When this pane is becoming into focus

        // Network related
        app.clientManager.setOnFailed(this::clientManagerFailed);
        app.clientManager.setOnSucceeded(this::clientManagerSucceeded);


        // Hosting related
        app.hostingManager.setOnFailed(this::hostingManagerFailed);
        app.hostingManager.setOnSucceeded(this::hostingManagerSucceeded);

        // bind selected opponent to actions for the opponent service
        opponentToggle.selectedToggleProperty().addListener((o, old, selected) -> {
            if (selected != opponentTypeNetwork && selected != opponentTypeHost) {
                loadingOpponent.set(LOADING_NONE);
            }
            if (old == opponentTypeLocal) {
                app.opponentName.unbind();
            }

            // When the selection has changed
            if (selected == opponentTypeNetwork) {
                SingleSelectionModel<App.NetworkOpponent> oppSelection = networkOption.getSelectionModel();
                if (oppSelection.getSelectedItem() == null) {
                    oppSelection.selectFirst();
                } else {
                    tryConnectNetwork();
                }
            } else if (selected == opponentTypeLocal) {
                app.opponentName.bind(localOpponentName.textProperty());
            } else if (selected == opponentTypeHost) {
                if (hostPortText.getText().isEmpty()) {
                    hostPort.set(6022);
                } else {
                    tryConnectHost();
                }
            } else if (selected == opponentTypeComputer) {
                app.opponentName.set("Computer");
            } else throw new UnsupportedOperationException("selected unidentified Toggle");

            if (old == opponentTypeNetwork) {
                app.clientManager.disconnect();
                networkStatusText.setText("");
            } else if (old == opponentTypeHost) {
                app.hostingManager.disconnect();
                hostStatusText.setText("");
            }
        });

        // Fill in defaults
        playerName.setText("Dennis");

        // Enable the action button
        playerName.onActionProperty().bind(startButton.onActionProperty());
    }

    private void hostingManagerSucceeded(Event event) {
        loadingOpponent.set(LOADING_NONE);
        hostStatusText.setText("Connected");
    }

    private void clientManagerSucceeded(Event event) {
        loadingOpponent.set(LOADING_NONE);
        networkStatusText.setText("Connected");
    }

    private void hostingManagerFailed(Event event) {
        int fc = app.hostingManager.getCurrentFailureCount();
        int maxFc = app.hostingManager.getMaximumFailureCount();
        if (fc == maxFc - 1) {
            hostStatusText.setText("Connection failed");
            loadingOpponent.set(LOADING_NONE);
            return;
        }
        if (fc != 0) {
            hostStatusText.setText(
                hostStatusText.getText() + "."
            );
        } else {
            hostStatusText.setText("Connection failed");
        }
    }

    private void clientManagerFailed(Event event) {
        int fc = app.clientManager.getCurrentFailureCount();
        int maxFc = app.clientManager.getMaximumFailureCount();
        if (fc == maxFc - 1) {
            // Network attempts ends
            networkStatusText.setText("Connection failed");
            loadingOpponent.set(LOADING_NONE);
            return;
        }
        if (fc != 0) {
            // Network is failing
            networkStatusText.setText(
                networkStatusText.getText() + "."
            );
            return;
        }
        // Network attempts start to fail
        networkStatusText.setText("Connection failed");
    }

    private ChangeListener<Object> ensureToggleListener(Toggle ot, Runnable otherwise) {
        return (observable, oldValue, newValue) -> {
            Toggle s = opponentToggle.getSelectedToggle();
            if (s != ot) {
                opponentToggle.selectToggle(ot);
            } else {
                otherwise.run();
            }
        };
    }

    @Override
    public void closeImpl() {
        textInputDelayer.cancel();
    }

    public void clickedStart(ActionEvent ignore) {
        Toggle selected = opponentToggle.getSelectedToggle();
        if (selected == null) {
            errorText.setText("Please select an opponent");
            errorText.setVisible(true);
            return;
        }
        if (playerName.getText().isEmpty()) {
            errorText.setText("Player name cannot be empty");
            errorText.setVisible(true);
            return;
        }
        if (selected == opponentTypeLocal && localOpponentName.getText().isEmpty()) {
            errorText.setText("Opponent must have a name");
            errorText.setVisible(true);
            return;
        }

        errorText.setVisible(false);
        networkLoading.setVisible(false);

        app.paneManager.nextPane();
    }

    @Override
    public void onBind() {
        // Setup all actions which depend on the App
        app.opponentType.bind(getOpponentTypeProperty());

        app.networkOpponent.bind(getNetworkOpponentProperty());
        networkOption.getSelectionModel().selectedItemProperty().addListener(
            ensureToggleListener(opponentTypeNetwork, this::tryConnectNetwork));

        hostPort.addListener(
            ensureToggleListener(opponentTypeHost, this::tryConnectHost));

        app.playerName.bind(playerName.textProperty());
    }

    public ReadOnlyObjectProperty<App.NetworkOpponent> getNetworkOpponentProperty() {
        return networkOption.getSelectionModel().selectedItemProperty();
    }

    public ObservableValue<App.OpponentType> getOpponentTypeProperty() {
        return opponentTypeProperty;
    }

    private static class StringIntegerProperty extends SimpleIntegerProperty {
        public StringIntegerProperty() {
            super(-1);
        }

        public void set(String s) {
            try {
                set(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                set(-1);
            }
        }
    }

    private class DirectChangeListener implements EventHandler<ActionEvent> {
        private final Runnable tConsumer;

        public DirectChangeListener(Runnable tConsumer) {
            this.tConsumer = tConsumer;
        }

        @Override
        public void handle(ActionEvent event) {
            if (prev != null) {
                prev.cancel();
                prev = null;
            }
            tConsumer.run();
        }
    }

    private class DelayedChangeListener<T> implements ChangeListener<T> {
        private final Consumer<T> laterConsumer;

        public DelayedChangeListener(Consumer<T> laterConsumer) {
            this.laterConsumer = laterConsumer;
        }

        @Override
        public void changed(ObservableValue<? extends T> observable, T oldValue, T
            newValue) {
            if (prev != null) {
                prev.cancel();
            }
            prev = new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() ->
                        laterConsumer.accept(newValue)
                    );
                }
            };
            textInputDelayer.schedule(prev, 1000);
        }
    }
}
