package javafx;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Shape;
import marblegame.Competition;
import marblegame.Match;
import marblegame.players.*;

/**
 * Created by dennis on 5-10-17.
 */
public class ControllerOneInput extends ControllerTwoPlayers {

    private RecordedPlayer<SimplePlayer> left;
    int leftIndex = -1;

    private Player right;
    int rightIndex = -1;

    @Override
    protected Competition createNewPlayers(Match match) {
        left = new RecordedPlayer<>(new SimplePlayer("Human"));
        right = new RecordedPlayer<>(new AiPlayer("Computer", match));
        right = new RecordedPlayer<>(new NaivePlayer("Naive", match));
        return new Competition(match, left, right);
    }

    public void doRightMove(MouseEvent event) {
        if (competition.getTurn() != 1) {
            System.err.println("It is not your turn!");
            return;
        }
        if (rightIndex != -1) {
            getShape(rightIndex).setFill(defaultColor);
        }
        leftScore += competition.move();
        rightIndex = competition.getLastMove();
        getShape(rightIndex).setFill(selectedRightColor);
        setLeftScoreText();
        setBoardText();
    }

    public void clickedCircle(MouseEvent event) {
        Node source = (Node) event.getSource();
        if (this.gridpane.equals(source.getParent())) {
            int index = (int) source.getProperties().get("field leftIndex");
            if (competition.isInRange(index)) { // competition.canSet
                clickMove(index);
            } else {
                unclickMove();
            }
        }
    }

    private void unclickMove() {
        if (this.leftIndex == -1) {
            return;
        }
        Shape prev = getShape(this.leftIndex);
        prev.setFill(defaultColor);
    }

    private void clickMove(int index) {
        if (rightIndex != -1) {
            getShape(rightIndex).setFill(defaultColor);
            rightIndex = -1;
        }
        if (this.leftIndex == index) {
            doMove();
        } else {
            unclickMove();
            getShape(index).setFill(selectedLeftColor);
            this.leftIndex = index;
        }
    }

    public void doMove(MouseEvent event) {
        doMove();
    }

    private void doMove() {
        left.get().setMove(leftIndex);
        rightScore += competition.move();
        setRightScoreText();
        setBoardText();
        unclickMove();
        this.leftIndex = -1;
    }

}
