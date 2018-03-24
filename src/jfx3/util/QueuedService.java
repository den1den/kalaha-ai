package jfx3.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueuedService<V> extends ScheduledService<V> {
    private Queue<Task<V>> queue = new LinkedBlockingQueue<>();

    protected void overwriteTask(Task<V> t) {
        cancel();
        queue.add(t);
        reset();
        start();
    }

    protected void scheduleTask(Task<V> t) {
        queue.add(t);
        kick();
    }

    @Override
    protected Task<V> createTask() {
        if (queue.isEmpty()) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING,
                "Starting while the queue is empty...");
        }
        // Also on application thread
        return queue.remove();
    }

    @Override
    protected void succeeded() {
        if (queue.isEmpty()) {
            cancel();
        }
        super.succeeded();
    }

    protected abstract class TickTask extends Task<Integer> {
        private final int ticks;
        private final long timeout;
        private final BooleanProperty ticked = new SimpleBooleanProperty(true);

        protected TickTask(int ticks, long timeout) {
            this.ticks = ticks;
            this.timeout = timeout;
            this.valueProperty().addListener(this::changed);
        }

        private void changed(ObservableValue o, Integer oldValue, Integer newValue) {
            tick();
            synchronized (ticked) {
                ticked.set(true);
                ticked.notify();
                //if(!isUpdated.getAndSet(true)){notify();}
            }
        }

        abstract protected void tick();

        @Override
        protected Integer call() throws InterruptedException {
            _wait();
            for (int i = 0; i < ticks; i++) {
                waitForTick(i);
                _wait();
            }
            return ticks;
        }

        private void _wait() throws InterruptedException {
            if (timeout > 0) {
                synchronized (this) {
                    wait(timeout);
                }
            }
        }

        private void waitForTick(int t) throws InterruptedException {
            ticked.set(false);
            updateValue(t);
            synchronized (ticked) {
                while (!ticked.get()) {
                    ticked.wait();
                }
            }
        }


//        private void update(Observable o, Number oldV, Number newV) {
//            int i = newV.intValue();
//
//            // unhighlight
//            synchronized (updated) {
//                updated.set(true);
//                updated.notify();
//            }
//        }
//
//        @Override
//        protected synchronized Integer call() throws Exception {
//            for (int i = 0; i <= maxI; i++) {
//                updated.set(false);
//                updateValue(i);
//                if (i != 0 || animateFirst) {
//                    long t = animationTimeout.get();
//                    if(t > 0) {
//                        wait(t);
//                    }
//                }
//                waitUpdate();
//            }
//            if(waitAfterwards){
//                long t = Long.max(animationTimeout.get(), 1500);
//                wait(t);
//            }
//            return maxI + 1;
//        }
//
//        private void waitUpdate() throws InterruptedException {
//            while (!updated.get()) {
//                synchronized (updated) {
//                    updated.wait();
//                }
//            }
//        }
    }
}
