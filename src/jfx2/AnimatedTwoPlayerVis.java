package jfx2;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import marblegame.gamemechanics.Competition;
import marblegame.gamemechanics.Match;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AnimatedTwoPlayerVis extends TwoPlayerVis {
    static long ANIMATION_TIMEOUT = 100;
    private static Paint defaultColor = Color.DODGERBLUE;
    private static Paint selectedColor = Color.BLUE;
    private static Paint selectedOpponentColor = Color.RED;
    final AtomicBoolean executing = new AtomicBoolean(false);
    AnimatorService animatorService = new AnimatorService();
    SimpleIntegerProperty selectedMove = new SimpleIntegerProperty(-1);
    SimpleIntegerProperty opponentSelectedMove = new SimpleIntegerProperty(-1);

    Alert alert = null;

    @Override
    void initialize() {
        super.initialize();

        // Set selection colorings
        selectedMove.addListener(new SelectionChangeListener(selectedColor));
        opponentSelectedMove.addListener(new SelectionChangeListener(selectedOpponentColor));
    }

    public MoveAnimationTask create(IntegerProperty selectedMove, int humanMove, boolean b, int[] preHumanBoardState, Competition competition) {
        return new MoveAnimationTask(selectedMove, humanMove, false, preHumanBoardState,
                competition.getMatch());
    }

    class MoveAnimationTask extends Task {
        final boolean animateFirst;
        final int moveIndex;
        final int[] fields;
        final IntegerProperty selectedMove;
        final Match match;

        MoveAnimationTask(IntegerProperty selectedMove, int moveIndex, boolean animateFirst,
                          int[] fields, Match match) {
            this.selectedMove = selectedMove;
            this.animateFirst = animateFirst;
            this.moveIndex = moveIndex;
            this.fields = fields;
            this.match = match;
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
                    setFields(match);
                    selectedMove.set(-1);
                }
            } catch (Exception e) {
                System.err.println("Could not animate" + " isCancelled() = " + isCancelled()
                        + " isRunning() = " + isRunning()
                        + " isDone() = " + isDone());
                e.printStackTrace();
                throw e;
            }
            return null;
        }
    }

    class AnimatorService extends RerunnableService {
        private Task task;

        void setTask(IntegerProperty selectedMove, int moveIndex, boolean animateFirst,
                     int[] fields, Match finalMatch) {
            this.task = new MoveAnimationTask(selectedMove, moveIndex, animateFirst,
                    fields, finalMatch);
        }

        @Override
        protected Task createTask() {
            return task;
        }

        void resetTo(IntegerProperty selectedMove, int moveIndex, boolean animateFirst,
                     int[] fields, Match finalMatch) {
            cancel();
            reset();
            setTask(selectedMove, moveIndex, animateFirst,
                    fields, finalMatch);
        }
    }

    private class SelectionChangeListener implements ChangeListener<Number> {
        final Paint selectedColor;

        SelectionChangeListener(Paint selectedColor) {
            this.selectedColor = selectedColor;
        }

        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            if (oldValue != null && oldValue.intValue() != -1) {
                getShape(oldValue.intValue()).setFill(defaultColor);
            }
            if (newValue.intValue() != -1) {
                getShape(newValue.intValue()).setFill(selectedColor);
            }
        }
    }
}
