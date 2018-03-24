package jfx3.components;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import jfx3.util.QueuedService;
import marblegame.gamemechanics.BoardState;

import static marblegame.Util.sum;

public class BoardAnimator extends QueuedService<Integer> {
    public final LongProperty animationTimeout = new SimpleLongProperty();
    private final BoardDrawer drawer;

    public BoardAnimator(BoardDrawer drawer) {
        this.drawer = drawer;
        setRestartOnFailure(false);
    }

    public void moveHuman(int move, BoardState inital, boolean animateLastState, BoardState post,
                          boolean waitAfterAnimation, Runnable then) {
        scheduleTask(new Animation(
            inital, post, move, then,
            false, animateLastState, waitAfterAnimation
        ));
    }

    public void moveComputer(int move, BoardState inital, boolean animateLastState, BoardState post,
                             boolean waitAfterAnimation, Runnable then) {
        scheduleTask(new Animation(
            inital, post, move, then,
            true, animateLastState, waitAfterAnimation
        ));
        kick();
    }

    private abstract class AnimationTaskBase extends TickTask {
        final BoardState initial;
        final BoardState postState;
        final int move;
        final Runnable post;
        final int stones;

        AnimationTaskBase(int ticks, BoardState initial, BoardState postState, int move,
                          Runnable post) {
            super(ticks, animationTimeout.get());
            this.initial = initial;
            this.postState = postState;
            this.move = move;
            this.post = post;
            this.stones = initial.getFields(move);
        }
    }

    private class Animation extends AnimationTaskBase {

        private final IntegerProperty selected;
        private final boolean animateFirst;
        private final boolean animateLast;
        private final boolean endDelay;

        private int offset = -1;

        private Animation(BoardState initial, BoardState postState, int move, Runnable post,
                          boolean animateFirst, boolean animateLast, boolean endDelay) {
            super(initial.getFields(move) + sum(animateFirst, animateLast, endDelay),
                initial, postState, move, post);
            this.selected = drawer.selectionField;
            this.animateFirst = animateFirst;
            this.animateLast = animateLast;
            this.endDelay = endDelay;
        }

        @Override
        protected void running() {
            tick();
        }

        @Override
        protected void tick() {
            if (offset == -1) {
                if (!animateFirst) {
                    drawer.setField(move, 0);
                    offset++;
                }
            } else if (offset == stones) {
                if (!animateLast) {
                    drawer.draw(postState);
                    if (endDelay) {
                        //->s2
                        offset++;
                    } else {
                        //->s3
                        selected.set(-1);
                        offset += 2;
                    }
                }
            } else if (offset == stones + 1) {
                if (!endDelay) {
                    //->s3
                    selected.set(-1);
                    offset++;
                }
            } else assert offset <= stones + 2;
            offset++;
            int newField = (move + Math.min(offset, stones)) % initial.getNFields();
            if (offset == 0) {
                drawer.setField(move, 0);
                if (animateFirst) {
                    drawer.selectionField.set(newField);
                }
            } else if (offset <= stones) {
                drawer.setField(newField, initial.getFields(newField) + 1);
                drawer.selectionField.set(newField);
            } else if (offset == stones + 1) {
                drawer.draw(postState);
            } else if (offset == stones + 2) {
                drawer.selectionField.set(-1);
            } else {
                post.run();
            }
        }

        @Override
        protected void succeeded() {
            tick();
        }
    }

}
