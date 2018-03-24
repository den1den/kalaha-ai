package jfx3;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jfx3.components.PaneManager;
import jfx3.main.ChoosePlayer;
import jfx3.main.TwoPlayerScene;
import jfx3.opponent.AiManager;
import jfx3.opponent.ClientManager;
import jfx3.opponent.ServerManager;
import marblegame.gamemechanics.Match;

public class App extends Application {
    // Opponent managers
    final public ClientManager clientManager = new ClientManager();
    final public ServerManager hostingManager = new ServerManager();
    final public AiManager aiManager = new AiManager();
    // Data
    public final StringProperty playerName
        = new SimpleStringProperty(this, "Player name");
    public final StringProperty opponentName
        = new SimpleStringProperty(this, "Opponent name");
    public final StringProperty windowTitle
        = new SimpleStringProperty(this, "Hallo Dionne");
    // Configuration
    public final ObjectProperty<OpponentType> opponentType
        = new SimpleObjectProperty<>(this, "Opponent type");
    public final ObjectProperty<NetworkOpponent> networkOpponent
        = new SimpleObjectProperty<>(this, "Network opponent");
    public final ObjectProperty<Match> match
        = new SimpleObjectProperty<>(this, "MatchController");
    public final BooleanProperty turn
        = new SimpleBooleanProperty(this, "Turn");
    // Scenes
    public final PaneManager paneManager = new PaneManager();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Runnable onStart;
        {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(ChoosePlayer.class.getResource(
                ChoosePlayer.class.getSimpleName() + ".fxml"));

            BorderPane root = fxmlLoader.load();
            ChoosePlayer choosePlayer = fxmlLoader.getController();
            paneManager.addPane(root, choosePlayer);

            onStart = () -> {
                choosePlayer.opponentTypeComputer.setSelected(true);
                paneManager.nextPane();
            };
        }
        {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(TwoPlayerScene.class.getResource(
                TwoPlayerScene.class.getSimpleName() + ".fxml"));

            BorderPane root = fxmlLoader.load();
            TwoPlayerScene matchScene = fxmlLoader.getController();
            paneManager.addPane(root, matchScene);
        }

        primaryStage.setScene(new Scene(paneManager));
        primaryStage.titleProperty().bind(windowTitle);
        primaryStage.setOnCloseRequest(this::onCloseRequest);
        primaryStage.show();

        paneManager.bind(this);
        paneManager.activateFirstPane();

        // DEBUG:
        if (onStart != null) {
            Platform.runLater(onStart);
        }
    }

    private void onCloseRequest(WindowEvent event) {
        if (!paneManager.isFirst()) {
            paneManager.nextPane();
            event.consume();
        } else {
            paneManager.closePanes();
            clientManager.close();
            hostingManager.close();
            aiManager.close();
        }
    }

    @Override
    public String toString() {
        return "App";
    }

    public enum OpponentType {
        COMPUTER, CLIENT, HOST, LOCAL
    }

    public static class NetworkOpponent {
        private final String name;
        private final String host;

        public NetworkOpponent(String name, String host) {
            this.name = name;
            this.host = host;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getHost() {
            return host;
        }
    }
}
