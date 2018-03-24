package talf.jfx;

import javafx.beans.property.StringProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import talf.mechanics.Match;

public class BoardPaneWithStatus extends BorderPane {
    private final Match match;
    BoardPane boardPane;
    StringProperty statusTextProp;

    public BoardPaneWithStatus(Match match, boolean canMoveSilver, boolean canMoveGold) {
        this.match = match;
        boardPane = new BoardPane(match, canMoveSilver, canMoveGold);
        boardPane.setOnMoved(this::onMoved);

        Text statusText = new Text();
        setCenter(boardPane);
        setBottom(statusText);
        statusTextProp = statusText.textProperty();

        updateStatus();
    }

    public void onMoved() {
        updateStatus();
    }

    private void updateStatus() {
        String player = match.isTurnGold() ? "Gold" : "Silver";
        statusTextProp.set(player + " " + (match.isFirsTurn() ? "first" : "second") + " turn");
    }
}
