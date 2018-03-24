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

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AnimatedTwoPlayerVis extends TwoPlayerVis {
    static long ANIMATION_TIMEOUT = 100;
    private static Paint defaultColor = Color.DODGERBLUE;
    private static Paint selectedColor = Color.BLUE;
    private static Paint selectedOpponentColor = Color.RED;
    final AtomicBoolean executing = new AtomicBoolean(false);
    final AnimatorService animatorService = new AnimatorService();
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

    class MoveAnimationTask extends Task<Integer> implements ChangeListener<Integer> {
        final boolean animateFirst;
        final int moveIndex;
        final int[] fields;
        final IntegerProperty selectedMove;
        final Match match;

        final AtomicBoolean updated = new AtomicBoolean(true);

        MoveAnimationTask(IntegerProperty selectedMove, int moveIndex, boolean animateFirst,
                          int[] fields, Match match) {
            this.selectedMove = selectedMove;
            this.animateFirst = animateFirst;
            this.moveIndex = moveIndex;
            this.fields = fields;
            this.match = match;
            System.out.println("Starting a moveNow animation, with end result points: " + Arrays.toString(match.getBoardState().getPointsCopy()) + "\n" + Arrays.toString(match.getBoardState().getFieldsCopy()));
            valueProperty().addListener(this);
        }

        @Override
        public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
            System.out.println("MoveAnimationTask.changed: oldValue = [" + oldValue + "], newValue = [" + newValue + "], updated = [" + updated.get() + "]");
            if (newValue == 0) {
                setField(moveIndex, 0);
                if (animateFirst) {
                    selectedMove.set(moveIndex);
                }
            } else if (newValue == -1) {
                setFields(match);
                selectedMove.set(-1);
            } else {
                int fieldIndex = (moveIndex + newValue) % fields.length;
                setField(fieldIndex, fields[fieldIndex] + 1);
                selectedMove.set(fieldIndex);
            }
            updated.set(true);
        }

        @Override
        protected Integer call() throws Exception {
            try {
                int stones = fields[moveIndex];
                synchronized (animatorService) {
                    updated.set(false);
                    updateValue(0);
                    if (animateFirst) {
                        animatorService.wait(ANIMATION_TIMEOUT);
                    }
                    while (!updated.get()) {
                        System.out.println("MoveAnimationTask.waiting for update");
                        animatorService.wait(100);
                    }
                    for (int i = 1; i <= stones; i++) {
                        updated.set(false);
                        updateValue(i);
                        animatorService.wait(ANIMATION_TIMEOUT);
                        while (!updated.get()) {
                            System.out.println("MoveAnimationTask.waiting for update 2");
                            animatorService.wait(100);
                        }
                    }

                    for (int i = moveIndex + 1; i <= moveIndex + stones; i++) {
                    }
                }
            } catch (Exception e) {
                System.err.println("Could not animate" + " isCancelled() = " + isCancelled()
                        + " isRunning() = " + isRunning()
                        + " isDone() = " + isDone());
                e.printStackTrace();
                throw e;
            }
            return -1;
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
